$(document).ready(function () {
  // 무한 스크롤 변수 설정
  var page = 1;
  var loading = false;
  var lastPage = false;
  var userId = $("#currentUserId").val();

  function loadMoreStories() {
    if (loading || lastPage) return;
    loading = true;
    $("#loading").show();
    $.ajax({
      url: '/board/otherStory/ajaxList',
      method: 'GET',
      data: {
        userId: userId,
        page: page,
        size: 8
      },
      success: function (data) {
        if (data && data.length > 0) {
          data.forEach(function (story) {

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
          page++; // 다음 페이지로 증가
          $("#loading").hide();
        } else {
          lastPage = true;
          // 마지막 페이지 메시지를 보여주고, hide() 호출하지 않음.
          $("#loading").text('마지막 페이지입니다.').show();
        }
        loading = false;
      },
      error: function () {
        loading = false;
        $("#loading").hide();
      }
    });
  }

  $("#whiteButton").click(function () {
    $("#whiteContent").show();
    $("#blackContent").hide();
    $("#loading").hide().text('');
  });
  $("#blackButton").click(function (e) {
    e.preventDefault(); // 기본 동작 방지
    userId = $("#currentUserId").val();
    var url = '/kanji/otherCollection?userId=' + userId; // AJAX 호출 URL 생성
    $.get(url, function (response) {
      $("#blackContent").html(response); // 받아온 HTML 삽입
      $("#blackContent").show(); // 검은 콘텐츠 영역 보이기
      $("#whiteContent").hide(); // 흰색 콘텐츠 숨기기
    });
  });

  // 초기 상태: 흰색 콘텐츠 보이기
  $("#whiteContent").show();
  $("#blackContent").hide();

  // 스크롤 이벤트: 창의 스크롤이 문서 하단 근처에 도달하면 loadMoreStories() 호출
  $(window).scroll(function () {
    if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100) {
      loadMoreStories();
    }
  });
});
