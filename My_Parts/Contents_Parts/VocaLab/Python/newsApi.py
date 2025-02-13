
from flask import Flask, request, jsonify
from flask_cors import CORS
import google.generativeai as genai
import requests
from bs4 import BeautifulSoup
import os
from dotenv import load_dotenv
from flask_limiter import Limiter
from flask_limiter.util import get_remote_address
import time
from functools import wraps

# Flask 앱 초기화
app = Flask(__name__)
app.config['DEBUG'] = True
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

# 뉴스 검색 함수
@retry_on_error(max_retries=3, delay=1)
def search_news(keyword):
    try:
        # Google 뉴스 검색 URL
        url = f"https://news.google.com/rss/search?q={keyword}&hl=en-US&gl=US&ceid=US:en"
        
        # RSS 피드 가져오기
        response = requests.get(url)
        soup = BeautifulSoup(response.content, 'xml')
        
        # 뉴스 항목 찾기 (최대 3개)
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

# Gemini를 사용한 뉴스 컨텍스트 생성
@retry_on_error(max_retries=3, delay=1)
def generate_news_context(word):
    try:
        prompt = f"""
        Create a news search context for the word "{word}". 
        Consider:
        1. The word's meaning and usage in current events
        2. Possible related topics and fields
        3. Recent developments or trends related to this word
        
        Return only the most relevant search phrase in English, no explanation needed.
        """
        
        response = model.generate_content(prompt)
        return response.text.strip()
    except Exception as e:
        print(f"Error in generate_news_context: {str(e)}")
        raise

# 뉴스 한글 요약 함수 추가
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
    
@app.route('/Python/generate-news', methods=['POST'])
@limiter.limit("10 per minute")  # Rate limit 설정
def news_recommend():
    try:
    
        data = request.get_json()
        
        # 입력 데이터 검증
        if not data or 'word' not in data:
            print("Error: No word provided in request")
            return jsonify({'error': 'No word provided'}), 400
            
        word = data['word']
        
        # 검색 컨텍스트 생성
        search_context = generate_news_context(word)
        
        # 뉴스 검색
        news_articles = search_news(search_context)
        
        if not news_articles:
            return jsonify({
                'error': 'No news found',
                'word': word,
                'search_context': search_context
            }), 404
        
        # 각 기사에 대한 한글 요약 추가
        # for article in news_articles:
        #     article['korean_summary'] = summarize_news_in_korean(article['description'])
        # 각 기사에 대한 한글 요약 추가
        for i, article in enumerate(news_articles):
            print(f"\n=== Article {i+1} Summary Process ===")
            print(f"Original description: {article['description'][:200]}...")  # 원본 텍스트 확인 (앞부분 200자만)
            try:
                summary = summarize_news_in_korean(article['description'])
                print(f"Generated summary: {summary}")
                article['korean_summary'] = summary
            except Exception as e:
                print(f"Error during summarization: {str(e)}")
                print(f"Full error details:")
                import traceback
                print(traceback.format_exc())
                article['korean_summary'] = "요약 중 오류가 발생했습니다."
            
        return jsonify({
            'word': word,
            'search_context': search_context,
            'news': news_articles
        })
        
    except Exception as e:
        print(f"Error in news_recommend: {str(e)}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(port=5001)
    
# 예시 요청 형식:
# {
#     "word": "artificial intelligence"
# }
#
# 응답 형식:
# {
#     "word": "artificial intelligence",
#     "search_context": "AI developments in technology",
#     "news": [
#         {
#             "title": "뉴스 제목",
#             "url": "뉴스 URL",
#             "description": "뉴스 설명"
#         },
#         ...
#     ]
# }