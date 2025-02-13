 <%@ page language="java" contentType="text/html;charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Compile Result</title>
    <link rel="stylesheet" href="/css/global.css"> <%-- 글로벌 CSS --%>
    <link rel="stylesheet" href="/css/compile/compile_result.css"> <%-- 컴파일 결과 전용 CSS --%>
    <%-- jQuery CDN --%>
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <script src="/js/compile/compile_result.js"></script> <%-- 컴파일 결과 전용 JS --%>
</head>
<body>
    <form class="page-container" name="addWordBook" action="addWordBookData" method="post">
        <%-- top배너 삽입 --%>
        <div id="banner_top" role="banner">
            <%@ include file="../banners/top_left.jsp" %>
        </div>
        <%-- 왼쪽 섹션 --%>
        <div id="section">
            <section class="left-section">
                <h3>단어 추가하기</h3>
                <div class="selected">
                    <p id="selected-wordbook">
                        <a id="now-wordbook">[선택된 단어장]</a>
                        <input type="hidden" id="final-wordBookId" name="wordBookId" value="">
                    </p>
                    <p style="font-size: 20px">
                        총 <span id="selected-count" style="font-weight: bold">0</span>개 선택
                    </p>
                    <button class="wordbook-list-btn" type="button" onclick="window.location.href='#'">단어장 선택</button>
                    <button id="add-words-btn" type="submit">단어 추가</button>
                    <button id="exit-btn" type="button" onclick="window.location.href='/'">나가기</button>
                </div>
            </section>

            <%-- 오른쪽 섹션 --%>
            <section class="right-section">
                <div class="table-top">
                    <label class="meaning-switch">
                        <input role="switch" type="checkbox" id="toggle" checked>
                        <span>뜻</span>
                    </label>
                    <div class="choose-voca">
                        <input type="checkbox" name="choose-voca" id="all-checked">
                        <a id="all-checked-click">전체선택</a>
                        &nbsp;
                        <input type="checkbox" name="choose-voca" id="all-unchecked">
                        <a id="all-unchecked-click">선택해제</a>
                    </div>
                </div>
<%--                <div>${response}</div>--%>
                <%-- 단어 테이블 --%>
                <table class="word-table">
                    <tbody id="word-table-body">
                        <c:set var="total" value="${fn:length(response)}" />
                        <c:set var="rows" value="${(total / 4)}" />
                        <c:forEach var="i" begin="0" end="${rows}" step="1">
                        <tr>
                            <c:forEach var="j" begin="0" end="3" step="1">
                            <td>
                                <c:choose>
                                    <c:when test="${(i * 4 + j) < fn:length(response)}">
                                        <c:set var="wordData" value="${response[i * 4 + j]}" />
                                        <input type="checkbox" name="word" class="compile-result-voca" id="word-${i}-${j}"  value="${wordData['word']}">
                                        <input type="checkbox" name="meaning" class="compile-result-voca" id="meaning-${i}-${j}" value="${wordData['meaning']}">
                                        <strong><c:out value="${wordData['word']}" /></strong><br />
<%--                                        <input type="hidden" id="word-${i}-${j}" name="word" value="${wordData['word']}">--%>
                                        <a class="vocabulary" id="voca-${i}-${j}" data-meaning="${wordData['meaning']}"></a>
<%--                                        <input type="hidden" id="meaning-${i}-${j}" name="meaning" value="${wordData['meaning']}">--%>
                                    </c:when>
                                    <c:otherwise>
                                        &nbsp;
                                    </c:otherwise>
                                </c:choose>

                            </td>
                            </c:forEach>
                        </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </section>
        </div>
    </form>

    <div class="banner_bottom">
        <%@ include file="../banners/bottom.jsp" %>
    </div>

    <%-- 단어장 목록 모달 --%>
    <div class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3>단어장 목록</h3>
                <img src="/images/X.png" alt="닫기" class="close-modal" width="15px"/>
            </div>
            <div class="modal-body">
                <%-- 단어장 리스트 --%>
                <div class="modal-buttons">
                    <button class="add-btn">+</button>
                    &nbsp;
                    <button class="delete-btn">-</button>
                </div>
                <ul class="wordbook-list">
                    <%-- 단어장 이름 목록 --%>
                    <c:forEach var="book" items="${books}" >
                        <li>
                            <input type="checkbox" name="wordbook-${book.wordBookId}" class="choose-wordbook" id="wordbook-${book.wordBookId}"/>
                            <input type="hidden" name="wordbookId-${book.wordBookId}" id="wordbookId-${book.wordBookId}" value="${book.wordBookId}" >
                            <span class="wordbook-name">${book.wordBookTitle}</span>
                        </li>
                    </c:forEach>
                </ul>
            </div>
        </div>
    </div> <%-- 모달창 --%>
    <div class="mouse-cursor"></div><%-- 뜻 모달창 --%>
</body>
</html>