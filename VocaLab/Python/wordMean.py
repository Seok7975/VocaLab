from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import json
import os 
from dotenv import load_dotenv

load_dotenv('env/api.env')  # env/api.env 파일 로드

apiKey = os.getenv('gemini_api_key')
print(f"Loaded API Key: {apiKey}")  # API 키가 제대로 로드되었는지 출력
# Google Generative AI API 설정
genai.configure(api_key=apiKey)
model = genai.GenerativeModel('gemini-1.5-flash')
# generation_config = genai.GenerationConfig(temperature=0)

app = Flask(__name__)
CORS(app)

@app.route('/Python/word-mean', methods=['POST'])
def word_mean():
    data = request.get_json()
    word = data.get('word')
    if not word:
        return jsonify({"error": "word parameter is missing"}), 400  # 오류 응답

    print("Send to Gemini: ",word)
    # Google Generative AI API 호출
    try:
        response = model.generate_content(
            f"{word} 을 {{word:word, partofspeech:품사, mean:한국어 뜻, speech:발음기호, example:생성한 예문(예문의 뜻)}} (단,예문은 1개이며 '예문(뜻)'형태여야함. 그리고 json 배열을 {word}로 감싸면 안됨.) 형태로 json 형식으로 1개만 보내시오."
        )
        response_content = response.text
        print(response_content)

        # JSON 데이터 추출
        json_start = response_content.find("{")
        json_end = response_content.rfind("}")
        if json_start == -1 or json_end == -1:
            return jsonify({"error": "JSON data not found in AI response"}), 500  # 오류 응답

        json_text = response_content[json_start:json_end + 1]
        
        print(json_text)

        # JSON 텍스트를 파싱
        parsed_json = json.loads(json_text)

        # 만약 응답이 단어를 키로 가지는 형태라면 내부 객체만 추출
        if isinstance(parsed_json, dict) and len(parsed_json) == 1 and word.lower() in parsed_json:
            parsed_json = parsed_json[word.lower()]

        # 파싱된 데이터를 파일로 저장 (옵션)
        with open("word_mean.json", "w", encoding="utf-8") as json_file:
            json.dump(parsed_json, json_file, ensure_ascii=False, indent=4)
        
        return jsonify(parsed_json)

    except json.JSONDecodeError as e:
        return jsonify({"error": "Failed to parse JSON", "details": str(e)}), 500

    except Exception as e:
        return jsonify({"error": "An unexpected error occurred", "details": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)