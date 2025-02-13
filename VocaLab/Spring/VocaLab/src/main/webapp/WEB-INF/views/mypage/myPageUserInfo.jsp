<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>VocaLab-mypage</title>
        <!-- CSS 파일 경로 -->
        <link rel="stylesheet" type="text/css" href="/css/global.css">
        <link rel="stylesheet" type="text/css" href="/css/myPage/myPage.css">
        <!-- jQuery CDN -->
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <!-- JavaScript 파일 경로 -->
        <script src="/js/top.js"></script> <!-- 배너 -->
        <script src="/js/bottom.js"></script> <!-- 배너 -->
        <script src="/js/myPage/myPage.js"></script> <!-- 배너 -->
    </head>
    <body>
        <div class="mypage-container">
            <div id="banner_top" role="banner">
                <%@ include file="../banners/top_left.jsp" %>
            </div>
            <div class="mypage-main-container">

                <div class="sidebar">
                    <ul>
                        <li id="word-history" class="menu-item">
                            <a href="/myPage/compileHistory">단어 생성 기록</a>
                        </li>
                        <li id="member-info" class="menu-item active">
                            <a href="/myPage/userInformation">회원정보</a>
                        </li>
                    </ul>
                </div>
                <div class="member-info-page">
                    <h1>회원 상세 정보</h1>
                    <div class="mypage-line"></div>
                    <form name="infoForm" action="checkPassword" method="post">
                        <div class="member-info-list">
                            <span class="info-list users">
                                <div>아이디: </div>
                                <label for="password" id="passLabel">비밀번호: </label>
                                <div>이름: </div>
                                <div>닉네임: </div>
                                <div>이메일: </div>
                                <div>생년월일: </div>
                                <div>성별: </div>
                            </span>
                            <span class="info-list user">
                                <div>${user.userId}</div>
                                <input type="password" name="userPassword" id="password">
                                <div>${user.userName}</div>
                                <div>${user.userNickname}</div>
                                <div>${user.userEmail}</div>
                                <div>${birth}</div>
                                <div>
                                    <c:choose>
                                        <c:when test="${user.gender eq 1}" >
                                            남자
                                        </c:when>
                                        <c:otherwise>
                                            여자
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </span>
                        </div>
                        <button type="submit" id="modifyBtn">회원정보 수정</button>
                    </form>
                </div>

            </div>
            <div class="banner_bottom">
                <%@ include file="../banners/bottom.jsp" %>
            </div>
        </div>
    </body>
</html>