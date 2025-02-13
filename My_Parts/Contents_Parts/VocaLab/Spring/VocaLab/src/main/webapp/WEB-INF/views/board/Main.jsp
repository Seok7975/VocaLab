<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%--
    고객센터 메인 페이지 입니다.
--%>
<html>
<head>
    <title>고객센터 메인 페이지</title>
</head>
<body>
<div class="nav">
    <a class="logo">VocaLab</a>

    <sec:authorize access="isAuthenticated()">
        <span>
            <%-- 컨트롤러에서 전달받은 loginType에 따라 표시 --%>
            <c:choose>
                <c:when test="${loginType eq 'normal'}">
                    ${userId}님 (${userNickname})
                </c:when>
                <c:when test="${loginType eq 'oauth2'}">
                    ${userNickname}님
                </c:when>
                <c:when test="${loginType eq 'oidc'}">
                    ${userNickname}님
                </c:when>
            </c:choose>

            <a href="/logout">로그아웃</a>
        </span>


        <%-- 디버깅용 정보 (필요시 주석 해제)--%>

        <div style="display: none">
            로그인 타입: ${loginType}<br>
            사용자 ID: ${userId}<br>
            사용자 이름: ${userName}<br>
            닉네임: ${userNickname}<br>
        </div>

    </sec:authorize>

    <sec:authorize access="!isAuthenticated()">
        <span>
            <a href="/login">로그인</a>
        </span>
    </sec:authorize>
</div>

    <p>메인페이지임</p>
</body>
</html>
