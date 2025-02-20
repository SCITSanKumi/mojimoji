$(document).ready(function () {
    var sharedBookId = $("#sharedBookId").val();
    var currentUserId = $("#currentUserId").val(); // 현재 로그인한 사용자의 ID

    // 댓글 목록을 불러오는 함수
    function loadComments() {
        $.ajax({
            url: "/board/story/comment",
            type: "GET",
            data: { sharedBookId: sharedBookId },
            success: function (comments) {
                var list = $("#comment-list");
                list.empty(); // 기존 댓글 목록 초기화
                $.each(comments, function (index, comment) {
                    // 댓글 항목 생성
                    var listItem = $("<li class='list-group-item'></li>");
                    listItem.append("<strong>" + comment.nickname + ":</strong> " + comment.content);

                    // 현재 로그인한 사용자가 댓글 작성자인 경우에만 삭제 버튼 추가
                    if (comment.userId == currentUserId) {
                        var deleteBtn = $("<button class='btn btn-sm btn-danger float-right'>삭제</button>");
                        deleteBtn.click(function () {
                            $.ajax({
                                url: "/board/story/comment?sharedBookReplyId=" + comment.sharedBookReplyId,
                                type: "DELETE",
                                success: function () {
                                    listItem.remove();
                                },
                                error: function (err) {
                                    console.error("댓글 삭제 실패:", err);
                                }
                            });
                        });
                        listItem.append(deleteBtn);
                    }
                    list.append(listItem);
                });
            },
            error: function (err) {
                console.error("댓글 불러오기 실패:", err);
            }
        });
    }

    // 초기 댓글 목록 로드
    loadComments();

    // 댓글 등록 버튼 클릭 이벤트 핸들러
    $("#add-comment").click(function () {
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
            success: function (response) {
                $("#comment-input").val(""); // 입력창 초기화
                loadComments();              // 댓글 목록 새로고침
            },
            error: function (err) {
                console.error("댓글 등록 실패:", err);
            }
        });
    });
});
