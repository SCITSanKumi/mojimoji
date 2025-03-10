$(document).ready(function () {
    // 무한 스크롤 변수 설정
    var page = 1;
    var loading = false;
    var userId = $("#currentUserId").val();

    function loadMoreStories() {
        if (loading) return;
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
                        var storyHtml = `
                            <div class="col">
                                <div class="card card-cover h-100 overflow-hidden text-bg-dark rounded-4 shadow-lg" 
                                     style="background-image: url(${story.thumbnailUrl ? story.thumbnailUrl : '/image/mountains.png'}); background-repeat: no-repeat; background-size: contain; background-position: center;">
                                    <div class="d-flex flex-column h-100 p-5 pb-3 text-white text-shadow-1">
                                        <h3 class="pt-5 mt-5 mb-4 display-6 lh-1 fw-bold">
                                            <a href="/board/story/detail?bookId=${story.bookId}" class="text-white text-decoration-none">${story.title}</a>
                                        </h3>
                                        <h5>${story.nickname}</h5>
                                        <ul class="d-flex list-unstyled mt-auto">
                                            <li class="me-auto">
                                                <img src="${story.profileUrl ? story.profileUrl : '/image/logo.png'}" alt="Profile" width="32" height="32" class="rounded-circle border border-white">
                                            </li>
                                            <li class="d-flex align-items-center me-3">
                                                <small>조회수</small>
                                            </li>
                                            <li class="d-flex align-items-center">
                                                <small>${story.hitCount}</small>
                                            </li>
                                        </ul>
                                        <h5>${story.createdAt}</h5>
                                        <h5>${story.gaechu}</h5>
                                    </div>
                                </div>
                            </div>
                        `;
                        $("#story-container").append(storyHtml);
                    });
                    page++; // 다음 페이지로 증가
                }
                loading = false;
                $("#loading").hide();
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