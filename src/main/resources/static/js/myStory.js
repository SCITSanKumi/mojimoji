// 공유 버튼 클릭 시 호출되는 함수
function shareStory(bookId) {
    $.ajax({
        url: '/board/myStory/share',
        type: 'POST',
        data: { bookId: bookId },
        success: function (response) {
            alert('공유되었습니다.');
            location.reload();
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
            location.reload();
        },
        error: function (xhr) {
            alert('삭제 실패: ' + xhr.responseText);
        }
    });
}