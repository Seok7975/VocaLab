<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>단어장 페이지</title>
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css" rel="stylesheet">
    <link href="/css/wordList.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <script src="/js/wordList.js"></script>
</head>
<body>
<div class="container-fluid">
    <input type="hidden" id="wordBookId" value="<%= request.getParameter("wordBookId") %>" />
    <div class="row">
        <!-- 선택된 단어 모달 창 -->
        <div id="selectedWordsModal" class="col-md-3 border-end p-3">
            <div class="d-flex justify-content-between align-items-center">
                <h5>선택된 단어</h5>
                <button id="btnMinimizeModal" class="btn btn-sm btn-secondary">-</button>
            </div>
            <ul id="selectedWordsList" class="list-group mt-3"></ul>
        </div>

        <div class="col-md-9">
            <div class="py-4">
                <h1 id="wordbook-title" class="d-flex align-items-center justify-content-center">
                    <span id="title-text">단어장</span>
                    <button class="btn btn-sm btn-outline-primary ms-2" id="btnEditTitle">
                        <i class="fas fa-edit"></i>
                    </button>
                </h1>
                <div class="action-buttons d-flex justify-content-between align-items-center">
                    <div class="search-section d-flex align-items-center gap-2">
                        <select id="sortSelect" class="form-select" style="width: auto;">
                            <option value="original">기본 순서</option>
                            <option value="abc">ABC 순</option>
                            <option value="zyx">ZYX 순</option>
                            <option value="meaning_asc">가나다(뜻) 순</option>
                            <option value="meaning_desc">역순(뜻)</option>
                        </select>
                        <button id="btnSelectAll" class="btn btn-primary">전체 선택</button>
                        <button id="btnDeselectAll" class="btn btn-secondary">전체 해제</button>
                        <input type="text" id="searchInput" class="form-control" placeholder="단어를 검색하세요">
                        <button class="btn btn-success" id="btnCreateContent" data-bs-toggle="modal" data-bs-target="#contentModal">컨텐츠 생성</button>
                        <button id="wordDeleteBtn" class="btn btn-danger btn-remove-word">단어 삭제</button>
                    </div>
                    <button id="btnRestoreModal" class="btn btn-primary restore-btn">+</button>
                </div>

                <div class="infinite-scroll-container">
                    <table class="table word-table">
                        <thead>
                        <tr>
                            <th colspan="6">단어장</th>
                        </tr>
                        </thead>
                        <tbody id="wordTableBody"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 단어 상세 정보 모달 -->
<div class="modal fade" id="wordDetailModal" tabindex="-1" aria-labelledby="wordDetailModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="wordDetailModalLabel">단어 상세 정보</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p><strong>단어:</strong> <span id="modalWord"></span></p>
                <p><strong>품사:</strong> <span id="modalType">N/A</span></p>
                <p><strong>뜻:</strong> <span id="modalMeaning"></span></p>
                <p><strong>예문:</strong> <span id="modalExample"></span></p>
                <p><strong>발음기호:</strong> <span id="modalSpeech">N/A</span></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>
<div class="modal fade" id="contentModal" tabindex="-1" role="dialog" aria-labelledby="contentModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="contentModalLabel">컨텐츠 생성</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="d-flex justify-content-around">
                    <button class="btn btn-primary" id="btnTest">테스트</button>
                    <button class="btn btn-secondary" id="btnWrite">작문</button>
                    <button class="btn btn-info" id="btnNews">뉴스</button>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
            </div>
        </div>
    </div>
</div>

<div class="modal fade" id="testTypeModal" tabindex="-1" role="dialog" aria-labelledby="testTypeModalLabel">
    <div class="modal-dialog" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="testTypeModalLabel">테스트 유형 선택</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div class="d-grid gap-3">
                    <button class="btn btn-primary test-type-btn" data-test-type="meaning">
                        1. 뜻 맞추기 테스트 (영어 → 한글)
                    </button>
                    <button class="btn btn-primary test-type-btn" data-test-type="word">
                        2. 단어 맞추기 테스트 (한글 → 영어)
                    </button>
                </div>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">취소</button>
            </div>
        </div>
    </div>
</div>
</body>
</html>
