$(document).ready(function () {
    const maxFiles = 5; // 최대 파일 개수
    const fileInput = $("#file-upload"); // 파일 입력 요소
    const filePreviewList = $("#file-preview-list"); // 미리보기 리스트


    // 파일 아이콘 마우스 이벤트
    const comment = "파일은 최대 " + maxFiles + "개까지 업로드할 수 있습니다.";
    const fileComment = $('<div class="file-comment"></div>').text(comment);
    $(".file-icon")
        .on("mouseenter", function () {
            $(".input-group").before(fileComment);
        })
        .on("mouseleave", function () {
            $(".file-comment").remove();
        });

    $(".file-icon").on("click", function (event) {
        if ($("#file-upload")[0].files.length > 0) {
            // 안내창 띄우기
            alert("파일이 이미 선택되었습니다. 두 개 이상의 파일 업로드는 파일을 삭제 후 다중 선택 해 주세요.");
            // 기본 동작(파일 선택창 열기) 방지
            event.preventDefault();
        }
    });


    // 파일 업로드 변경 이벤트
    fileInput.on("change", function () {
        const files = Array.from(this.files); // 업로드된 파일 배열
        const dataTransfer = new DataTransfer(); // DataTransfer 객체 생성

        // 파일 개수 제한
        if (filePreviewList.children().length + files.length > maxFiles) {
            alert(`파일은 최대 ${maxFiles}개까지 업로드할 수 있습니다.`);
            fileInput.val(""); // 파일 입력 필드 초기화
            return;
        }

        // 새로 추가된 파일을 업로드 목록과 미리보기 리스트에 추가
        files.forEach((file) => {
            const previewContainer = $(`
                <div class="preview-container">
                    <img class="preview-icon" src="images/docs.png" width="20" height="20">
                    <span class="file-name">${file.name}</span>
                    <img class="preview-icon delete-icon" src="images/X.png" width="10" height="10">
                </div>
            `);

            // 삭제 버튼 이벤트 추가
            previewContainer.find(".delete-icon").on("click", function () {
                const updatedFiles = Array.from(fileInput[0].files).filter(
                    (f) => f.name !== file.name
                ); // 삭제할 파일 제외한 파일 리스트 생성

                const newDataTransfer = new DataTransfer(); // 새로운 DataTransfer 객체 생성
                updatedFiles.forEach((remainingFile) => {
                    newDataTransfer.items.add(remainingFile); // 남은 파일 추가
                });

                fileInput[0].files = newDataTransfer.files; // 파일 입력 필드 업데이트
                previewContainer.remove(); // 미리보기 컨테이너 삭제

                // 모든 파일 삭제 시 파일 입력 필드 초기화
                if (fileInput[0].files.length === 0) {
                    fileInput.val(""); // 파일 입력 필드 초기화
                }
            });

            // 미리보기 리스트에 추가
            filePreviewList.append(previewContainer);
        });
    });

    // 단어 추출 버튼 클릭 이벤트
    $("form").submit(function () {
        if ($("#url-input").val().trim() === "" && $("#file-upload")[0].files.length === 0) {
            alert("단어를 추출할 자료가 없습니다. URL을 입력하거나 파일을 업로드해주세요.");

            return false;
        }
        // 로딩 화면 표시
        $("#loading-screen").show();
        return true;
    });
});
