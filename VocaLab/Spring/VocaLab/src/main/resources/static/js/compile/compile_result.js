$(document).ready(function () {
    // [ 단어장 모달창 ]
    // 단어장 목록 링크 클릭 시 모달창 열기
    $(".wordbook-list-btn").click(function () {
        $(".modal").fadeIn(); // 모달창 열기
        console.log("모달창 열기");
    }); //open modal

    // 모달창 닫기
    $(".close-modal").click(function () {
        $(".modal").fadeOut(); // 모달창 닫기
        $(".new-wordbook").hide();
        console.log($(".choose-wordbook:checked").length > 1 );
        if ($(".choose-wordbook:checked").length > 1 ) {
            $(".wordbook-name").css({
                "color": "black",
                "text-decoration": "none"
            });
            // 체크 해제
            $(".choose-wordbook").prop("checked", false); // 체크박스 선택 해제
            console.log($(".choose-wordbook:checked").length > 1 );
        }
    });// close modal

    // 동적으로 추가된 요소를 포함하여 모든 .wordbook-name 클릭 처리
    $(".wordbook-list").on("click", ".wordbook-name", function () {
        console.log("단어장 선택")
        const parentLi = $(this).closest("li"); // 현재 클릭한 텍스트의 부모 <li> 요소
        const checkbox = parentLi.find(".choose-wordbook"); // 해당 <li> 내의 체크박스 선택
        const isChecked = checkbox.is(":checked"); // 체크박스 상태 확인
        const wordBookIdValue = parentLi.find("input[type='hidden']").val();// hidden input에서 ID 값 가져오기
        if (!isChecked) {
            // 체크박스 선택
            checkbox.prop("checked", true); // 체크박스 선택
            $(this).css({
                "color": "tomato",
                "text-decoration": "underline"
            });
            $("#now-wordbook").text($(this).text());
            $("#final-wordBookId").val(wordBookIdValue);
            console.log("선택된 단어장은 : " + $("#final-wordBookId").val());
        } else {
            // 체크박스 선택 해제
            checkbox.prop("checked", false); // 체크박스 선택 해제
            $(this).css({
                "color": "black",
                "text-decoration": "none"
            });
            $("#now-wordbook").text("단어장미선택");
            $("#final-wordBookId").val("");
            console.log("미선택" + $("#final-wordBookId").val())
        }

    }); //wordbookname on click

    // 단어장 선택 버튼
    $(".add-btn").click(function () {
        if($(".new-wordbook").length === 0){
            $(".wordbook-list").append('<li><input type="text" class="new-wordbook" placeholder="새 단어장"></li>');
            $(".new-wordbook").focus(); // 텍스트 박스 포커스
        } else if($(".new-wordbook").val().trim() === ""){
            alert("새 단어장 입력란이 이미 존재합니다. 단어를 입력해주세요.");
            $(".new-wordbook").focus();
        } else {
            alert("단어장 추가를 위해 엔터를 눌러주세요.");
            $(".new-wordbook").focus();
        }

        // 텍스트 박스에서 키보드 입력 이벤트
        $(".new-wordbook").off("keypress").on("keypress", function (event) {

            // 엔터 키를 눌렀을 때
            if (event.key === "Enter") {
                const inputBox = $(this); // 현재 입력 중인 텍스트 박스
                const inputValue = inputBox.val().trim(); // 텍스트 박스의 값
                if (inputValue !== "") {
                    console.log("엔터키");
                    // 값이 비어 있지 않다면 AJAX 요청
                    $.ajax({
                        url: "addWordbook",
                        type: "POST",
                        data: {wordBookTitle: inputValue},
                        success: function (data) {
                            inputBox.parent("li").remove(); // 해당 입력 박스를 삭제
                            $(".choose-wordbook").prop("checked", false); // 체크박스 선택 해제
                            $(".wordbook-name").css({
                                "color": "black",
                                "text-decoration": "none"
                            });
                            console.log(data);
                            var code =
                                "<li>"
                                + "<input type=\"checkbox\" name=\"wordbook-" + data.wordBookId + "\""
                                + "class=\"choose-wordbook\" id=\"wordbook" + data.wordBookId + "\"/>"
                                + "<input type = \"hidden\" name = \"wordbookId-" + data.wordBookId + "\""
                                + "id = \"wordbookId-" + data.wordBookId + "\" value=\"" + data.wordBookId + "\">"
                                + "<span class=\"wordbook-name\">" + data.wordBookTitle + "</span>" +
                                "</li>";
                            if (data != null || data != "") {
                                $(".wordbook-list").append(code);
                            }
                        },
                        error: function (e) {
                            alert("단어장 추가에 실패했습니다." + e); // 오류 메시지
                        }
                    });
                } else {
                    alert("단어장 이름을 입력하세요."); // 입력값이 없을 때 경고
                }
                event.preventDefault();// 기본 동작 방지
            }
        });

        // esc눌렀을 때 텍스트 박스 사라짐
        $(".new-wordbook").off("keydown").on("keydown", function (event) {
            if (event.key === "Escape") {
                // ESC 키를 눌렀을 때
                $(this).parent("li").remove();
            }
        });
    }); //add btn

    // 단어장 삭제 버튼
    $(".delete-btn").click(function () {

        const selected = $(".choose-wordbook:checked").closest("li"); // 선택된 항목

        if (selected.length === 0) {
            alert("삭제할 단어장을 선택하세요."); // 선택된 항목이 없을 때 경고
        } else {
            const wordBookTitles = []; // 삭제할 단어장 이름 저장
            const wordBookIds = []; // 삭제할 단어장 ID 저장

            // 선택된 각 항목 처리
            selected.each(function () {
                const wordBookTitle = $(this).find(".wordbook-name").text(); // 단어장 이름
                const wordBookId = $(this).find(".choose-wordbook").attr("id").replace(/\D/g, ""); // 단어장 ID
                wordBookTitles.push(wordBookTitle);
                wordBookIds.push(wordBookId);
                console.log(wordBookIds);
                console.log(wordBookTitles);
            });

            // 삭제 확인 메시지
            const confirmMessage = `${wordBookTitles.join(", ")}을(를) 삭제하시겠습니까? 단어장의 단어 데이터도 함께 삭제됩니다.`;
            if (confirm(confirmMessage)) {
                // AJAX 요청
                $.ajax({
                    url: "removeWordbook", // 서버의 삭제 처리 URL
                    type: "POST",
                    contentType: "application/json", // JSON 형태로 데이터 전송
                    data: JSON.stringify({ids: wordBookIds}), // 삭제할 단어장의 ID 배열
                    success: function (response) {
                        console.log(response);
                        if(response){
                            alert("단어장이 삭제되었습니다."); // 성공 메시지
                            selected.remove(); // 삭제된 항목 화면에서 제거
                        }else{
                            alert("삭제 실패");
                        }
                    },
                    error: function (xhr, status, error) {
                        alert("단어장 삭제에 실패했습니다."); // 오류 메시지
                        console.error(error); // 오류 로그
                    }
                });
            }
        }
    }); //delete btn

    // [ 오른쪽 섹션 ]
    let isAllChecked = false; // 전체 선택 상태를 추적

    // 단어와 뜻 체크박스 동시에 체크됨
    $("input[name='word']").on("change", function () {
        // 단어 체크박스의 선택 상태를 가져옴
        const isChecked = $(this).prop("checked");
        // 같은 td 내부의 뜻 체크박스 선택 상태도 동일하게 변경
        $(this).closest("td").find("input[name='meaning']").prop("checked", isChecked);
    });

    // 선택된 단어 갯수 업데이트 함수
    function updateSelectedCount() {
        const selectedCount = $("input[name='word']:checked").length; // 선택된 체크박스 갯수
        $("#selected-count").text(selectedCount); // 갯수를 업데이트
    }

    // 전체 선택 버튼 클릭 이벤트
    $("#all-checked-click").on("click", function () {
        isAllChecked = true; // 전체 선택 상태 설정
        $(".compile-result-voca").prop("checked", true); // 모든 체크박스 선택
        $(".word-table td").addClass("selectedWord"); // 선택 스타일 추가
        updateSelectedCount(); // 선택 갯수 업데이트
    });

    // 선택 해제 버튼 클릭 이벤트
    $("#all-unchecked-click").on("click", function () {
        isAllChecked = false; // 전체 선택 상태 해제
        $(".compile-result-voca").prop("checked", false); // 모든 체크박스 선택 해제
        $(".word-table td").removeClass("selectedWord"); // 선택 스타일 제거
        updateSelectedCount(); // 선택 갯수 업데이트
    });

    // 단어 셀 클릭 이벤트
    $(".word-table td").on("click", function (e) {
        const $wordCheckbox = $(this).find("input[name='word']");
        const $meaningCheckbox = $(this).find("input[name='meaning']");

        // checkbox 클릭이 아닐 경우 체크 상태 반전
        if (!$(e.target).is('input[type="checkbox"]')) {
            const newState = !$wordCheckbox.prop("checked"); // 단어 checkbox 기준으로 토글
            $wordCheckbox.prop("checked", newState);
            $meaningCheckbox.prop("checked", newState);
        }

        // 개별 선택 시 전체 선택 해제
        if (isAllChecked) {
            isAllChecked = false; // 전체 선택 상태 해제
            $("#all-checked").prop("checked", false);
        }

        // 선택 상태에 따라 스타일 적용
        if ($wordCheckbox.prop("checked")) {
            $(this).addClass("selectedWord");
        } else {
            $(this).removeClass("selectedWord");
        }

        updateSelectedCount(); // 선택 갯수 업데이트
    });

    // 체크박스 직접 클릭 시 이벤트 중복 방지
    $(".word-table td input[name='word']").on("click", function (e) {
        e.stopPropagation();
    });

    // 단어 추가 버튼 클릭 이벤트
    $("#add-words-btn").on("click", function (e) {
        const selectedCount = $("input[name='word']:checked").length; // 선택된 체크박스 갯수
        const selectedWordbook = $("#final-wordBookId").val().trim(); // 선택된 단어장 ID 확인
        const selectedWordBookName = $("#now-wordbook").text().trim();
        console.log("단어 체크 : " + selectedCount)
        console.log("뜻 체크 : " + $("input[name='meaning']:checked").length)
        if (!selectedWordbook) {
            // 단어장이 선택되지 않은 경우
            alert("단어장을 먼저 선택해 주세요.");
            e.preventDefault(); // form 제출 방지
        } else if (selectedCount === 0) {
            // 선택된 단어가 없을 경우
            alert("단어를 선택해 주세요.");
            e.preventDefault(); // form 제출 방지
        } else {
            // 선택된 단어가 있을 경우 확인 메시지
            const confirmMessage = `"${selectedWordBookName}"에 ${selectedCount}개의 단어를 추가하시겠습니까?`;
            if (!confirm(confirmMessage)) {
                e.preventDefault(); // 확인 취소 시 form 제출 방지
            }
        }
    });

    // 초기 선택 갯수 표시
    updateSelectedCount();

    // 체크 상태 동기화 (전체 선택과 선택 해제는 독립적임)
    $(".compile-result-voca").change(function () {
        const allChecked = $(".compile-result-voca").length === $(".compile-result-voca:checked").length;
        const anyUnchecked = $(".compile-result-voca:checked").length === 0;

        if (allChecked) {
            $("#all-checked").prop("checked", true);
            $("#all-unchecked").prop("checked", false);
        } else if (anyUnchecked) {
            $("#all-checked").prop("checked", false);
            $("#all-unchecked").prop("checked", true);
        } else {
            $("#all-checked").prop("checked", false);
            $("#all-unchecked").prop("checked", false);
        }
    }); // voca result change

    // 체크박스 클릭 시 이벤트 중복 방지
    $(".word-table td input[type='checkbox']").on("click", function (e) {
        e.stopPropagation(); // 이벤트 버블링 차단
    });// td checkbox on click

    // [ 토글&마우스오버시 뜻 ]
    const $tooltip = $(".mouse-cursor"); // 툴팁 요소

    // 뜻 표시 활성화 함수
    function enableMeaning() {
        $(".word-table td").on("mouseenter", function (e) {
            const $vocabulary = $(this).find(".vocabulary"); // 단어 요소
            const meaning = $vocabulary.data("meaning"); // 뜻 가져오기

            if (meaning) {
                $tooltip.text(meaning); // 툴팁에 뜻 설정
                $tooltip.show(); // 툴팁 표시
            }
        });

        $(".word-table td").on("mousemove", function (e) {
            $tooltip.css({
                left: e.pageX + 10 + "px", // 마우스 X좌표 + 10px
                top: e.pageY + 10 + "px"  // 마우스 Y좌표 + 10px
            });
        });

        $(".word-table td").on("mouseleave", function () {
            $tooltip.hide(); // 툴팁 숨김
        });
    } // fn enableMeaning

    // 뜻 표시 비활성화 함수
    function disableMeaning() {
        $(".word-table td").off("mouseenter mousemove mouseleave");
        $tooltip.hide(); // 툴팁 숨김
    } // fn disableMeaning()

    // 초기 토글 상태 확인 후 적용
    if ($("#toggle").is(":checked")) {
        enableMeaning(); // 기본 활성화
    }// toggle is

    // 토글 스위치 상태 변경 처리
    $("#toggle").change(function () {
        if ($(this).is(":checked")) {
            enableMeaning();
        } else {
            disableMeaning();
        }
    }); //toggle change

});
