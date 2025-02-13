<%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>VocaLab-mypage</title>
        <!-- CSS 파일 경로 -->
        <link rel="stylesheet" type="text/css" href="/css/top.css">
        <link rel="stylesheet" type="text/css" href="/css/bottom.css">
        <link rel="stylesheet" type="text/css" href="/css/global.css">
        <link rel="stylesheet" type="text/css" href="/css/myPage/myPage.css">
        <!-- jQuery CDN -->
        <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
        <!-- JavaScript 파일 경로 -->
        <script src="/js/myPage/myPage.js"></script>
        <script src="/js/top.js"></script> <!-- 배너 -->
        <script src="/js/bottom.js"></script> <!-- 배너 -->

    </head>
    <body>
        <div class="mypage-container">
            <div id="banner_top" role="banner">
                <%@ include file="../banners/top_left.jsp" %>
            </div>
            <div class="mypage-main-container">
                <div class="sidebar">
                    <ul>
                        <li id="word-history" class="menu-item active">
                            <a href="/myPage/compileHistory">단어 생성 기록</a>
                        </li>
                        <li id="member-info" class="menu-item">
                            <a href="/myPage/userInformation">회원정보</a>
                        </li>
                    </ul>
                </div>
                <div class="word-history-page">
                    <h1>단어 생성 기록</h1>
                    <div class="mypage-line"></div>
<%--                        <div class="select-all">--%>
<%--                            <div>--%>
<%--                                <a id="select-all-a">전체선택</a> &nbsp;--%>
<%--                            </div>--%>
<%--                            <div>--%>
<%--                                <a id="deselect-a">선택해제</a> &nbsp;--%>
<%--                            </div>--%>
<%--                            <div>--%>
<%--                                <input type="button" id="deleteSelect" value="선택삭제">--%>
<%--                            </div>--%>
<%--                        </div>--%>
                        <div id="record-list">
                            <c:choose>
                                <c:when test="${!empty recordList}">
                                    <ul>
                                        <c:forEach var="record" items="${recordList}">
                                            <li id="li-${record.compileId}">
                                                <div>
                                                    <form name="form-${record.compileId}" action="/compile/result" method="post" enctype="multipart/form-data">
                                                        &nbsp;
                                                            <%--                                              <input type="checkbox" class="compile-record">&nbsp;--%>
                                                            ${record.createdAt}&nbsp;
                                                        <input type="hidden" value="${record.compileId}" name="compileId" class="compileId">
                                                        <input type="file" name="files" multiple hidden>
                                                        <c:choose>
                                                            <c:when test="${record.daysAgo < 1}" >
                                                                (${record.hoursAgo}시간 전)
                                                            </c:when>
                                                            <c:otherwise>
                                                                (${record.daysAgo}일 전)
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </form>
                                                </div>
                                            </li>
                                        </c:forEach>

                                    </ul>
                                </c:when>
                                <c:otherwise>
                                    <div class="noHistory">
                                        <div>단어 추출 기록이 없습니다.</div>
                                        <div>단어를 추출하고 원하는 단어로 나만의 단어장을 만들어 보세요.</div>
                                        <a href="/">단어 추출하러 가기</a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                </div>
            </div>
            <div class="banner_bottom">
                <%@ include file="../banners/bottom.jsp" %>
            </div>
        </div>
    </body>
</html>
