from flask import Flask, request, jsonify
from flask_cors import CORS
import random
import google.generativeai as genai
import json
import os 
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import time
from functools import wraps

# Flask 앱 초기화
app = Flask(__name__)
CORS(app)  # CORS 활성화

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

# Gemini API 호출 함수
@retry_on_error(max_retries=3, delay=1)
def generate_wrong_answers(prompt):
    try:
        response = model.generate_content(prompt)
        answers = [ans.strip() for ans in response.text.split(',')]
        return answers[:3]  # 최대 3개의 답변만 반환
    except Exception as e:
        print(f"Error in generate_wrong_answers: {str(e)}")
        raise

@app.route('/Python/generate-test', methods=['POST'])
@limiter.limit("10 per minute")  # Rate limit 설정
def generate_test():
    try:
        data = request.get_json()
        
        # 입력 데이터 검증
        if not data:
            return jsonify({'error': 'No data provided'}), 400
            
        words = data.get('words', [])
        test_type = data.get('test_type')
        
        if not words or not isinstance(words, list):
            return jsonify({'error': 'Valid words list is required'}), 400
            
        if not test_type or test_type not in ['meaning', 'word']:
            return jsonify({'error': 'Valid test_type is required'}), 400

        # 목표 문제 수 설정 (최대 20개)
        target_count = min(len(words), 20)
        
        # 단어 목록 섞기
        random.shuffle(words)
        
        questions = []
        failed_words = []  # 실패한 단어들 추적
        processed_words = set()  # 이미 처리된 단어 추적
        
        # 단어 수 제한
        if len(words) > 20:
            words = random.sample(words, 20)

        questions = []
        
        # 첫 번째 시도
        for word_data in words:
            if len(questions) >= target_count:
                break
                
            try:
                # 단어 형식 확인 및 추출
                if 'english' in word_data and 'korean' in word_data:
                    word = word_data['english']
                    meaning = word_data['korean']
                elif 'word' in word_data and 'meaning' in word_data:
                    word = word_data['word']
                    meaning = word_data['meaning']
                else:
                    continue

                # 이미 처리된 단어 스킵
                if word in processed_words:
                    continue
                    
                if test_type == 'meaning':  # 영어 → 한글
                    prompt = f"'{meaning}'와 완전히 다른 의미를 가진 한국어 단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."
                    wrong_answers = generate_wrong_answers(prompt)
                    
                    if len(wrong_answers) < 3:
                        failed_words.append(word_data)
                        continue
                    
                    question = {
                        'word': word,
                        'choices': [meaning] + wrong_answers,
                        'correct_answer': 0
                    }
                    
                else:  # 한글 → 영어
                    prompt = f"'{word}'와 완전히 다른 의미를 가진 영단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."
                    wrong_answers = generate_wrong_answers(prompt)
                    
                    if len(wrong_answers) < 3:
                        failed_words.append(word_data)
                        continue
                    
                    question = {
                        'word': meaning,
                        'choices': [word] + wrong_answers,
                        'correct_answer': 0
                    }

                # 보기 섞기
                choices = question['choices']
                correct_choice = choices[0]
                random.shuffle(choices)
                question['correct_answer'] = choices.index(correct_choice)
                
                questions.append(question)
                processed_words.add(word)

            except Exception as e:
                print(f"Error processing word {word if 'word' in locals() else 'unknown'}: {str(e)}")
                failed_words.append(word_data)
                continue

        # 목표 문제 수에 도달할 때까지 실패한 단어들 재시도
        retry_count = 0
        while len(questions) < target_count and failed_words:
            retry_count += 1
            print(f"Retry attempt {retry_count} for failed words")
            
            retry_words = failed_words.copy()
            failed_words = []  # 실패 목록 초기화
            
            for word_data in retry_words:
                if len(questions) >= target_count:
                    break
                    
                try:
                    if 'english' in word_data and 'korean' in word_data:
                        word = word_data['english']
                        meaning = word_data['korean']
                    elif 'word' in word_data and 'meaning' in word_data:
                        word = word_data['word']
                        meaning = word_data['meaning']
                    else:
                        print(f"Skipping invalid word_data format: {word_data}")
                        continue

                    if word in processed_words:
                        print(f"Skipping already processed word: {word}")
                        continue

                    # 재시도 프롬프트
                    if test_type == 'meaning':
                        prompt = f"'{meaning}'와 완전히 다른 의미를 가진 한국어 단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."
                        wrong_answers = generate_wrong_answers(prompt)
                    else:
                        prompt = f"'{word}'와 완전히 다른 의미를 가진 영단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."
                        wrong_answers = generate_wrong_answers(prompt)

                    # 오답 3개 추가
                    if len(wrong_answers) < 3:
                        failed_words.append(word_data)
                        continue

                    question = {
                        'word': word if test_type == 'meaning' else meaning,
                        'choices': [meaning if test_type == 'meaning' else word] + wrong_answers,
                        'correct_answer': 0
                    }

                    choices = question['choices']
                    correct_choice = choices[0]
                    random.shuffle(choices)
                    question['correct_answer'] = choices.index(correct_choice)

                    questions.append(question)
                    processed_words.add(word)

                except Exception as e:
                    print(f"Error in retry {retry_count} for word {word if 'word' in locals() else 'unknown'}: {str(e)}")
                    failed_words.append(word_data)
                    time.sleep(1)  # API 요청 간 짧은 대기 시간 추가

        # 최종 결과 반환
        if len(questions) < target_count:
            print(f"Warning: Could only generate {len(questions)} out of {target_count} questions after {retry_count} retries")
            return jsonify({
                'error': f'목표한 {target_count}개의 문제 중 {len(questions)}개만 생성되었습니다.',
                'questions': questions,
                'total': len(questions)
            }), 206  # Partial Content
        
        return jsonify({
            'questions': questions,
            'total': len(questions)
        })

    except Exception as e:
        print(f"Error in generate_test: {str(e)}")
        return jsonify({'error': str(e)}), 400

if __name__ == '__main__':
    app.run(port=5001)



# 
# {
#     "type": "객관식",
#     "words": [
#         {"english": "apple", "korean": "사과"},
#         {"english": "banana", "korean": "바나나"},
#         // ...더 많은 단어들
#     ]
# }
# 

