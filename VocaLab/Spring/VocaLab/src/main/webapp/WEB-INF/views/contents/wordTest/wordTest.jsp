<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>단어 테스트</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        body {
            background-color: #f5f7fa;
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
        }

        .container {
            max-width: 800px;
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

        .question-card {
            margin-bottom: 25px;
            border: none;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            transition: transform 0.2s ease;
        }

        .question-card:hover {
            transform: translateY(-3px);
        }

        .card-title {
            color: #2c3e50;
            font-weight: 600;
            border-bottom: 2px solid #e9ecef;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }

        .card-text {
            font-size: 1.4rem;
            color: #1894ec;
            font-weight: 500;
            text-align: center;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 10px;
            margin-bottom: 20px;
        }

        .choices {
            margin-top: 20px;
            font-size: 1.0rem;
        }

        .form-check {
            margin-bottom: 12px;
            padding: 2px 2px; /* 좌우 패딩 최소화 */
            border-radius: 8px;
            transition: all 0.2s ease;
            cursor: pointer;
            display: flex;
            align-items: center;
            min-height: 44px;
        }

        .form-check:hover {
            background-color: #f8f9fa;
        }

        .form-check-input {
            cursor: pointer;
            margin-right: 10px;
            flex-shrink: 0;
        }

        .form-check-label {
            cursor: pointer;
            width: 700px; /* 너비 700px로 설정 */
            height: 40px; /* 높이 40px로 설정 */
            padding-left: 5px;
            color: #495057;
            display: flex; /* flexbox 사용 */
            align-items: center; /* 세로 중앙 정렬 */
            line-height: 40px; /* 텍스트 세로 중앙 정렬 */
            box-sizing: border-box; /* 패딩을 너비에 포함 */
            margin: 0; /* 마진 제거 */
        }

        .form-check-input.submitted,
        .form-check-label.submitted {
            cursor: not-allowed !important;
            opacity: 0.7;
        }

        .progress {
            height: 20px;
            border-radius: 5px;
            background-color: #e9ecef;
            margin: 20px 0;
        }

        .progress-bar {
            transition: width 0.6s ease;
            background: linear-gradient(45deg, #3498db, #2ecc71);
            border-radius: 5px;
        }

        .submit-section {
            margin: 40px 0;
            text-align: center;
        }

        .btn-primary {
            background: linear-gradient(45deg, #3498db, #2ecc71);
            border: none;
            padding: 12px 30px;
            border-radius: 25px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(52,152,219,0.3);
        }

        .score-summary {
            background: linear-gradient(45deg, #fff, #f8f9fa);
            padding: 25px;
            border-radius: 15px;
            box-shadow: 0 5px 15px rgba(0,0,0,0.05);
            margin-bottom: 30px;
            text-align: center;
        }

        .score-summary h4 {
            color: #2c3e50;
            margin-bottom: 15px;
        }

        .result-item {
            padding: 15px;
            margin-bottom: 10px;
            border-radius: 10px;
            background-color: white;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
        }

        .question-number {
            font-weight: 500;
            color: #2c3e50;
        }

        .result-feedback {
            margin-top: 15px;
            display: none;
        }

        .alert {
            border-radius: 10px;
            border: none;
            box-shadow: 0 2px 5px rgba(0,0,0,0.05);
        }

        .alert-success {
            background-color: #d4edda;
            color: #155724;
        }

        .alert-danger {
            background-color: #f8d7da;
            color: #721c24;
        }

        .alert-warning {
            background-color: #fff3cd;
            color: #856404;
        }

        @keyframes fadeIn {
            from { opacity: 0; transform: translateY(10px); }
            to { opacity: 1; transform: translateY(0); }
        }

        #resultSection {
            animation: fadeIn 0.5s ease-out;
        }
    </style>
</head>
<body>
<%-- top배너 삽입 --%>
<div id="banner_top" role="banner">
    <%@ include file="../../banners/top_left.jsp" %>
</div>
<div class="container mt-4">
    <div id="testContainer">
        <div id="testHeader" class="text-center page-title">
            <h2>단어 테스트</h2>
        </div>
        <div class="progress mb-3">
            <div class="progress-bar" role="progressbar" style="width: 0%"></div>
        </div>
        <div id="testContent"></div>
        <div class="submit-section">
            <button id="submitTest" class="btn btn-primary">제출하기</button>
        </div>
        <div id="resultSection" class="mt-4" style="display: none;">
            <h3 class="text-center">테스트 결과</h3>
            <div id="scoreDisplay" class="text-center mb-4"></div>
            <div class="results-detail mt-4" id="resultDetails"></div>
        </div>
    </div>
</div>
<div class="banner_bottom">
    <%@ include file="../../banners/bottom.jsp" %>
</div>
<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script>
    $(document).ready(function() {
        // 세션 스토리지에서 테스트 데이터와 타입 가져오기
        const testData = JSON.parse(sessionStorage.getItem('testData'));
        const testType = sessionStorage.getItem('testType');

        // 제출 상태를 추적하는 변수
        let isSubmitted = false;

        console.log('Raw testData:', sessionStorage.getItem('testData'));
        console.log('Session Storage - Test Data:', testData);
        console.log('Session Storage - Test Type:', testType);

        if (!testData || !testData.questions || !Array.isArray(testData.questions)) {
            console.error('Invalid test data structure:', testData);
            alert('테스트 데이터가 없습니다.');
            window.location.href = '/contents/wordTest';
            return;
        }

        // 문제 데이터 가져오기
        const questions = testData.questions;
        console.log('Questions loaded:', questions);
        const testTypeTitle = testType === 'meaning' ? '영어 → 한글' : '한글 → 영어';
        console.log('Test Type Title:', testTypeTitle);

        $('#testHeader h2').text(testTypeTitle + ' 테스트');

        // 문제 렌더링
        questions.forEach((question, index) => {
            const questionHtml = createQuestionHTML(question, index);
            console.log('Rendering question ' + (index + 1) + ':', question);
            $('#testContent').append(questionHtml);
        });

        // 클릭 on/off 가능, 반응이 조금 느림
        $(document).on('click', '.form-check-input, .form-check-label', function(e) {
            // 제출된 상태면 클릭 이벤트 무시
            if (isSubmitted) {
                e.preventDefault();
                return;
            }

            let $radio;
            if ($(this).is('label')) {
                // 1. 먼저 기본 동작을 막음 (브라우저의 기본 동작(라디오 버튼 자동 클릭) 차단)
                // label 클릭시에만 preventDefault
                // 이것을 안하면
                // 1. label 클릭 이벤트 발생
                // 2. 우리의 코드 실행 (체크 해제 시도)
                // 3. 브라우저의 기본 동작으로 인해 연결된 라디오 버튼이 자동으로 클릭됨
                // 4. 결과적으로 항상 체크 상태가 됨
                e.preventDefault();

                // 연결된 라디오 버튼 찾기
                $radio = $('#' + $(this).attr('for'));

                // 2. 이후 모든 동작을 우리가 직접 제어
                // 이미 선택된 라디오 버튼을 다시 클릭한 경우
                if ($radio.data('waschecked') === true) {
                    $radio.prop('checked', false);
                    $radio.data('waschecked', false);
                } else {
                    // 같은 name을 가진 다른 라디오 버튼들의 waschecked 값을 false로 설정
                    $('input[name="' + $radio.attr('name') + '"]').prop('checked', false).data('waschecked', false);
                    // 현재 클릭한 라디오 버튼 선택
                    $radio.prop('checked', true);
                    $radio.data('waschecked', true);
                }
            }

            // input 직접 클릭시(라디오버튼)
            else {
                $radio = $(this); // 클릭된 라디오 버튼 저장

                // 브라우저의 기본 동작 실행 (자동으로 체크 상태 변경)

                // 브라우저의 체크 상태 변경이 완료된 후 우리가 원하는 작업 실행
                setTimeout(() => {
                    // 이전에 체크된 상태였다면 체크 해제
                    if ($radio.data('waschecked') === true) {
                        $radio.prop('checked', false);
                        $radio.data('waschecked', false);
                    } else {
                        // 다른 라디오 버튼들 초기화
                        $('input[name="' + $radio.attr('name') + '"]').not($radio).data('waschecked', false);
                        $radio.data('waschecked', true);
                    }
                }, 0);
            }

            // 진행률 바 업데이트 (약간의 지연을 주어 상태 변경이 반영되도록 함)
            setTimeout(() => {
                const total = questions.length;
                const answered = $('input[type="radio"]:checked').length;
                const percentage = (answered / total * 100).toFixed(1);

                $('.progress-bar')
                    .css('width', percentage + '%')
                    .text(percentage + '%');
            }, 10);
        });
        // 클릭 on/off 불가, 대신 반응이 빠름
        // $(document).on('change', 'input[type="radio"]', function() {
        //     const total = questions.length;
        //     const answered = $('input[type="radio"]:checked').length;
        //     const percentage = (answered / total * 100).toFixed(1);
        //
        //     // 문제 진행률바
        //     $('.progress-bar')
        //         .css('width', percentage + '%')
        //         .text(percentage + '%');
        // });

        // 제출 버튼 클릭 이벤트
        $('#submitTest').click(function() {
            console.log('Submit button clicked');

            // 모든 문제 풀었는지 확인
            let unansweredQuestions = [];
            let allAnswered = true;

            questions.forEach((question, index) => {
                const selectedRadio = $('input[name="question' + index + '"]:checked');
                const questionCard = $('#question' + index);
                const feedback = questionCard.find('.result-feedback');


                console.log('Question ' + (index + 1) + ':', {
                    question: question,
                    selectedRadio: selectedRadio.length > 0 ? selectedRadio.val() : 'none',
                    correct_answer: question.correct_answer
                });

                if (!selectedRadio.length) {
                    unansweredQuestions.push(index + 1);
                    allAnswered = false;
                }
            });

            console.log('Unanswered Questions:', unansweredQuestions);
            console.log('All Answered:', allAnswered);

            // 풀지 않은 문제가 있는 경우
            if (!allAnswered) {
                console.log('Showing confirm dialog for unanswered questions:', unansweredQuestions.join(', '));
                const unansweredText = unansweredQuestions.map(num => num + "번").join(', ');
                const confirmSubmit = confirm(
                    "문제 " + unansweredText + "을 풀지 않았습니다.\n정말 제출하시겠습니까?"
                );
                if (!confirmSubmit) {
                    return;
                }
            }

            // 제출 상태 설정
            isSubmitted = true;
            $('.form-check-input, .form-check-label').addClass('submitted');

            // 결과 처리 부분
            let score = 0;
            let total = questions.length;
            let resultDetailsHtml = '';

            questions.forEach((question, index) => {
                const selectedRadio = $('input[name="question' + index + '"]:checked');
                const questionCard = $(`#question` + index);
                const feedback = questionCard.find('.result-feedback');

                // 디버깅을 위한 로그 추가
                console.log('Processing Question ' + (index + 1) + ':', {
                    question: question,
                    selectedRadio: selectedRadio.length ? selectedRadio.val() : 'none',
                    correct_answer: question.correct_answer
                });

                // 답을 선택하지 않은 경우
                if (!selectedRadio.length) {
                    feedback.html('<div class="alert alert-warning">답을 선택하지 않았습니다.</div>');
                    feedback.show();

                    resultDetailsHtml += createResultItemHtml({
                        questionNum: index + 1,
                        answered: false,
                        correct: false,
                        selectedAnswer: '미응답',
                        correctAnswer: question.choices[question.correct_answer]
                    });
                } else {
                    // 답을 선택한 경우
                    const selectedAnswer = parseInt(selectedRadio.val());

                    // 디버깅을 위한 로그 추가
                    console.log('Comparing answers for question ' + (index + 1) + ':', {
                        selectedAnswer: selectedAnswer,
                        correct_answer: question.correct_answer,
                        isEqual: selectedAnswer === question.correct_answer
                    });

                    const isCorrect = selectedAnswer === question.correct_answer;

                    if (isCorrect) {
                        score++;
                        feedback.html('<div class="alert alert-success">' +
                            '<strong>정답입니다!</strong><br>' +
                            question.word + ' = ' + question.choices[question.correct_answer] +
                            '</div>');
                    } else {
                        feedback.html('<div class="alert alert-danger">' +
                            '<strong>오답입니다.</strong><br>' +
                            '선택한 답: ' + question.choices[selectedAnswer] + '<br>' +
                            '정답: ' + question.choices[question.correct_answer] +
                            '</div>');
                    }

                    resultDetailsHtml += createResultItemHtml({
                        questionNum: index + 1,
                        answered: true,
                        correct: isCorrect,
                        selectedAnswer: question.choices[selectedAnswer],
                        correctAnswer: question.choices[question.correct_answer]
                    });
                }

                feedback.show();
                questionCard.find('input[type="radio"]').prop('disabled', true);
            });


            // 결과 표시
            const percentage = (score / total * 100).toFixed(1);

            $('#scoreDisplay').html(
                '<div class="score-summary">' +
                '<h4>총점: ' + score + '/' + total + ' (' + percentage + '%)</h4>' +
                '<p>정답 ' + score + '개 / 오답 ' + (total - score) + '개</p>' +
                '</div>'
            );

            $('#resultDetails').html(resultDetailsHtml);
            $('#resultSection').show();
            $('#submitTest').prop('disabled', true);

            // 정답률 바 업데이트
            $('.progress-bar')
                .css('width', percentage + '%')
                .text(percentage + '%')
                .addClass(percentage >= 60 ? 'bg-success' : 'bg-danger');
        });
    });

    // 문제 HTML 생성 함수
    function createQuestionHTML(question, index) {
        const questionNumber = index + 1;

        console.log('Creating question HTML:', {
            questionNumber: questionNumber,
            question: question,
            word: question.word,
            choices: question.choices,
            correct_answer: question.correct_answer
        });

        console.log('Creating question number:', questionNumber);

        const testType = sessionStorage.getItem('testType');
        let questionWord = question.word;
        let choices = question.choices;

        let html = '<div id="question' + index + '" class="card question-card">' +
            '<div class="card-body">' +
            '<h5 class="card-title">문제 ' + questionNumber + '</h5>' +
            '<p class="card-text">' + questionWord + '</p>' +
            '<div class="choices">';

        choices.forEach((choice, i) => {
            html += '<div class="form-check">' +
                '<input class="form-check-input" type="radio" ' +
                'name="question' + index + '" ' +
                'value="' + i + '" ' +
                'id="choice' + index + '_' + i + '">' +
                '<label class="form-check-label" for="choice' + index + '_' + i + '">' +
                choice +
                '</label>' +
                '</div>';
        });

        html += '</div>' +
            '<div class="result-feedback"></div>' +
            '</div>' +
            '</div>';

        return html;
    }

    // 결과 아이템 HTML 생성 함수
    function createResultItemHtml(result) {
        const statusClass = !result.answered ? 'text-warning' :
            (result.correct ? 'text-success' : 'text-danger');
        const statusText = !result.answered ? '미응답' :
            (result.correct ? '정답' : '오답');

        let html = '<div class="result-item mb-2">' +
            '<span class="question-number">문제 ' + result.questionNum + '번:</span>' +
            '<span class="' + statusClass + '"> ' + statusText + '</span>';

        if (result.answered && !result.correct) {
            html += '<br><small class="text-muted">' +
                '선택한 답: ' + result.selectedAnswer + ' / ' +
                '정답: ' + result.correctAnswer +
                '</small>';
        }

        html += '</div>';
        return html;
    }
</script>
</body>
</html>
