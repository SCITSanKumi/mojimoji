$(document).ready(function() {
    var sharedBookId = $("#sharedBookId").val();
    var currentUserId = $("#currentUserId").val(); // 현재 로그인한 사용자의 ID

    var page = 0; // 현재 로드한 페이지 번호
    var size = 10; // 한 번에 불러올 댓글 수
    var loading = false; // AJAX 요청 중복 방지
    var noMoreComments = false; // 더 이상 불러올 댓글이 없음을 체크

    function loadComments(reset = false) {
        if (loading || noMoreComments) return;
        loading = true;

        // reset이 true면 페이지 초기화 및 기존 목록 삭제
        if (reset) {
            page = 0;
            noMoreComments = false;
            $("#comment-list").empty();
        }

        $.ajax({
            url: "/board/story/comment",
            type: "GET",
            data: {
                sharedBookId: sharedBookId,
                page: page,
                size: size
            },
            success: function(comments) {
                if (comments.length < size) {
                    noMoreComments = true;
                    $("#load-more-button").hide(); // 더보기 버튼 숨기기
                } else {
                    $("#load-more-button").show(); // 다음 댓글이 있을 수 있으므로 버튼 보이기
                }

                var list = $("#comment-list");
                $.each(comments, function(index, comment) {
                    // 댓글 항목 생성
                    var listItem = $("<li class='list-group-item'></li>")
                        .attr("data-comment-id", comment.sharedBookReplyId)
                        .attr("data-user-id", comment.userId);

                    // 댓글 텍스트 부분
                    var commentText = $("<div class='comment-text'></div>")
                        .html("<strong>" + comment.nickname + ":</strong> " + comment.content);
                    listItem.append(commentText);

                    // 삭제 버튼 (현재 로그인한 사용자가 댓글 작성자인 경우에만)
                    if (comment.userId == currentUserId) {
                        var deleteBtn = $("<button class='btn btn-sm btn-danger'>삭제</button>");
                        deleteBtn.click(function() {
                            $.ajax({
                                url: "/board/story/comment?sharedBookReplyId=" + comment.sharedBookReplyId,
                                type: "DELETE",
                                success: function() {
                                    listItem.remove();
                                },
                                error: function(err) {
                                    console.error("댓글 삭제 실패:", err);
                                }
                            });
                        });
                        listItem.append(deleteBtn);
                    }
                    list.append(listItem);
                });

                page++; // 다음 페이지 번호
                loading = false;
            },
            error: function(err) {
                console.error("댓글 불러오기 실패:", err);
                loading = false;
            }
        });
    }

    // 초기 댓글 목록 로드
    loadComments();

    // "더보기" 버튼 클릭 시 다음 페이지 로드
    $("#load-more-button").click(function() {
        loadComments(false);
    });

    // 댓글 등록 버튼 클릭 이벤트 핸들러
    $("#add-comment").click(function() {
        var commentContent = $("#comment-input").val().trim();
        if (commentContent === "") {
            alert("댓글을 입력해주세요.");
            return;
        }
        $.ajax({
            url: "/board/story/comment",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify({
                sharedBookId: sharedBookId,
                content: commentContent
            }),
            success: function(newComment) {
                $("#comment-input").val(""); // 입력창 초기화

                // 새 댓글을 맨 위에 추가 (prepend)
                var listItem = $("<li class='list-group-item'></li>")
                    .attr("data-comment-id", newComment.sharedBookReplyId)
                    .attr("data-user-id", newComment.userId);

                var commentText = $("<div class='comment-text'></div>")
                    .html("<strong>" + newComment.nickname + ":</strong> " + newComment.content);
                listItem.append(commentText);

                // 삭제 버튼 생성 (작성자 본인이므로 추가)
                var deleteBtn = $("<button class='btn btn-sm btn-danger'>삭제</button>");
                deleteBtn.click(function() {
                    $.ajax({
                        url: "/board/story/comment?sharedBookReplyId=" + newComment.sharedBookReplyId,
                        type: "DELETE",
                        success: function() {
                            listItem.remove();
                        },
                        error: function(err) {
                            console.error("댓글 삭제 실패:", err);
                        }
                    });
                });
                listItem.append(deleteBtn);

                // 최상단에 새 댓글 추가 및 스크롤 최상단으로 이동
                $("#comment-list").prepend(listItem);
                $("#comment-list").scrollTop(0);
            },
            error: function(err) {
                console.error("댓글 등록 실패:", err);
            }
        });
    });

    // 추천 수 토글 기능
    $('#like-button').click(function() {
        $.ajax({
            url: '/board/story/like',
            type: 'POST',
            data: {
                sharedBookId: sharedBookId
            },
            success: function(response) {
                // 추천 수 업데이트
                $('#like-count').text(response.gaechu);
                // 추천 상태에 따라 하트 아이콘 변경: liked가 true이면 채워진 하트(♥), 아니면 빈 하트(♡)
                $('#Give-It-An-Id').prop('checked', response.liked);
            },
            error: function(err) {
                console.error("추천 토글 실패", err);
                alert(err.responseText);
            }
        });
    });
});

// 삭제 버튼 클릭 시 호출되는 함수
function deleteStory(bookId) {
    if (!confirm('정말 삭제하시겠습니까?')) return;
    $.ajax({
        url: '/board/myStory/delete',
        type: 'DELETE',
        data: {
            bookId: bookId
        },
        success: function(response) {
            alert('삭제되었습니다.');
            window.location.href = '/board/story/list';
        },
        error: function(xhr) {
            alert('삭제 실패: ' + xhr.responseText);
        }
    });
}