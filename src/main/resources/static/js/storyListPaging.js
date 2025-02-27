$(document).ready(function () {
    var page = 1; // 초기 렌더링은 서버에서 처리됨 (0페이지). 이후 AJAX는 1페이지부터 로드
    var size = 6;
    var loading = false;
    var noMoreData = false;

    // 디바운스 함수: 스크롤 이벤트 호출 빈도를 줄임
    function debounce(func, delay) {
        let timer;
        return function () {
            const context = this, args = arguments;
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
                    var storyHtml = '<div class="col">' +
                        '<div class="card card-cover h-100 overflow-hidden text-bg-dark rounded-4 shadow-lg" style="background-image: url(' + story.thumbnailUrl + ');">' +
                        '<div class="d-flex flex-column h-100 p-5 pb-3 text-white text-shadow-1">' +
                        '<h3 class="pt-5 mt-5 mb-4 display-6 lh-1 fw-bold">' +
                        '<a href="/board/story/detail?bookId=' + story.bookId + '" class="text-white text-decoration-none">' + story.title + '</a>' +
                        '</h3>' +
                        '<h5>' + story.nickname + '</h5>' +
                        '<ul class="d-flex list-unstyled mt-auto">' +
                        '<li class="me-auto"><img src="' + story.profileUrl + '" alt="Profile" width="32" height="32" class="rounded-circle border border-white"></li>' +
                        '<li class="d-flex align-items-center me-3"><small>조회수</small></li>' +
                        '<li class="d-flex align-items-center"><small>' + story.hitCount + '</small></li>' +
                        '</ul>' +
                        '<h5>' + story.createdAt + '</h5>' +
                        '<h5>' + story.gaechu + '</h5>' +
                        '</div>' +
                        '</div>' +
                        '</div>';
                    $("#story-container").append(storyHtml);
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
