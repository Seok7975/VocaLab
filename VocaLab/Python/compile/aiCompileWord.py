from flask import Flask, request, jsonify 
from flask_cors import CORS
from bs4 import BeautifulSoup
import requests
import pytesseract
from PIL import Image
import fitz 
import docx
import os
import io
import google.generativeai as genai
from config import Config
from dotenv import load_dotenv
import uuid

# .env 파일 로드
load_dotenv('../env/api.env')

app = Flask(__name__)
CORS(app)

# Tesseract 경로 설정
pytesseract.pytesseract.tesseract_cmd = r"/usr/bin/tesseract"

# Google Gemini API 설정
genai.configure(api_key=os.getenv('gemini_api_key'))
system_instruction = (
    f"단어를 json으로 만들 때 관사, be 동사, 인칭 대명사, 지시 대명사, 고유 명사(사람 이름, 회사 이름 등)는 제외해주세요."
    f"중복된 단어는 제거하고, 최소 1개에서 최대 200개의 단어를 소문자로 보여주세요."
    f"jsonArray중 json데이터의 key는 \"단어\", \"뜻\" 한개만 오고, key가 \"단어\"인 value에는 영어단어가, key가 \"뜻\"인 value에는 품사와 단어뜻을 넣어주세요."
    f"뜻은 무조건 한국어입니다 뜻 앞에 품사의 영어약어도 함께 넣어주세요., json데이터의 key도 한글로 작성해 주세요"
    f"결과에는 어떠한 말도 넣지 말고 json데이터만 반환해 주세요"
) 
model = genai.GenerativeModel('gemini-1.5-flash', system_instruction=system_instruction)

@app.route('/apiCompile', methods=['POST'])
def compile_word():
    print("--python--")
    try:
        # [1] 요청 데이터 처리
        data = request.json
        print(f"📌 요청 데이터 확인: {data}")  # [추가] 요청 데이터 출력
        compile_source = data.get("compileSource")  # URL 또는 텍스트 데이터
        original_files = data.get("originalFiles", [])  # 파일 데이터 리스트
        print(f"파일 유무 확인 : {original_files}")
        base_url = "https://vocalab.21v.in"  # Spring 서버 주소

        file_data = ""  # 파일 처리 결과 저장
        text_data = ""  # 텍스트 데이터 저장

        # [2] 파일 유무 및 소스 타입 처리
        if original_files:  # 파일이 있는 경우
            print("파일이 업로드되었습니다.")
            file_data = process_uploaded_files(original_files, base_url)
            print(f"file_data : {file_data}")

            if compile_source:  # 파일 있고 compileSource도 있는 경우
                if is_url(compile_source):
                    print("경우 1: 파일 유, compileSource = URL")
                    text_data = extract_text_from_url(compile_source)
                else:
                    print("경우 2: 파일 유, compileSource = 텍스트")
                    text_data = compile_source
        else:  # 파일이 없는 경우
            print("파일이 업로드되지 않았습니다.")
            if not compile_source:
                return jsonify({"error": "파일과 소스 데이터가 모두 제공되지 않았습니다."}), 400
            
            if is_url(compile_source):
                print("경우 3: 파일 무, compileSource = URL")
                text_data = extract_text_from_url(compile_source)
            else:
                print("경우 4: 파일 무, compileSource = 텍스트")
                text_data = compile_source

        print(f"최종 file_data : {file_data}")    
        print(f"최종 text_data : {text_data}")

        # [3] AI 요청 데이터 생성
        line = (
            f"아래 데이터1과 데이터2에서 문장이 있다면 문맥을 분석해 단어를 영어 단어 단위로 나눠주세요."
            f"단어의 뜻은 문맥상 적합한 뜻을 우선으로 넣고, 문맥이 없다면 일반적으로 사용 빈도가 높은 뜻을 작성하세요."
            f"csv파일로 저장하기 쉬운 json데이터가 필요하고, csv헤더로 \"단어\",\"뜻\"이 올거에요."
            f"데이터1 : {file_data}\n"
            f"데이터2 : {text_data}"
        )
        
        print(f"===========[요청 사항]=============")
        print(f"{line}")
        
        # [4] AI API 호출
        print("================[응답 결과]================")
        response = send_text_to_ai(line)
        print(f"ai 결과 : {response}")
        print("================================")

        # [5] 응답 반환
        return jsonify(response)

    except Exception as e:
        print(f"Error in compile_word: {str(e)}")
        return jsonify({"error": "처리 중 오류 발생", "details": str(e)}), 500

def is_url(string):
    if not string:
        return False
    return string.startswith("http://") or string.startswith("https://")

