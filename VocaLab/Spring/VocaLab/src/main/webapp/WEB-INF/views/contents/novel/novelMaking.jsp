<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <title>나만의 소설만들기</title>
    <style>
        .story-container {
            white-space: pre-line;
            line-height: 1.8;
            margin: 20px 0;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 5px;
        }
        .loading {
            text-align: center;
            padding: 20px;
        }
        .word-list {
            margin-top: 20px;
            padding: 10px;
            background-color: #f8f9fa;
            border-radius: 5px;
        }
    </style>
</head>
<body><%-- top배너 삽입 --%>
<div id="banner_top" role="banner">
    <%@ include file="../../banners/top_left.jsp" %>
</div>
<div class="container mt-5">
    <h1 class="text-center mb-4">나만의 단어로 만드는 이야기</h1>

    <div class="text-center mb-4">
        <button id="generateStory" class="btn btn-primary">이야기 생성하기</button>
        <button id="copyStory" class="btn btn-secondary ms-2" style="display: none;">이야기 복사하기</button>
    </div>

    <div id="loading" class="loading" style="display: none;">
        <div class="spinner-border text-primary" role="status">
            <span class="visually-hidden">Loading...</span>
        </div>
        <p class="mt-2">이야기를 생성하고 있습니다...</p>
    </div>

    <div id="storyContainer" class="story-container" style="display: none;"></div>

    <div id="wordList" class="word-list">
        <h4>선택된 단어 목록:</h4>
        <ul class="list-group" id="selectedWordsList"></ul>
    </div>
</div>
<div class="banner_bottom">
    <%@ include file="../../banners/bottom.jsp" %>
</div>
<script>
    $(document).ready(function() {
        // 세션 스토리지에서 선택된 단어 데이터 가져오기
        const selectedWords = JSON.parse(sessionStorage.getItem('selectedWords') || '[]');

        // 선택된 단어 목록 표시
        const $wordsList = $('#selectedWordsList');
        selectedWords.forEach(function(word) {
            $wordsList.append(
                '<li class="list-group-item">' +
                word.word + ' - ' + word.meaning +
                '</li>'
            );
        });

        // 이야기 생성 버튼 클릭 이벤트
        $('#generateStory').click(function() {
            if (selectedWords.length === 0) {
                alert('선택된 단어가 없습니다.');
                return;
            }

            $('#loading').show();
            $('#storyContainer').hide();
            $('#copyStory').hide();

            $.ajax({
                url: '/contents/novel/generate-novel',
                method: 'POST',
                contentType: 'application/json',
                data: JSON.stringify({ words: selectedWords }),
                success: function(response) {
                    $('#storyContainer').html(response.story).show();
                    $('#copyStory').show();
                    $('#loading').hide();
                },
                error: function(xhr, status, error) {
                    alert('이야기 생성에 실패했습니다.');
                    console.error('Error:', error);
                    $('#loading').hide();
                }
            });
        });

        // 이야기 복사 버튼 클릭 이벤트
        $('#copyStory').click(function() {
            const storyText = $('#storyContainer').text();
            navigator.clipboard.writeText(storyText).then(function() {
                alert('이야기가 클립보드에 복사되었습니다.');
            }).catch(function(err) {
                console.error('클립보드 복사 실패:', err);
                alert('클립보드 복사에 실패했습니다.');
            });
        });
    });
</script>
</body>
</html>
