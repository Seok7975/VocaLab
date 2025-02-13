$(document).ready(function () {
    // 드롭다운 메뉴 이벤트
    $(".dropdown").on("mouseenter", function () {
        $(this).find(".dropdown-menu").stop(true, true).slideDown(200); // 드롭다운 표시
    });

    $(".dropdown").on("mouseleave", function () {
        $(this).find(".dropdown-menu").stop(true, true).slideUp(200); // 드롭다운 숨김
    });

});