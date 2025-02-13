<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <title>단어 관련 뉴스 추천</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f8f9fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .container {
            max-width: 1200px;
            padding: 2rem;
        }

        .page-title {
            color: #2c3e50;
            font-weight: 600;
            margin-bottom: 2rem;
            position: relative;
            padding-bottom: 1rem;
        }

        .page-title:after {
            content: '';
            position: absolute;
            bottom: 0;
            left: 50%;
            transform: translateX(-50%);
            width: 100px;
            height: 3px;
            background: linear-gradient(90deg, #007bff, #00d2ff);
            border-radius: 2px;
        }

        .news-card {
            margin-bottom: 25px;
            transition: all 0.3s ease;
            border: none;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            background: white;
            overflow: hidden;
        }

        .news-card:hover {
            transform: translateY(-10px);
            box-shadow: 0 15px 30px rgba(0,0,0,0.1);
        }

        .card-body {
            padding: 1.5rem;
        }

        .card-title {
            color: #2c3e50;
            font-size: 1.2rem;
            font-weight: 600;
            margin-bottom: 1rem;
            line-height: 1.4;
        }

        .description {
            color: #666;
            font-size: 0.95rem;
            line-height: 1.6;
            margin-bottom: 1.5rem;
        }

        .btn-primary {
            background: linear-gradient(45deg, #007bff, #00d2ff);
            border: none;
            padding: 0.6rem 1.5rem;
            border-radius: 25px;
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(0,123,255,0.4);
        }

        #searchContext {
            background: linear-gradient(45deg, rgba(0,123,255,0.1), rgba(0,210,255,0.1));
            border: none;
            border-radius: 15px;
            padding: 1rem 1.5rem;
            color: #2c3e50;
            font-size: 1.1rem;
        }

        #loadingSpinner {
            margin: 2rem 0;
        }

        .spinner-border {
            width: 3rem;
            height: 3rem;
            color: #007bff;
        }

        #errorMessage {
            border-radius: 15px;
            padding: 1rem 1.5rem;
        }

        @media (max-width: 768px) {
            .container {
                padding: 1rem;
            }

            .news-card {
                margin-bottom: 15px;
            }
        }

        /* 애니메이션 효과 */
        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .news-card {
            animation: fadeIn 0.5s ease-out forwards;
        }

        .original-text, .korean-summary {
            margin-bottom: 1rem;
        }

        .korean-summary .summary-text {
            font-size: 0.9rem;
            color: #2c3e50;
            background-color: #f8f9fa;
            padding: 0.8rem;
            border-radius: 8px;
            border-left: 3px solid #007bff;
        }

        .text-muted {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 500;
        }

        .description {
            font-size: 0.9rem;
            color: #666;
            line-height: 1.6;
        }
    </style>
</head>
<body>
<div class="container mt-5">
    <h2 class="text-center page-title">단어 관련 뉴스 추천</h2>

    <div id="loadingSpinner" class="text-center">
        <div class="spinner-border" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
    </div>

    <div id="searchContext" class="mb-4" style="display: none;">
        <i class="fas fa-search me-2"></i>
        <strong>검색 단어:</strong> <span id="contextText"></span>
    </div>

    <div id="newsResults" class="row">
    </div>

    <div id="errorMessage" class="alert alert-danger" style="display: none;">
        <i class="fas fa-exclamation-circle me-2"></i>
        <span class="error-text"></span>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        var selectedWordsData = sessionStorage.getItem('selectedWords');
        console.log('Data imported from session storage(세션 스토리지에서 불러온 데이터): ', selectedWordsData);

        if (!selectedWordsData) {
            $('#errorMessage').text('선택된 단어가 없습니다.').show();
            return;
        }

        var selectedWords = JSON.parse(selectedWordsData);
        console.log('parsed word data(파싱된 단어 데이터): ', selectedWords);
        console.log('Selected Words(선택된 단어): ', selectedWords[0].word);

        if (selectedWords.length === 0) {
            $('#errorMessage').text('선택된 단어가 없습니다.').show();
            return;
        }

        var word = selectedWords[0].word;
        $('#contextText').text(word);
        $('#searchContext').show();

        $('#loadingSpinner').show();
        $('#newsResults').empty();
        $('#errorMessage').hide();

        $.ajax({
            url: '/contents/news/generate-news',
            method: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({ word: word }),
            success: function(response) {
                $('#loadingSpinner').hide();

                // 응답 데이터 로깅 추가
                console.log('API Response:', response);
                console.log('News Array:', response.news);

                if (!response.news || !Array.isArray(response.news)) {
                    $('#errorMessage').text('뉴스 데이터 형식이 올바르지 않습니다.').show();
                    return;
                }

                response.news.forEach(function(article, index) {

                    console.log('Article ' + (index + 1) + ' Full Data:', article);

                    let koreanSummary = article.korean_summary || '요약을 불러올 수 없습니다.';

                    console.log('Article ' + (index + 1) + ':', {
                        title: article.title,
                        hasDescription: !!article.description,
                        hasSummary: !!koreanSummary,
                        summaryLength: koreanSummary ? koreanSummary.length : 0
                    });


                    console.log('Final Korean Summary for Article ' + (index + 1) + ':', koreanSummary);

                    var newsCard = '<div class="col-md-4">' +
                        '<div class="card news-card">' +
                        '<div class="card-body">' +
                        '<h5 class="card-title">' + (article.title || '제목 없음') + '</h5>' +
                        '<div class="card-text">' +
                        '<div class="original-text mb-3">' +
                        '<small class="text-muted">원문</small>' +
                        '<p class="description">' + (article.description || '내용 없음') + '</p>' +
                        '</div>' +
                        '<div class="korean-summary mb-3">' +
                        '<small class="text-muted">한글 요약</small>' +
                        '<p class="summary-text p-2 bg-light rounded">' +
                        koreanSummary +
                        '</p>' +
                        '</div>' +
                        '</div>' +
                        '<a href="' + article.url + '" class="btn btn-primary" target="_blank">' +
                        '자세히 보기' +
                        '</a>' +
                        '</div>' +
                        '</div>' +
                        '</div>';
                    $('#newsResults').append(newsCard);
                });
            },
            error: function(xhr, status, error) {
                $('#loadingSpinner').hide();
                console.error('API Error:', error);
                console.error('Error Status:', status);
                console.error('Error Response:', xhr.responseText);
                console.error('Response:', xhr.responseText);
                var errorMessage = xhr.responseJSON ? xhr.responseJSON.error : '뉴스를 가져오는 중 오류가 발생했습니다.';
                $('#errorMessage').text(errorMessage).show();
            }
        });
    });
</script>
</body>
</html>