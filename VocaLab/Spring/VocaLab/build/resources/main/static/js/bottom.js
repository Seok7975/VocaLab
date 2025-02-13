$(document).ready(function () {
    // 고객센터 클릭 시 href 속성으로 이동
    $(".bottom-content a[href='cs']").on("click", function (e) {
        e.preventDefault();
        alert("고객센터 페이지로 이동합니다.");
        window.location.href = $(this).attr("href");
    });

    // 추가적인 이벤트를 이곳에 작성 가능
});
