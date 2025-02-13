<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>회원가입 - VocaLab</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/registration.css" rel="stylesheet">
</head>
<body>
<div class="register-wrap">
    <div class="register-header">
        <a href="/" class="logo-link">
            <img src="/images/logoEx1.png" alt="VocaLab" height="100">
        </a>
    </div>

    <div class="register-content">
        <div class="register-form-wrap">
            <h2 class="register-title">회원가입</h2>
            <form action="/register" method="post">
                <div class="input-group">
                    <label for="userId">아이디</label>
                    <input type="text" id="userId" name="userId" class="input-field" placeholder="아이디를 입력하세요" autocomplete="off" required>
                </div>

                <div class="input-group">
                    <label for="userPassword">비밀번호</label>
                    <input type="password" id="userPassword" name="userPassword" class="input-field" placeholder="비밀번호를 입력하세요" autocomplete="off" required>
                </div>

                <div class="input-group">
                    <label for="userName">이름</label>
                    <input type="text" id="userName" name="userName" class="input-field" placeholder="이름을 입력하세요" autocomplete="off" required>
                </div>

                <div class="input-group">
                    <label for="userNickname">닉네임</label>
                    <input type="text" id="userNickname" name="userNickname" class="input-field" placeholder="닉네임을 입력하세요" autocomplete="off" required>
                </div>

                <div class="input-group">
                    <label for="userEmail">이메일</label>
                    <input type="email" id="userEmail" name="userEmail" class="input-field" placeholder="이메일을 입력하세요" autocomplete="off" required>
                </div>

                <div class="birth-gender-group">
                    <label>생년월일</label>
                    <div class="birth-inputs">
                        <input type="text" id="year" name="year" class="birth-field" placeholder="YYYY" maxlength="4" pattern="[0-9]{4}" required>
                        <input type="text" id="month" name="month" class="birth-field" placeholder="MM" maxlength="2" pattern="[0-9]{1,2}" required>
                        <input type="text" id="day" name="day" class="birth-field" placeholder="DD" maxlength="2" pattern="[0-9]{1,2}" required>
                    </div>
                </div>

                <div class="gender-group">
                    <label>성별</label>
                    <div class="gender-options">
                        <label class="gender-label">
                            <input type="radio" name="gender" value="1" required>
                            <span>남성</span>
                        </label>
                        <label class="gender-label">
                            <input type="radio" name="gender" value="2" required>
                            <span>여성</span>
                        </label>
                    </div>
                </div>

                <button type="submit" class="register-btn">가입하기</button>
            </form>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>