def process_uploaded_files(files, base_url):
    print("업로드된 파일을 처리 - process_uploaded_files")
    
    folder_path = "../compileFiles"
    
    if not os.path.exists(folder_path):
        try:
            os.makedirs(folder_path)
            # 권한 설정 (필요한 경우)
            os.chmod(folder_path, 0o755)
        except Exception as e:
            print(f"디렉토리 생성 중 오류: {str(e)}")
            return []
        
    DeleteAllFiles(folder_path)
    
    file_results = []
    for file_info in files:
        try:
            print(f"처리할 파일 정보: {file_info}")
            file_path = file_info.get("filePath", "")
            file_name = file_info.get("fileName", "")
            
            if not file_path or not file_name:
                print("파일 경로 또는 이름이 없습니다.")
                continue

            # file_path가 리스트인 경우 문자열로 변환
            if isinstance(file_path, list):
                file_path = "/".join(file_path)
            
            file_path = os.path.normpath(file_path)
            # 경로 정규화
            if file_path.startswith('//'):
                file_path = file_path[1:]

            # 절대 경로를 상대 경로로 변경
            file_path = file_path.replace('/home/files/uploads', '/uploads', 1)

            # 파일명에서 UUID 생성
            file_extension = os.path.splitext(file_name)[1]
            uuid_filename = f"{str(uuid.uuid4())}{file_extension}"
            # 다운로드 URL 및 로컬 경로 설정
            download_url = f"{base_url}{file_path}/{file_name}"
            local_file_path = os.path.join(folder_path, uuid_filename)
            
            print(f"다운로드 URL: {download_url}")
            print(f"로컬 저장 경로: {local_file_path}")



            # 파일 다운로드
            response = requests.get(download_url, stream=True)
            response.raise_for_status()
            
            with open(local_file_path, "wb") as file:
                for chunk in response.iter_content(chunk_size=8192):
                    if chunk:
                        file.write(chunk)

            # 파일 내용 추출
            extracted_text = process_file(local_file_path)
            if extracted_text:
                file_results.append({"text": extracted_text})
            else:
                file_results.append({"error": "텍스트를 추출할 수 없습니다."})

        except Exception as e:
            print(f"파일 처리 중 오류 발생: {str(e)}")
            file_results.append({"error": str(e)})

    return file_results

def process_file(file_path):
    if not os.path.exists(file_path):
        print(f"파일이 존재하지 않습니다: {file_path}")
        return None

    file_extension = os.path.splitext(file_path)[1].lower()
    print(f"파일 확장자: {file_extension}")

    try:
        if file_extension in [".jpg", ".jpeg", ".png",".webp",".jfif",".gif"]:
            print("이미지 파일 처리")
            image = Image.open(file_path)
            return pytesseract.image_to_string(image, lang='eng')

        elif file_extension == ".pdf":
            print("PDF 파일 처리")
            return process_pdf_with_ocr(file_path)

        elif file_extension == ".docx":
            print("Word 파일 처리")
            doc = docx.Document(file_path)
            return "\n".join(paragraph.text for paragraph in doc.paragraphs)

        elif file_extension == ".txt":
            print("텍스트 파일 처리")
            with open(file_path, "r", encoding="utf-8") as file:
                return file.read()

        else:
            print(f"지원되지 않는 파일 형식: {file_extension}")
            return None

    except Exception as e:
        print(f"파일 처리 중 오류 발생: {str(e)}")
        return None

def process_pdf_with_ocr(pdf_path):
    print(f"PDF 처리 시작: {pdf_path}")
    pdf_text = []
    
    try:
        with fitz.open(pdf_path) as pdf:
            for page_num, page in enumerate(pdf):
                print(f"페이지 {page_num + 1} 처리 중")
                
                # 텍스트 추출 시도
                text = page.get_text()
                if text.strip():
                    pdf_text.append(text)
                    continue

                # 텍스트가 없는 경우 이미지 처리
                images = page.get_images(full=True)
                for img_index, img in enumerate(images):
                    try:
                        xref = img[0]
                        base_image = pdf.extract_image(xref)
                        image_bytes = base_image["image"]
                        
                        image = Image.open(io.BytesIO(image_bytes))
                        ocr_text = pytesseract.image_to_string(image, lang="eng")
                        if ocr_text.strip():
                            pdf_text.append(ocr_text)
                    except Exception as e:
                        print(f"이미지 {img_index} 처리 중 오류: {str(e)}")
                        continue

        return "\n".join(pdf_text)
    except Exception as e:
        print(f"PDF 처리 중 오류 발생: {str(e)}")
        return None

def send_text_to_ai(line):
    print("AI API 호출")
    try:
        response = model.generate_content(line)
        return {"status": "success", "response": response.to_dict()}
    except Exception as e:
        print(f"AI 처리 중 오류: {str(e)}")
        return {"error": f"AI 처리 중 오류 발생: {str(e)}"}

def extract_text_from_url(url):
    print(f"URL에서 텍스트 추출: {url}")
    try:
        response = requests.get(url)
        response.raise_for_status()
        
        soup = BeautifulSoup(response.text, 'lxml')
        
        # 불필요한 태그 제거
        for tag in soup(['script', 'style', 'meta', 'link']):
            tag.decompose()
            
        page_text = soup.body.get_text(separator="\n", strip=True)
        return page_text
        
    except Exception as e:
        print(f"URL 처리 중 오류: {str(e)}")
        return f"처리 중 오류 발생: {str(e)}"

def DeleteAllFiles(folder_path):
    try:
        if os.path.exists(folder_path):
            for file in os.scandir(folder_path):
                os.remove(file.path)
            print(f"{folder_path} 내 모든 파일 삭제 완료")
        else:
            print(f"폴더가 존재하지 않습니다: {folder_path}")
    except Exception as e:
        print(f"파일 삭제 중 오류 발생: {str(e)}")

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)