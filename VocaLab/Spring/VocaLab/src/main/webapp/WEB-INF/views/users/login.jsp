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
    <title>Login</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="/css/login.css" rel="stylesheet">
</head>
<body>
<div class="nav">
<div class="login-wrap">
    <div class="login-header">
        <a href="/" class="logo-link">
            <img src="/images/logoEx1.png" alt="VocaLab" height="100">
        </a>
    </div>

    <div class="login-content">
        <div class="login-form-wrap">
            <c:if test="${param.error != null}">
                <div class="error-message">
                    아이디나 비밀번호가 일치하지 않습니다.
                </div>
            </c:if>

            <form action="/login-process" method="POST">
                <div class="input-group">
                    <input type="text" name="userId" class="input-field" placeholder="아이디" required>
                </div>
                <div class="input-group">
                    <input type="password" name="userPassword" class="input-field" placeholder="비밀번호" required>
                </div>
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <button type="submit" class="login-btn">로그인</button>
            </form>

            <div class="login-util">
                <a href="/findId" class="util-link">아이디 찾기</a>
                <span class="util-divider">|</span>
                <a href="/findPassword" class="util-link">비밀번호 찾기</a>
                <span class="util-divider">|</span>
                <a href="/register" class="util-link">회원가입</a>
            </div>
        </div>

        <div class="social-login">
            <p class="social-title">소셜 계정으로 로그인</p>
            <div class="social-buttons">
                <a href="/oauth2/authorization/google" class="social-button google">
                    <img src="/images/g-logo.png" alt="Google">
                    <span>구글 로그인</span>
                </a>
                <a href="/oauth2/authorization/naver" class="social-button naver">
                    <img src="/images/naver-logo.png" alt="Naver">
                    <span>네이버 로그인</span>
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
</body>
</html>