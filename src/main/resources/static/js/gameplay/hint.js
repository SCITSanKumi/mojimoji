$(document).ready(function () {
  $("#inventory-button").click(function () {
    $("#inventory-panel").toggle();
  });

  // 힌트 버튼 클릭 시 힌트 박스 토글
  $("#hint-btn").click(function () {
    $("#hint-box").toggle();
  });

  // 한자 카드 드래그 설정
  $(".kanji-card").draggable({
    appendTo: "body",
    helper: "clone",
    scroll: false,
    zIndex: 2000,
    revert: function (dropped) {
      // 유효한 드롭 영역에 놓이면 revert하지 않음
      return !dropped;
    },
    revertDuration: 500,
    start: function (event, ui) {
      $(this).css("visibility", "hidden"); // 드래그 시작 시 원본 숨김
      ui.helper.css("z-index", 3000);
      $(this).removeData("dropped"); // dropped 플래그 초기화
    },
    stop: function (event, ui) {
      const $this = $(this);
      // 드롭된 경우 0.2초 후에 원래 위치에서 보이도록 (애니메이션 효과 없이)
      if ($this.data("dropped")) {
        setTimeout(function () {
          $this.css("visibility", "visible");
        }, 200);
      } else {
        $this.css("visibility", "visible");
      }
    }
  });

  // 채팅 영역을 드롭 영역으로 설정
  $(".chat-container").droppable({
    accept: ".kanji-card",

    over: function (event, ui) {
      $(this).addClass("over");
    },

    out: function (event, ui) {
      $(this).removeClass("over");
    },

    drop: function (event, ui) {
      // 1) 드롭영역 스타일 초기화
      $(this).removeClass("over");

      // 2) 드롭된 카드에 dropped 플래그 설정
      ui.draggable.data("dropped", true);

      // 선택된 한자 추출
      let selectedKanji = ui.draggable.find(".kanji-character").text();


      // 4) Confetti 효과
      confetti({
        particleCount: 80,
        spread: 70,
        origin: { y: 0.6 }
      });

      // 5) "한자 카드 사용" 메시지를 화면 중앙에 표시
      const usedMsg = $(`
        <div class="hint-used-message animate__animated animate__fadeInUp"
             style="
               position: fixed;
               top: 50%;
               left: 50%;
               transform: translate(-50%, -50%);
               background-color: rgba(255, 255, 255, 0.95);
               padding: 20px;
               border: 1px solid #ccc;
               border-radius: 8px;
               z-index: 9999;
               text-align: center;
             ">
          <strong>한자 카드 [${selectedKanji}] 사용!</strong><br>
          힌트가 적용되었습니다.
        </div>
      `);
      $("body").append(usedMsg);

      // 1.5초 후 사라지는 애니메이션
      setTimeout(() => {
        usedMsg.removeClass("animate__fadeInUp").addClass("animate__fadeOutDown");
        // 0.8초 후 DOM 제거
        setTimeout(() => {
          usedMsg.remove();
        }, 800);
      }, 1500);

      // 6) AJAX 요청 (기존 로직)
      $.ajax({
        url: `/game/hint/${bookId}`,
        method: "GET",
        data: { kanji: selectedKanji },
        success: function (response) {
          $("#user-input").val(response);
        },
        error: function (e) {
          alert("힌트 요청 중 오류 발생");
        }
      });
    }
  });
});
