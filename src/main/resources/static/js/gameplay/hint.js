$(document).ready(function () {
    document.addEventListener('click', function(event) {
        var hintBox = document.getElementById('hint-box');
        var hintBtn = document.getElementById('hint-btn');
        if (!hintBox.contains(event.target) && !hintBtn.contains(event.target)) {
            hintBox.style.display = 'none';
        }
    });

    // 힌트 버튼
    $("#hint-btn").click(function () {
        $("#hint-box").toggle();
    });

    // 카드 드래그
    $(".kanji-card").draggable({
        appendTo: "body",
        helper: "clone",
        scroll: false,
        zIndex: 2000,
        revert: function (dropped) {
            return !dropped;
        },
        revertDuration: 500,
        start: function (event, ui) {
            $(this).css("visibility", "hidden");
            ui.helper.css("z-index", 3000);
            $(this).removeData("dropped");
        },
        stop: function (event, ui) {
            const $this = $(this);
            if ($this.data("dropped")) {
                setTimeout(function () {
                    $this.css("visibility", "visible");
                }, 200);
            } else {
                $this.css("visibility", "visible");
            }
        }
    });

    // 드롭
    $(".chat-container").droppable({
        accept: ".kanji-card",

        over: function (event, ui) {
            $(this).addClass("over");
        },
        out: function (event, ui) {
            $(this).removeClass("over");
        },

        drop: function (event, ui) {
            // 드롭영역 해제
            $(this).removeClass("over");
            ui.draggable.data("dropped", true);

            // 드롭된 한자 추출
            let selectedKanji = ui.draggable.find(".kanji-character").text();

            // 입력창에 힌트카드 사용 문구 (로딩중)
            $("#user-input").val(`힌트카드 [${selectedKanji}] 사용! 힌트가 적용되었습니다`);

            // (A) 전 화면 중앙에 .textWrapper 표시
            $(".loader")
                .css({ display: "flex" }) // 보이게
                .appendTo("body");        // body에 append → 화면 중앙

            // (B) AJAX
            $.ajax({
                url: `/game/hint/${bookId}`,
                method: "GET",
                data: { kanji: selectedKanji },
                success: function (response) {
                    // 입력창에 실제 힌트


                    // (C) .textWrapper fadeOut 후 제거
                    $(".loader").fadeOut(100, function () {
                        $(this).hide();
                        $("#user-input").val(response);
                    });
                },
                error: function (e) {
                    $("#user-input").val(`힌트카드 [${selectedKanji}] 사용 중 오류 발생...`);
                    alert("힌트 요청 중 오류 발생");
                    // 로딩 표시 제거
                    $(".loader").fadeOut(300, function () {
                        $(this).hide();
                    });
                }
            });
        }
    });
});
