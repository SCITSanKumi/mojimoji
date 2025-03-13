$(document).ready(function () {
  var page = 1; // 초기 렌더링은 서버에서 처리됨 (0페이지). 이후 AJAX는 페이지 1부터 로드
  var size = 8; // 한 번에 불러올 데이터 수
  var loading = false; // AJAX 요청 중복 실행 방지
  var noMoreData = false; // 더 이상 불러올 데이터가 없음을 체크

  // 디바운스 함수: 연속되는 이벤트 호출을 제한하여 성능을 개선
  function debounce(func, delay) {
    let timer;
    return function () {
      const context = this,
        args = arguments;
      clearTimeout(timer);
      timer = setTimeout(() => func.apply(context, args), delay);
    };
  }

  // 내 스토리 무한 스크롤로 불러오기 위한 AJAX 함수
  function loadMyStories() {
    if (loading || noMoreData) return;
    loading = true;
    $("#loading").text("Loading...").show();

    $.ajax({
      url: "/board/myStory/ajaxList",
      type: "GET",
      dataType: "json",
      data: {
        page: page,
        size: size
      },
      success: function (data) {
        // 만약 응답 데이터가 HTML이라면 로그인 페이지로 리다이렉트 처리
        if (typeof data === "string" && data.trim().startsWith("<!DOCTYPE")) {
          window.location.href = "/user/login";
          return;
        }
        // 반환된 데이터 개수가 요청한 size보다 작으면 더 이상 불러올 데이터가 없다고 판단
        if (data.length < size) {
          noMoreData = true;
          $("#loading").text("마지막 페이지 입니다.");
        }
        // 각 내 스토리 항목에 대해 카드 HTML 생성 및 추가
        $.each(data, function (index, myStory) {
          // 공유되지 않은 경우에만 공유 버튼 생성
          var shareBtn = "";
          if (!myStory.isShared) {
            shareBtn = '<button type="button" class="btn btn-primary btn-sm me-2" onclick="shareStory(' + myStory.bookId + ')">공유</button>';
          }
          // 삭제 버튼은 항상 표시
          var deleteBtn = '<button type="button" class="btn btn-danger btn-sm" onclick="deleteStory(' + myStory.bookId + ')">삭제</button>';

          // story.createdAt 예시: "2023-05-12T10:20:30Z"
          var rawDate = myStory.createdAt;

          // 1) Date 객체로 변환
          var dateObj = new Date(rawDate);

          // 2) 연, 월, 일 추출
          var year = dateObj.getFullYear();
          var month = String(dateObj.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
          var day = String(dateObj.getDate()).padStart(2, '0');

          // 3) yyyy-MM-dd 형태로 조합
          var createdAtFormatted = `${year}-${month}-${day}`;

          var thumbnailUrl = (myStory.thumbnailUrl && myStory.thumbnailUrl !== "null") ?
            myStory.thumbnailUrl :
            '/image/mountains.png';

          var profileUrl = (myStory.profileUrl && myStory.profileUrl !== "null") ?
            myStory.profileUrl :
            'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png';

          var storyHtml = '<div class="col">' +
            '<div class="card">' +
            '<a href="/board/story/detail?bookId=' +
            myStory.bookId +
            '"class="text-decoration-none">' +
            '<div class="card-thumbnail-container">' +
            '<img src="' +
            thumbnailUrl +
            '"alt= "Card image" class="card-thumbnail">' +
            '</div>' +
            '<div class="card-content">' +
            '<h2 class="card-title">' +
            myStory.title +
            '</h2>' +
            '<div class="author-row">' +
            '<img src="' +
            profileUrl +
            '"alt="Author" class="author-avatar">' +
            '<span class="author-name">' +
            myStory.nickname +
            '</span>' +
            '</div>' +
            '<div class="card-stats">' +
            '<span class="stat date-stat">' +
            '<i class="fas fa-calendar-alt stat-icon"></i>' +
            '<span>' +
            createdAtFormatted +
            '</span>' +
            '</span>' +
            shareBtn + deleteBtn +
            '</div>' +
            '</div>' +
            '</a>' +
            '</div>' +
            '</div>';
          $(".card-container").append(storyHtml);
        });
        page++;
        loading = false;
        if (!noMoreData) {
          $("#loading").hide();
        }
      },
      error: function (err) {
        console.error("AJAX 요청 에러:", err);
        loading = false;
        $("#loading").hide();
      }
    });
  }

  // 스크롤 이벤트에 디바운스 적용 (200ms 딜레이)
  $(window).scroll(debounce(function () {
    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
      loadMyStories();
    }
  }, 200));
});

// 공유 버튼 클릭 시 호출되는 함수
function shareStory(bookId) {
  $.ajax({
    url: '/board/myStory/share',
    type: 'POST',
    data: {
      bookId: bookId
    },
    success: function (response) {
      alert('공유되었습니다.');
      window.location.href = '/board/story/list';
    },
    error: function (xhr) {
      alert('공유 실패: ' + xhr.responseText);
    }
  });
}

// 삭제 버튼 클릭 시 호출되는 함수
function deleteStory(bookId) {
  if (!confirm('정말 삭제하시겠습니까?')) return;
  $.ajax({
    url: '/board/myStory/delete',
    type: 'DELETE',
    data: {
      bookId: bookId
    },
    success: function (response) {
      alert('삭제되었습니다.');
      window.location.href = '/board/myStory/list';
    },
    error: function (xhr) {
      alert('삭제 실패: ' + xhr.responseText);
    }
  });
}