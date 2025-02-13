<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<div class="reply-view mt-4">
    <!-- 답변 카드 -->
    <div class="card">
        <div class="card-header bg-white py-3">
            <div class="d-flex align-items-center">
                <span class="badge bg-success me-2">답변</span>
                <h5 class="card-title mb-0">답변 내용</h5>
            </div>
        </div>
        <div class="card-body" id="replyContent">
            <!-- 로딩 표시 -->
            <div class="text-center py-4">
                <div class="spinner-border text-primary" role="status">
                    <span class="visually-hidden">답변 로딩중...</span>
                </div>
                <p class="text-muted mt-2 mb-0">답변을 불러오는 중입니다...</p>
            </div>
        </div>
    </div>
</div>

<style>
    .table > :not(caption) > * > * {
        padding: 1rem;
    }
    .content-area {
        min-height: 200px;
        padding: 1rem;
        background-color: #f8f9fa;
        border-radius: 0.375rem;
        white-space: pre-wrap;
    }
</style>

<script>
    $(document).ready(function() {
        $.ajax({
            url: '/CS/Reply',
            type: 'GET',
            data: { parentId: ${row.boardId} },
            dataType: 'json',
            success: function(response) {
                if(response) {
                    var replyHtml =
                        '<table class="table table-bordered mb-0">' +
                        '<tbody>' +
                        '<tr>' +
                        '<th class="table-light" style="width: 10%">No</th>' +
                        '<td style="width: 15%">' + response.boardId + '</td>' +
                        '<th class="table-light" style="width: 10%">작성자</th>' +
                        '<td style="width: 25%">' + response.userNickname + '</td>' +
                        '<th class="table-light" style="width: 10%">작성일</th>' +
                        '<td style="width: 30%">' + response.createdAt + '</td>' +
                        '</tr>' +
                        '<tr>' +
                        '<th class="table-light">제목</th>' +
                        '<td colspan="5">' + response.title + '</td>' +
                        '</tr>';

                    replyHtml += '<tr>' +
                        '<th class="table-light">내용</th>' +
                        '<td colspan="5">' +
                        '<div class="content-area">' + response.content + '</div>' +
                        '</td>' +
                        '</tr>' +
                        '</tbody>' +
                        '</table>';

                    $('#replyContent').html(replyHtml);
                } else {
                    $('#replyContent').html(
                        '<div class="alert alert-info mb-0" role="alert">' +
                        '<i class="bi bi-info-circle me-2"></i>' +
                        '등록된 답변이 없습니다.' +
                        '</div>'
                    );
                }
            },
            error: function() {
                $('#replyContent').html(
                    '<div class="alert alert-danger mb-0" role="alert">' +
                    '<i class="bi bi-exclamation-triangle-fill me-2"></i>' +
                    '답변 조회 중 오류가 발생했습니다.' +
                    '</div>'
                );
                console.error('답변 조회 실패');
            }
        });
    });
</script>