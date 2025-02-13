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
</head>
<body>
    <div class="nav">
        <a class="logo">VocaLab</a>

        <sec:authorize access="isAuthenticated()">
            <span>
                <sec:authentication property="principal.username" />님
                <a href="/logout">로그아웃</a>
            </span>
        </sec:authorize>

        <sec:authorize access="!isAuthenticated()">
            <span>
                <a href="/login">로그인</a>
            </span>
        </sec:authorize>
    </div>
    <h4>로그인페이지</h4>

    <c:if test="${param.error != null}">
        <h4>아이디나 비밀번호가 일치하지 않습니다.</h4>
    </c:if>

    <form action="/login" method="POST">
        <input name="username">
        <input name="password" type="password">
        <button type="submit">로그인</button>
        <a href="${pageContext.request.contextPath}/signup" class="btn">회원가입</a>
        <%-- <button class="btn" onclick="location.href='/signup'">회원가입</button> --%>
    </form>
</body>
</html>