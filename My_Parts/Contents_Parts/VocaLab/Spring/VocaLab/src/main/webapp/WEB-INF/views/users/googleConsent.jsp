<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>Google 로그인</title>
    <style>
        body {
            font-family: 'Google Sans', Roboto, sans-serif;
            margin: 0;
            padding: 24px;
            background-color: white;
        }
        .container {
            max-width: 450px;
            margin: 0 auto;
        }
        .header {
            text-align: center;
            margin-bottom: 32px;
        }
        .google-logo {
            width: 75px;
            margin-bottom: 16px;
        }
        .title {
            font-size: 24px;
            margin-bottom: 8px;
            color: #202124;
        }
        .subtitle {
            font-size: 16px;
            color: #5f6368;
            margin-bottom: 32px;
        }
        .service-info {
            padding: 24px;
            border: 1px solid #dadce0;
            border-radius: 8px;
            margin-bottom: 24px;
        }
        .info-section {
            margin-bottom: 24px;
        }
        .info-title {
            font-size: 14px;
            font-weight: 500;
            color: #202124;
            margin-bottom: 8px;
        }
        .info-item {
            font-size: 14px;
            color: #5f6368;
            margin: 4px 0;
        }
        .notice {
            font-size: 12px;
            color: #5f6368;
            line-height: 1.5;
            margin: 24px 0;
        }
        .buttons {
            display: flex;
            justify-content: flex-end;
            gap: 12px;
            margin-top: 32px;
        }
        .btn {
            padding: 8px 24px;
            border-radius: 4px;
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
        }
        .btn-cancel {
            background: white;
            color: #1a73e8;
            border: 1px solid #dadce0;
        }
        .btn-agree {
            background: #1a73e8;
            color: white;
            border: none;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <img src="${pageContext.request.contextPath}/images/google-logo.png" alt="Google" class="google-logo">
        <h1 class="title">VocaLab에 로그인</h1>
        <p class="subtitle">${userInfo.email}</p>
    </div>

    <div class="service-info">
        <div class="info-section">
            <div class="info-title">VocaLab에서 다음 정보에 액세스합니다:</div>
            <div class="info-item">• Google 계정 이메일 주소</div>
            <div class="info-item">• 이름</div>
            <div class="info-item">• 생년월일</div>
            <div class="info-item">• 성별</div>
        </div>

        <div class="notice">
            Google은 로그인 기능 제공자이며, VocaLab 서비스 제공자가 아닙니다.
            VocaLab 서비스 및 이용약관에 대한 의무와 책임은 VocaLab에 있습니다.
            계속하면 VocaLab의 이용약관 및 개인정보처리방침에 동의하는 것으로 간주됩니다.
        </div>
    </div>

    <div class="buttons">
        <button class="btn btn-cancel" onclick="window.close()">취소</button>
        <button class="btn btn-agree" onclick="confirmConsent()">동의</button>
    </div>
</div>
<script>
    function confirmConsent() {
        fetch('/oauth2/consent/confirm', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
        }).then(response => {
            if (response.ok) {
                window.opener.postMessage('oauth2-consent-success', '*');
                window.close();
            }
        });
    }
</script>
</body>
</html>
