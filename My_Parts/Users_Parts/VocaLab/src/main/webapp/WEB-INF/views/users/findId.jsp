<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!doctype html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <!-- CSRF 토큰을 위한 메타 태그 추가 -->
<%--    <meta name="_csrf" content="${_csrf.token}" />--%>
<%--    <meta name="_csrf_header" content="${_csrf.headerName}" />--%>
    <title>아이디 찾기</title>
    <!-- 스타일 코드는 동일하므로 생략 -->
    <style>
        /* 기존 스타일 코드 유지 */
    </style>
    <script>
        // console.log('CSRF 메타 태그 확인:');
        // console.log('Token:', document.querySelector("meta[name='_csrf']"));
        // console.log('Header:', document.querySelector("meta[name='_csrf_header']"));
    </script>
</head>
    <body>
        <div class="find-id-wrap">
            <header class="find-id-header">
                <a href="/" class="logo-link">VocaLab</a>
            </header>

            <div class="find-id-form-wrap">
                <h2 class="form-title">아이디 찾기</h2>

                <!-- 이메일 입력 폼 -->
                <div id="emailForm">
                    <div class="input-group">
                        <input type="email" id="email" class="input-field" placeholder="이메일을 입력하세요" required>
                    </div>
                    <button type="button" id="sendVerificationBtn" class="submit-btn">인증번호 받기</button>
                    <p id="emailError" class="error-message" style="display: none;"></p>
                </div>

                <!-- 인증번호 입력 폼 -->
                <div id="verificationForm" style="display: none;">
                    <div class="input-group verification-group">
                        <input type="text" id="verificationCode" class="input-field verification-input"
                               placeholder="인증번호 6자리 입력" maxlength="6" required>
                        <span id="timer" class="verification-timer"></span>
                    </div>
                    <div class="button-group" style="display: flex; gap: 10px;">
                        <button type="button" id="verifyCodeBtn" class="submit-btn">확인</button>
                        <button type="button" id="resendBtn" class="submit-btn">인증번호 재발송</button>
                    </div>
                    <p id="verificationError" class="error-message" style="display: none;"></p>
                </div>

                <!-- 결과 표시 영역 -->
                <div id="resultArea" class="result-wrap" style="display: none;">
                    <p class="result-message">고객님의 아이디는</p>
                    <p class="found-id" id="foundUserId"></p>
                    <p class="result-message">입니다.</p>
                    <div class="button-group">
                        <a href="/login" class="btn login-btn">로그인하기</a>
                        <a href="/findPassWord" class="btn find-pw-btn">비밀번호 찾기</a>
                    </div>
                </div>
            </div>
        </div>

        <script>
            document.addEventListener('DOMContentLoaded', function() {
                let verificationId = '';
                let timerInterval;
                let timerExpired = false;

                function startTimer(duration) {
                    let timer = duration;
                    const timerDisplay = document.getElementById('timer');

                    clearInterval(timerInterval);
                    timerInterval = setInterval(function() {
                        const minutes = parseInt(timer / 60, 10);
                        const seconds = parseInt(timer % 60, 10);

                        timerDisplay.textContent = minutes + ':' + (seconds < 10 ? '0' : '') + seconds;

                        if (--timer < 0 && !timerExpired) {
                            timerExpired = true; // 타이머 만료 상태를 true로 설정
                            clearInterval(timerInterval); // 타이머 중지
                            timerDisplay.textContent = '시간 만료';
                            document.getElementById('verifyCodeBtn').disabled = true;
                            alert('인증 시간이 만료되었습니다. 다시 시도해 주세요.');
                            window.location.href = '/login'; // 로그인 페이지로 이동
                        }
                    }, 1000);
                }

                document.getElementById('sendVerificationBtn').addEventListener('click', function() {
                    const email = document.getElementById('email').value;
                    const emailError = document.getElementById('emailError');

                    if (!email) {
                        emailError.textContent = '이메일을 입력해주세요.';
                        emailError.style.display = 'block';
                        return;
                    }

                    fetch('/api/findId/send-verification', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({ email: email })
                    })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                verificationId = data.verificationId;
                                document.getElementById('emailForm').style.display = 'none';
                                document.getElementById('verificationForm').style.display = 'block';
                                startTimer(300); // 5분 타이머 시작
                                alert('인증번호가 발송되었습니다.');
                            } else {
                                emailError.textContent = data.message || '등록되지 않은 이메일입니다.';
                                emailError.style.display = 'block';
                            }
                        })
                        .catch(error => {
                            console.error('Error:', error);
                            emailError.textContent = '서버 오류가 발생했습니다.';
                            emailError.style.display = 'block';
                        });
                });

                // 인증번호 재발송 버튼 클릭
                document.getElementById('resendBtn').addEventListener('click', function() {
                    const email = document.getElementById('email').value;
                    const verificationError = document.getElementById('verificationError');

                    fetch('/api/findId/send-verification', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            email: email
                        })
                    })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                verificationId = data.verificationId; // 새로운 인증 ID 저장
                                startTimer(300); // 타이머 재시작
                                document.getElementById('verifyCodeBtn').disabled = false; // 확인 버튼 활성화
                                alert('인증번호가 재발송되었습니다.');
                                verificationError.style.display = 'none';
                            } else {
                                verificationError.textContent = data.message;
                                verificationError.style.display = 'block';
                            }
                        })
                        .catch(error => {
                            verificationError.textContent = '서버 오류가 발생했습니다.';
                            verificationError.style.display = 'block';
                        });
                });

                document.getElementById('verifyCodeBtn').addEventListener('click', function() {
                    const code = document.getElementById('verificationCode').value;
                    const verificationError = document.getElementById('verificationError');
                    const email = document.getElementById('email').value;

                    if (!code) {
                        verificationError.textContent = '인증번호를 입력해주세요.';
                        verificationError.style.display = 'block';
                        return;
                    }

                    fetch('/api/findId/verify', {
                        method: 'POST',
                        headers: {
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            verificationId: verificationId,
                            code: code,
                            email: email
                        })
                    })
                        .then(response => response.json())
                        .then(data => {
                            if (data.success) {
                                clearInterval(timerInterval);
                                document.getElementById('verificationForm').style.display = 'none';
                                document.getElementById('resultArea').style.display = 'block';
                                document.getElementById('foundUserId').textContent = data.userId;
                            } else {
                                verificationError.textContent = data.message || '잘못된 인증번호입니다.';
                                verificationError.style.display = 'block';
                            }
                        })
                        .catch(error => {
                            verificationError.textContent = '서버 오류가 발생했습니다.';
                            verificationError.style.display = 'block';
                        });
                });
            });
        </script>
    </body>
</html>