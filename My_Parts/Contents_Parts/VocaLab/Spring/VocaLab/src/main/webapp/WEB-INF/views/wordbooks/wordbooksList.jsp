<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${userNickname}님의 단어장 목록</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <!-- jQuery -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap 5 Bundle JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" rel="stylesheet">
    <!-- JS 분리 -->
    <script src="/js/wordbooksList.js"></script>
    <style>
        .wordbook-container {
            display: flex;
            flex-wrap: wrap;
            gap: 15px;
            justify-content: flex-start; /* 오른쪽부터 빈 공간이 생기도록 설정 */
        }

        .wordbook-card {
            flex: 0 0 calc(25% - 15px); /* 4열 구성 */
            max-width: calc(25% - 15px);
            border: 1px solid #dee2e6;
            border-radius: 5px;
            padding: 15px;
            transition: transform 0.2s ease-in-out;
            background-color: #fff;
            box-sizing: border-box;
        }

        .wordbook-card:hover {
            transform: scale(1.05);
        }

        .modal-content {
            border-radius: 10px;
        }
    </style>
</head>
<body>
<div class="container py-4">
    <h1>${userNickname}님의 단어장 목록</h1>

    <!-- 정렬 및 필터링 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <div>
            <select id="sortBy" class="form-select" style="width: 200px;">
                <option value="createdAsc">생성순</option>
                <option value="createdDesc">최신순</option>
                <option value="titleAsc">가나다 순</option>
            </select>
        </div>
        <div>
            <input type="checkbox" id="showFavorites" class="form-check-input">
            <label for="showFavorites" class="form-check-label">즐겨찾기만 보기</label>
        </div>
    </div>
    <div id="wordbooksContainer" class="wordbook-container">
        <c:forEach items="${wordbooks}" var="wordbook">
            <div class="wordbook-card"
                 data-wordbook-id="${wordbook.wordBookId}"
                 data-wordbook-title="${wordbook.wordBookTitle}"
                 data-wordbook-bookmark="${wordbook.bookmark}">
                <h5>${wordbook.wordBookTitle}</h5>
                <p>생성일: ${wordbook.createdAt}</p>
                <c:if test="${wordbook.bookmark}">
                    <span class="badge bg-warning text-dark">즐겨찾기</span>
                </c:if>
            </div>
        </c:forEach>
    </div>
</div>
<!-- 모달 -->
<div class="modal fade" id="wordbookModal" tabindex="-1" aria-labelledby="modalWordbookTitle" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="modalWordbookTitle"></h5>
                <div class="d-flex align-items-center">
                    <button class="btn btn-sm btn-outline-primary me-2" id="btnEditTitle">
                        <i class="fas fa-edit"></i> 수정
                    </button>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
            </div>
            <div class="modal-body">
                <div class="d-flex flex-column gap-3">
                    <button class="btn btn-primary" id="btnMoveWordbook">단어장 이동</button>
                    <button class="btn btn-danger" id="btnDeleteWordbook">단어장 삭제</button>
                    <button class="btn btn-warning text-dark" id="btnToggleFavorite"></button>
                    <button class="btn btn-secondary" id="btnGoWriting">작문 이동</button>
                    <button class="btn btn-secondary" id="btnGoNews">뉴스 이동</button>
                    <button class="btn btn-secondary" id="btnGoTest">테스트 이동</button>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>
<script>
</script>

</body>
</html>
