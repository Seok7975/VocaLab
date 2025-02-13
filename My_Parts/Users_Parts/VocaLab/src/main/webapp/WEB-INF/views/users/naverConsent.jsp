<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>


<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>네이버 로그인</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            margin: 0;
            padding: 0;
            background-color: #f5f6f7;
        }
        .container {
            max-width: 500px;
            margin: 0 auto;
            background: white;
            min-height: 100vh;
        }
        .header {
            background: #03c75a;
            color: white;
            padding: 12px 16px;
            display: flex;
            align-items: center;
        }
        .header img {
            width: 24px;
            height: 24px;
            margin-right: 8px;
        }
        .header h1 {
            font-size: 16px;
            margin: 0;
        }
        .content {
            padding: 20px;
        }
        .service-name {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 20px;
        }
        .info-box {
            border: 1px solid #e5e5e5;
            border-radius: 4px;
            padding: 16px;
            margin-bottom: 20px;
        }
        .info-title {
            color: #333;
            font-weight: bold;
            margin-bottom: 12px;
        }
        .info-item {
            color: #666;
            margin: 8px 0;
            font-size: 14px;
        }
        .notice {
            font-size: 12px;
            color: #888;
            line-height: 1.5;
            margin: 20px 0;
        }
        .buttons {
            position: fixed;
            bottom: 0;
            left: 0;
            right: 0;
            padding: 16px;
            background: white;
            border-top: 1px solid #e5e5e5;
            display: flex;
            gap: 8px;
        }
        .btn {
            flex: 1;
            padding: 12px;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            font-weight: bold;
            cursor: pointer;
        }
        .btn-cancel {
            background: #f5f6f7;
            color: #333;
        }
        .btn-agree {
            background: #03c75a;
            color: white;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="header">
        <img src="${pageContext.request.contextPath}/images/naver-logo-white.png" alt="네이버">
        <h1>네이버 로그인</h1>
    </div>

    <div class="content">
        <div class="service-name">VocaLab</div>

        <div class="info-box">
            <p>${userInfo.email} 회원님의 정보가 제공됩니다.</p>

            <div class="info-title">개인정보 제공 동의 (필수)</div>
            <div class="info-item">• 이름</div>
            <div class="info-item">• 이메일 주소</div>
            <div class="info-item">• 생년월일</div>
            <div class="info-item">• 성별</div>
        </div>

        <div class="notice">
            네이버는 회원가입/로그인 기능 제공자이며, VocaLab 서비스 제공자가 아닙니다.
            VocaLab 서비스 및 이용약관에 대한 의무와 책임은 VocaLab에 있습니다.
            동의 후에는 VocaLab의 이용약관 및 개인정보처리방침에 따라 정보가 관리됩니다.
        </div>
    </div>

    <div class="buttons">
        <button class="btn btn-cancel" onclick="window.close()">취소</button>
        <button class="btn btn-agree" onclick="confirmConsent()">동의하기</button>
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