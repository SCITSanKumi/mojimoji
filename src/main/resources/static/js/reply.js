$(document).ready(function () {
    var sharedBookId = $("#sharedBookId").val();

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
                    list.append(
                        "<li class='list-group-item'><strong>" + comment.nickname + ":</strong> " + comment.content + "</li>"
                    );
                });
            },
            error: function (err) {
                console.error("댓글 불러오기 실패:", err);
            }
        });
    }

    loadComments();

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
