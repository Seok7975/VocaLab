<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>게시물 수정</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/css/bootstrap.min.css" rel="stylesheet">
    <!-- Summernote CSS -->
    <link href="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.css" rel="stylesheet">
    <!-- jQuery -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/3.6.0/jquery.min.js"></script>
    <!-- Bootstrap 5 Bundle JS -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap/5.3.0/js/bootstrap.bundle.min.js"></script>
    <!-- Summernote JS -->
    <script src="https://cdn.jsdelivr.net/npm/summernote@0.8.18/dist/summernote-lite.min.js"></script>

    <style>
        .board.write-view {
            max-width: 900px;
            margin: 2rem auto;
            padding: 0 1rem;
        }
        .note-editor {
            margin-top: 1rem;
        }
        .form-actions {
            margin-top: 1rem;
            text-align: right;
        }
    </style>

    <script>
        $(document).ready(function() {
            // content를 불러와서 필터링
            var content =  `${row.content}`.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, '');
            $("#title").val('${row.title}'.replace(/<script\b[^<]*(?:(?!<\/script>)<[^<]*)*<\/script>/gi, ''));
            // 필터링된 content를 summernote에 설정
            $('#summernote').val(content);
            $('#summernote').summernote({
                height: 800,
                lang: 'ko-KR',
                toolbar: [
                    ['fontsize', ['fontsize']],
                    ['style', ['bold', 'italic', 'underline','strikethrough', 'clear']],
                    ['color', ['color']],
                    ['table', ['table']],
                    ['para', ['ul', 'ol', 'paragraph']],
                    ['height', ['height']],
                    ['insert',['picture']]
                ],
                fontNames: ['Arial', 'Arial Black', 'Comic Sans MS', 'Courier New','맑은 고딕','궁서','굴림체','굴림','돋음체','바탕체'],
                fontSizes: ['8','9','10','11','12','14','16','18','20','22','24','28','30','36','50','72'],
                styleTags: [
                    'p',
                    {
                        title: 'Blockquote',
                        tag: 'blockquote',
                        className: 'blockquote',
                        value: 'blockquote',
                    },
                    'pre',
                    {
                        title: 'code_light',
                        tag: 'pre',
                        className: 'code_light',
                        value: 'pre',
                    },
                    {
                        title: 'code_dark',
                        tag: 'pre',
                        className: 'code_dark',
                        value: 'pre',
                    },
                    'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
                ],
                focus: true,
                callbacks: {
                    onImageUpload: function(files) {
                        for (var i = files.length - 1; i >= 0; i--) {
                            uploadSummernoteImageFile(files[i], this);
                        }
                    },
                    onMediaDelete: function($target) {
                        if (confirm('이미지를 삭제하시겠습니까?')) {
                            var deletedImageUrl = $target.attr('src').split('/').pop();
                            deleteSummernoteImageFile(deletedImageUrl);
                        }
                    },
                    onPaste: function (e) {
                        var bufferText = ((e.originalEvent || e).clipboardData || window.clipboardData).getData('Text');
                        e.preventDefault();
                        document.execCommand('insertText', false, bufferText);

                    },
                    sanitize: true,
                }
            });

            $('form[name="frm"]').on('submit', function(e) {
                // 제목 필터링
                var title = $('#title').val();
                if (/<[^>]*>|alert|onerror|onclick/i.test(title)) {
                    alert('제목에 HTML 태그나 스크립트를 포함할 수 없습니다.');
                    e.preventDefault();
                    return false;
                }
                // 내용 필터링 및 검증
                var content = $('#summernote').summernote('code');
                if (!content || content === '<p><br></p>') {
                    alert('내용을 입력하세요.');
                    e.preventDefault();
                    return false;
                }
            });
        });

        function uploadSummernoteImageFile(file, el, caption) {
            var data = new FormData();
            data.append('file', file);

            $.ajax({
                data: data,
                type: 'POST',
                url: '/CS/uploadSummernoteImageFile',
                contentType: false,
                enctype: 'multipart/form-data',
                processData: false,
                success: function(data) {
                    let imageUrl = data.url;
                    $(el).summernote(
                        'editor.insertImage',
                        imageUrl,
                        function($image) {
                            $image.attr('alt', caption);
                        }
                    );
                },
                error: function() {
                    alert("이미지 업로드 중 오류가 발생했습니다.");
                },
            });
        }

        function deleteSummernoteImageFile(imageName) {
            var data = new FormData();
            data.append('file', imageName);
            $.ajax({
                data: data,
                type: 'POST',
                url: '/CS/deleteSummernoteImageFile',
                contentType: false,
                enctype: 'multipart/form-data',
                processData: false
            });
        }
        const boardCat = transformCategory('${row.category}');
        function transformCategory(category) {
            if (category === 'FAQ') {
                return 'FAQ';
            }
            return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
        }
    </script>
</head>
<body>
<div class="board write-view">
    <div class="card">
        <div class="card-header bg-white">
            <h5 class="mb-0">게시물 수정</h5>
        </div>
        <div class="card-body">
            <form name="frm" method="post" action="/CS/Update" enctype="multipart/form-data" class="needs-validation" novalidate>
                <!-- Hidden inputs -->
                <input type="hidden" name="replyStatus" value="${row.replyStatus}"/>
                <input type="hidden" name="boardId" value="${row.boardId}"/>
                <input type="hidden" name="category" value="${row.category}"/>
                <input type="hidden" name="createdAt" value="${row.createdAt}"/>

                <!-- Title input -->
                <div class="mb-3">
                    <label for="title" class="form-label">제목</label>
                    <input type="text" class="form-control" id="title" name="title"
                           placeholder="제목을 입력하세요" required value="${row.title}">
                    <div class="invalid-feedback">
                        제목을 입력해주세요.
                    </div>
                </div>

                <!-- Content textarea -->
                <div class="mb-3">
                    <textarea id="summernote" name="content" required>${row.content}</textarea>
                </div>

                <!-- Form actions -->
                <div class="form-actions">
                    <button type="button" class="btn btn-secondary"
                            onclick="location.href='/CS/' + boardCat;">
                        목록
                    </button>
                    <button type="submit" name="doit" class="btn btn-primary">
                        저장
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>
</body>
</html>