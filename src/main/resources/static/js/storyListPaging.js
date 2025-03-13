$(document).ready(function () {
  var page = 1; // 초기 렌더링은 서버에서 처리됨 (0페이지). 이후 AJAX는 1페이지부터 로드
  var size = 8;
  var loading = false;
  var noMoreData = false;

  // 디바운스 함수: 스크롤 이벤트 호출 빈도를 줄임
  function debounce(func, delay) {
    let timer;
    return function () {
      const context = this,
        args = arguments;
      clearTimeout(timer);
      timer = setTimeout(() => func.apply(context, args), delay);
    };
  }

  function loadStories() {
    if (loading || noMoreData) return;
    loading = true;
    $("#loading").text("Loading...").show();

    $.ajax({
      url: "/board/story/ajaxList",
      type: "GET",
      dataType: "json",
      data: {
        page: page,
        size: size,
        searchWord: $("#searchWord").val(),
        searchItem: $("#searchItem").val(),
        sortOption: $("#sortOption").val(),
      },
      success: function (data) {
        // 만약 응답이 HTML이라면 로그인 페이지로 리다이렉트
        if (typeof data === "string" && data.trim().startsWith("<!DOCTYPE")) {
          window.location.href = "/user/login";
          return;
        }
        // 데이터 개수가 요청 사이즈보다 작으면 더 이상 불러올 데이터가 없음
        if (data.length < size) {
          noMoreData = true;
          $("#loading").text("마지막 페이지 입니다.");
        }
        // 각 스토리에 대해 카드 HTML 생성 및 추가
        $.each(data, function (index, story) {

          // story.createdAt 예시: "2023-05-12T10:20:30Z"
          var rawDate = story.createdAt;

          // 1) Date 객체로 변환
          var dateObj = new Date(rawDate);

          // 2) 연, 월, 일 추출
          var year = dateObj.getFullYear();
          var month = String(dateObj.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1
          var day = String(dateObj.getDate()).padStart(2, '0');

          // 3) yyyy-MM-dd 형태로 조합
          var createdAtFormatted = `${year}-${month}-${day}`;
          // story.thumbnailUrl이 falsy하거나 "null" 문자열이면 fallback URL 사용
          var thumbnailUrl = (story.thumbnailUrl && story.thumbnailUrl !== "null") ?
            story.thumbnailUrl :
            '/image/mountains.png';

          var profileUrl = (story.profileUrl && story.profileUrl !== "null") ?
            story.profileUrl :
            'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png';


          var storyHtml = '<div class="col">' +
            '<div class="card">' +
            '<a href="/board/story/detail?bookId=' +
            story.bookId +
            '"class="text-decoration-none">' +
            '<div class="card-thumbnail-container">' +
            '<img src="' +
            thumbnailUrl +
            '"alt= "Card image" class="card-thumbnail">' +
            '</div>' +
            '<div class="card-content">' +
            '<h2 class="card-title">' +
            story.title +
            '</h2>' +
            '<div class="author-row">' +
            '<img src="' +
            profileUrl +
            '"alt="Author" class="author-avatar">' +
            '<span class="author-name">' +
            story.nickname +
            '</span>' +
            '</div>' +
            '<div class="card-stats">' +
            '<span class="stat date-stat">' +
            '<i class="fas fa-calendar-alt stat-icon"></i>' +
            '<span>' +
            createdAtFormatted +
            '</span>' +
            '</span>' +
            '<span class="stat"><i class="fas fa-heart stat-icon"></i><span>' +
            story.gaechu +
            '</span></span>' +
            '<span class="stat"><i class="fas fa-eye stat-icon"></i><span>' +
            story.hitCount +
            '</span></span>' +
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
      loadStories();
    }
  }, 200));

  // 내 스토리로 이동
  $("#myStoryBtn").click(function () {
    window.location.href = "/board/myStory/list";
  });
});