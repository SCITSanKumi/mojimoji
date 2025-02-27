$(document).ready(function () {
    var page = 1; // 초기 렌더링은 서버에서 처리됨 (0페이지). 이후 AJAX는 페이지 1부터 로드
    var size = 6; // 한 번에 불러올 데이터 수
    var loading = false; // AJAX 요청 중복 실행 방지
    var noMoreData = false; // 더 이상 불러올 데이터가 없음을 체크

    // 디바운스 함수: 연속되는 이벤트 호출을 제한하여 성능을 개선
    function debounce(func, delay) {
        let timer;
        return function () {
            const context = this, args = arguments;
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

                    var thumbnailUrl = (myStory.thumbnailUrl && myStory.thumbnailUrl !== "null")
                        ? myStory.thumbnailUrl
                        : '/image/mountains.png';

                    var profileUrl = (myStory.profileUrl && myStory.profileUrl !== "null")
                        ? myStory.profileUrl
                        : '/image/logo.png';

                    var myStoryHtml = '<div class="col">' +
                        '<div class="card card-cover h-100 overflow-hidden text-bg-dark rounded-4 shadow-lg" ' +
                        'style="background-image: url(' + thumbnailUrl + '); background-repeat: no-repeat; background-size: contain; background-position: center;">' +
                        '<div class="d-flex flex-column h-100 p-5 pb-3 text-white text-shadow-1 position-relative">' +
                        // 공유/삭제 버튼 영역
                        '<div class="position-absolute top-0 end-0 p-2">' +
                        '<div class="d-flex">' +
                        shareBtn + deleteBtn +
                        '</div>' +
                        '</div>' +
                        '<h3 class="pt-5 mt-5 mb-4 display-6 lh-1 fw-bold">' +
                        '<a href="/board/myStory/detail?bookId=' + myStory.bookId + '" class="text-white text-decoration-none">' + myStory.title + '</a>' +
                        '</h3>' +
                        '<h5>' + myStory.nickname + '</h5>' +
                        '<ul class="d-flex list-unstyled mt-auto">' +
                        '<li class="me-auto"><img src="' + profileUrl + '" alt="Profile" width="32" height="32" class="rounded-circle border border-white"></li>' +
                        '<li class="d-flex align-items-center me-3"><small>조회수</small></li>' +
                        '<li class="d-flex align-items-center"><small>' + myStory.hitCount + '</small></li>' +
                        '</ul>' +
                        '</div>' +
                        '</div>' +
                        '</div>';
                    $("#myStory-container").append(myStoryHtml);
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
        data: { bookId: bookId },
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
        data: { bookId: bookId },
        success: function (response) {
            alert('삭제되었습니다.');
            window.location.href = '/board/myStory/list';
        },
        error: function (xhr) {
            alert('삭제 실패: ' + xhr.responseText);
        }
    });
}
