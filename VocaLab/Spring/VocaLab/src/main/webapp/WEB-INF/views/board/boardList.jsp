<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@page import="com.dev.vocalab.board.BoardFunction"%>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle}</title>
    <!-- jQuery 먼저 로드 -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap 5.3.0 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <!-- Bootstrap JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <script>
        $(document).ready(function() {
            $('form[method="get"]').on('submit', function(e) {
                var searchWord = $('input[name="searchWord"]').val();
                // 더 엄격한 필터링
                if (/[<>{}[\]"';]|javascript:|data:|vbscript:|expression|alert|onclick|onerror/i.test(searchWord)) {
                    alert('검색어에 특수문자나 스크립트를 포함할 수 없습니다.');
                    e.preventDefault();
                    return false;
                }
            });
        });
    </script>
    <link href="/css/board.css" rel="stylesheet">
</head>
<body>
<%-- top배너 삽입 --%>
<div id="banner_top" role="banner">
    <%@ include file="../banners/top_left.jsp" %>
</div>
<div class="container py-4">
    <!-- 탭 메뉴 -->
    <ul class="board-tabs">
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${boardCat eq 'Notice' ? 'active' : ''}" href="/CS/Notice">공지사항</a>
        </li>
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${boardCat eq 'FAQ' ? 'active' : ''}" href="/CS/FAQ">FAQ</a>
        </li>
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${boardCat eq 'Inquiry' ? 'active' : ''}" href="/CS/Inquiry">1:1 문의</a>
        </li>
    </ul>

    <!-- 게시판 테이블 -->
    <div class="board">
        <table class="table table-hover">
            <thead class="table-light">
            <tr>
                <th style="width: 10%">No</th>
                <th style="width: ${boardCat eq 'Inquiry' ? '35%' : '45%'}">제목</th>
                <c:if test="${boardCat eq 'Inquiry'}">
                    <th style="width: 10%">답변 여부</th>
                </c:if>
                <th style="width: 15%">작성자</th>
                <th style="width: 15%">작성일</th>
                <th style="width: 15%">수정일</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach items="${boardList.content}" var="row" varStatus="vs">
                <tr onclick="location.href='View?boardId=${row.boardId}'">
                    <td>${row.boardId}</td>
                    <td class="text-break">${row.title}</td>
                    <c:if test="${boardCat eq 'Inquiry'}">
                        <td>
                            <c:choose>
                                <c:when test="${empty row.replyStatus || row.replyStatus eq 'NONE'}">
                                    <span class="badge bg-secondary">답변 대기중</span>
                                </c:when>
                                <c:when test="${row.replyStatus eq 'DONE'}">
                                    <span class="badge bg-success">답변 완료</span>
                                </c:when>
                            </c:choose>
                        </td>
                    </c:if>
                    <td>${row.userNickname}</td>
                    <td>${BoardFunction.getShortFormattedDate(row.createdAt)}</td>
                    <td>
                        <c:if test="${row.updatedAt != row.createdAt}">
                            ${BoardFunction.getShortFormattedDate(row.updatedAt)}
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            </tbody>
        </table>
    </div>

    <!-- 페이지네이션 -->
    <div class="page-number text-center my-4">
        ${pagingImg}
    </div>

    <!-- 검색 및 작성 버튼 -->
    <div class="board-search-group">
        <c:if test="${not empty userSession}">
            <button type="button" class="btn btn-primary" onclick="location.href='/CS/Write';">
                작성
            </button>
        </c:if>
        <form method="get" action="/CS/${boardCat}" class="d-flex flex-grow-1">
            <input type="text" class="form-control" name="searchWord" value="${searchWord}"
                   placeholder="검색어를 입력하세요">
            <button type="submit" class="btn btn-outline-primary ms-2">검색</button>
        </form>
    </div>
</div>
<div class="banner_bottom">
    <%@ include file="../banners/bottom.jsp" %>
</div>
</body>
</html>
