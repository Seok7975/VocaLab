<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${row.title}</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <!-- jQuery -->
    <script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
    <!-- Bootstrap 5 Bundle JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>

    <style>
        .board.detail-view {
            max-width: 900px;
            margin: 2rem auto;
            padding: 0 1rem;
        }
        .content-area {
            min-height: 300px;
            padding: 1rem;
            white-space: pre-wrap;
        }
        .nav-tabs {
            border-bottom: 1px solid #dee2e6;
        }
        .nav-tabs .nav-link {
            border: none;
            border-bottom: 2px solid transparent;
            color: #495057;
        }
        .nav-tabs .nav-link.active {
            border-bottom: 2px solid #0d6efd;
            color: #0d6efd;
        }
        .content-area {
            min-height: 300px;
            padding: 1rem;
            white-space: pre-wrap;
            overflow: hidden; /* 넘치는 콘텐츠 숨김 */
        }

        .content-area img {
            max-width: 100%; /* 부모 요소의 너비를 넘지 않도록 */
            height: auto; /* 비율 유지 */
            display: block; /* 인라인 요소를 블록으로 변경 */
            margin: 1rem auto; /* 상하 여백과 중앙 정렬 */
        }
    </style>

    <script>
        function confirmDelete() {
            if (confirm("정말 삭제하시겠습니까?")) {
                document.getElementById('deleteForm').submit();
            }
        }

        function transformCategory(category) {
            if (category === 'FAQ') {
                return 'FAQ';
            }
            return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
        }

        $(document).ready(function() {
            const category = '${row.category}';
            const transformedCategory = transformCategory(category);

            $('#list-button').on('click', function() {
                location.href = '/CS/' + transformedCategory;
            });
        });
    </script>
</head>
<body>
<div class="board detail-view">
    <!-- 상단 탭 메뉴 -->
    <ul class="nav nav-tabs mb-4">
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${row.category eq 'NOTICE' ? 'active' : ''}" href="/CS/Notice">공지사항</a>
        </li>
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${row.category eq 'FAQ' ? 'active' : ''}" href="/CS/FAQ">FAQ</a>
        </li>
        <li class="nav-item" style="width: 33.33%">
            <a class="nav-link text-center ${row.category eq 'INQUIRY' ? 'active' : ''}" href="/CS/Inquiry">1:1 문의</a>
        </li>
    </ul>

    <!-- 게시글 내용 -->
    <div class="card mb-4">
        <div class="card-header bg-white">
            <div class="row align-items-center">
                <div class="col">
                    <h5 class="mb-0">
                        <c:choose>
                            <c:when test="${row.category eq 'NOTICE'}">
                                <span class="badge bg-primary me-2">공지사항</span>
                            </c:when>
                            <c:when test="${row.category eq 'FAQ'}">
                                <span class="badge bg-info me-2">FAQ</span>
                            </c:when>
                            <c:when test="${row.category eq 'INQUIRY'}">
                                <span class="badge bg-success me-2">1:1 문의</span>
                            </c:when>
                        </c:choose>
                        ${row.title}
                    </h5>
                </div>
            </div>
        </div>
        <div class="card-body">
            <div class="row mb-3">
                <div class="col-md-6">
                    <small class="text-muted">
                        <strong>No.</strong> ${row.boardId} |
                        <strong>작성자:</strong> ${row.userNickname}
                    </small>
                </div>
                <div class="col-md-6 text-md-end">
                    <small class="text-muted">
                        <strong>작성일:</strong> ${row.createdAt}
                        <c:if test="${row.updatedAt != row.createdAt}">
                            | <strong>수정일:</strong> ${row.updatedAt}
                        </c:if>
                    </small>
                </div>
            </div>
            <div class="content-area border rounded bg-light">
                ${row.content}
            </div>
        </div>
    </div>
    <!-- 삭제 폼 -->
    <form id="deleteForm" action="/CS/Delete" method="post">
        <input type="hidden" name="boardId" value="${row.boardId}"/>
    </form>

    <!-- 하단 버튼 -->
    <div class="form-actions text-end">
        <c:if test="${row.replyStatus ne 'DONE'}">
            <c:if test="${row.userId.toString() eq userSession.userId.toString()}">
                <button type="button" class="btn btn-primary"
                        onclick="location.href='/CS/Edit?boardId=${row.boardId}';">
                    수정
                </button>
            </c:if>
        </c:if>
        <c:if test="${userSession != null and (row.userId eq userSession.userId or userSession.userRole eq 'ADMIN')}">
            <button type="button" class="btn btn-danger" onclick="confirmDelete();">
                삭제
            </button>
        </c:if>
        <button id="list-button" type="button" class="btn btn-secondary">
            목록
        </button>
    </div>

    <!-- 답변 영역 -->
    <c:if test="${row.category eq 'INQUIRY'}">
        <div class="reply-area mb-4">
            <c:if test="${row.replyStatus eq 'NONE'}">
                <c:import url="/WEB-INF/views/board/boardReplyWrite.jsp" />
            </c:if>
            <c:if test="${row.replyStatus eq 'DONE'}">
                <c:import url="/WEB-INF/views/board/boardReplyView.jsp" />
            </c:if>
        </div>
    </c:if>
</div>
</body>
</html>