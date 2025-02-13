from flask import Flask, request, jsonify
from flask_cors import CORS
import random
import google.generativeai as genai
import requests
from bs4 import BeautifulSoup
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

# 건강 체크 엔드포인트
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'ok'})

# 뉴스 검색 함수
@retry_on_error(max_retries=3, delay=1)
def search_news(keyword):
    try:
        url = f"https://news.google.com/rss/search?q={keyword}&hl=en-US&gl=US&ceid=US:en"
        response = requests.get(url)
        soup = BeautifulSoup(response.content, 'xml')
        items = soup.find_all('item', limit=3)
        
        news_articles = []
        for item in items:
            article = {
                'title': item.title.text,
                'url': item.link.text,
                'description': item.description.text if item.description else "설명 없음"
            }
            news_articles.append(article)
        return news_articles
    except Exception as e:
        print(f"Error in search_news: {str(e)}")
        raise

# 뉴스 컨텍스트 생성
@retry_on_error(max_retries=3, delay=1)
def generate_news_context(word):
    try:
        prompt = f"""
        Given the exact word "{word}", create a search phrase that will find news articles directly related to this specific word.
        Requirements:
        1. The search phrase MUST find articles about this EXACT word/concept
        2. DO NOT change or replace the original word
        3. Focus on current events and news specifically about "{word}"

        Return only the exact word "{word}" as the search phrase, no additional context or explanation.
        """
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        print(f"Error in generate_news_context: {str(e)}")
        raise

# 뉴스 한글 요약 함수
@retry_on_error(max_retries=3, delay=1)
def summarize_news_in_korean(text):
    try:
        prompt = f"""
        Summarize the following news article in Korean in 4-5 sentences:
        
        {text}
        
        Rules:
        1. Keep it concise and clear
        2. Use natural Korean language
        3. Focus on the main points
        4. Return only the summary, no additional text
        """
        
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        print(f"Error in summarize_news: {str(e)}")
        return "요약을 생성할 수 없습니다."

# 오답 생성 함수
@retry_on_error(max_retries=3, delay=1)
def generate_wrong_answers(prompt):
    try:
        response = model.generate_content(prompt)
        answers = [ans.strip() for ans in response.text.split(',')]
        return answers[:3]
    except Exception as e:
        print(f"Error in generate_wrong_answers: {str(e)}")
        raise

# 뉴스 추천 API 엔드포인트
@app.route('/Python/generate-news', methods=['POST'])
@limiter.limit("10 per minute")
def news_recommend():
    try:
        data = request.get_json()
        if not data or 'word' not in data:
            return jsonify({'error': 'No word provided'}), 400
            
        word = data['word']
        search_context = generate_news_context(word)
        news_articles = search_news(search_context)
        
        if not news_articles:
            return jsonify({
                'error': 'No news found',
                'word': word,
                'search_context': search_context
            }), 404
            
        # 각 기사에 대한 한글 요약 추가
        for article in news_articles:
            article['korean_summary'] = summarize_news_in_korean(article['description'])
        
        return jsonify({
            'word': word,
            'search_context': search_context,
            'news': news_articles
        })
        
    except Exception as e:
        print(f"Error in news_recommend: {str(e)}")
        return jsonify({'error': str(e)}), 500

# 테스트 생성 API 엔드포인트
@app.route('/Python/generate-test', methods=['POST'])
@limiter.limit("10 per minute")
def generate_test():
    try:
        data = request.get_json()
        if not data:
            return jsonify({'error': 'No data provided'}), 400
            
        words = data.get('words', [])
        test_type = data.get('test_type')
        
        if not words or not isinstance(words, list):
            return jsonify({'error': 'Valid words list is required'}), 400
            
        if not test_type or test_type not in ['meaning', 'word']:
            return jsonify({'error': 'Valid test_type is required'}), 400

        target_count = min(len(words), 20)
        random.shuffle(words)
        
        questions = []
        failed_words = []
        processed_words = set()
        
        if len(words) > 20:
            words = random.sample(words, 20)

        # 첫 번째 시도
        for word_data in words:
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
                    continue

                if word in processed_words:
                    continue
                    
                if test_type == 'meaning':
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
                    
                else:
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

        # 재시도 로직
        retry_count = 0
        while len(questions) < target_count and failed_words:
            retry_count += 1
            print(f"Retry attempt {retry_count} for failed words")
            
            retry_words = failed_words.copy()
            failed_words = []
            
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
                        continue

                    if word in processed_words:
                        continue

                    if test_type == 'meaning':
                        prompt = f"'{meaning}'와 완전히 다른 의미를 가진 한국어 단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."
                    else:
                        prompt = f"'{word}'와 완전히 다른 의미를 가진 영단어를 품사 상관없이 3개만 나열해주세요. 쉼표로 구분하고 다른 설명은 하지 마세요."

                    wrong_answers = generate_wrong_answers(prompt)

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
                    time.sleep(1)

        if len(questions) < target_count:
            print(f"Warning: Could only generate {len(questions)} out of {target_count} questions after {retry_count} retries")
            return jsonify({
                'error': f'목표한 {target_count}개의 문제 중 {len(questions)}개만 생성되었습니다.',
                'questions': questions,
                'total': len(questions)
            }), 206

        return jsonify({
            'questions': questions,
            'total': len(questions)
        })

    except Exception as e:
        print(f"Error in generate_test: {str(e)}")
        return jsonify({'error': str(e)}), 400

# 소설 생성 API 엔드포인트
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
    app.run(host='0.0.0.0', port=5001, debug=True)