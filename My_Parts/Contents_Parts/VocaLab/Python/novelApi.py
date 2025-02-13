from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import os
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import time
from functools import wraps

# Flask 앱 초기화
app = Flask(__name__)
CORS(app) # CORS 활성화

# Rate Limiter 설정
limiter = Limiter(
    app=app,
    key_func=get_remote_address,
    default_limits=["200 per day", "50 per hour"]
)

# 재시도 데코레이터
def retry_on_error(max_retries=3, delay=1):
    def decorator(func):
        @wraps(func)
        def wrapper(*args, **kwargs):
            retries = 0
            while retries < max_retries:
                try:
                    return func(*args, **kwargs)
                except Exception as e:
                    print(f"Retry {retries + 1}/{max_retries} due to error: {str(e)}")
                    retries += 1
                    if retries == max_retries:
                        raise e
                    time.sleep(delay * retries)
            return func(*args, **kwargs)
        return wrapper
    return decorator

# 환경 변수 로드
load_dotenv('env/api.env')

# API 키 설정
apiKey = os.getenv('gemini_api_key')
print(f"Loaded API Key: {apiKey}")

# Google Generative AI API 설정
genai.configure(api_key=apiKey)
model = genai.GenerativeModel('gemini-1.5-flash')

# 건강 체크 엔드포인트(서버가 정상적으로 실행 중인지 확인하는 용도)
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'ok'})

@app.route('/Python/generate-novel', methods=['POST'])
@limiter.limit("10 per minute")
@retry_on_error(max_retries=3, delay=1)
def generate_novel():
    try:
        data = request.get_json()
        if not data or 'words' not in data:
            return jsonify({'error': 'No words provided'}), 400

        words = data['words']
        word_list = [f"{word['word']} ({word['meaning']})" for word in words]
        
        prompt = f"""
        다음 영단어들을 모두 사용하여 흥미로운 짧은 이야기를 만들어주세요:
        {', '.join(word_list)}

        요구사항:
        1. 모든 단어를 최소 1번 이상 사용할 것
        2. 사용된 영단어는 대소문자 구분하지 않을 것
        3. 이야기는 영어로 작성해주고 작성을 다 작성한 뒤, 한글 해석도 같이 작성해줄 것. 사용된 영단어는 괄호 안에 표시 ex) I like (apple).
        4. 이야기는 도입-전개-결말 구조를 가질 것
        5. 전체 길이는 약 800-1200자 정도
        6. 이야기 제목, 이야기 본문, 이야기 해석은 형식을 따르고 마지막에 사용된 단어 목록은 특히 띄워주는데 예시를 따를 것
        7. 사용된 단어 목록은 밑의 예시를 따르는데, 사용된 단어를 설명할 단어만 ()표시해줄것.
        8. 사용된 단어 목록은 밑의 예시를 따르는데, 단어가 여러번 사용되었다면 사용된 모든 문장을 써줄 것

        형식:
        
        제목:
         
        [이야기 제목]

        [이야기 본문]

        [이야기 제목 해석]
        
        [이야기 해석]
        
        ---
        [사용된 단어 목록]
        ex)
        ① apple(사과):
            • I like (apple).
            - 해석: 나는 사과를 좋아한다.
            
            • (Apple) is very delicious.
            - 해석: 사과는 매우 맛있다.<br>
          
        ② go(가다):
            • I (go) to school.
            - 해석: 나는 학교에 간다.
            
            • I want to (go) to that cafe because I want to eat their apple pie.
            - 해석: 나는 그 곳의 애플파이를 먹고싶기 때문에 그 카페에 가고 싶다.<br>
        """

        response = model.generate_content(prompt)
        story = response.text.strip()

        return jsonify({
            'story': story,
            'wordCount': len(words)
        })

    except Exception as e:
        print(f"Error in generate_novel: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(port=5001)
