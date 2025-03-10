$(document).ready(function () {
    // 모달 이벤트 처리 (포커스 관리)
    $('#changePasswordModal, #deleteAccountModal').on('shown.bs.modal', function () {
        $(this).removeAttr('inert').find('input, button').first().focus();
    }).on('hidden.bs.modal', function () {
        document.activeElement && document.activeElement.blur();
        $('#profileUpdateBtn').focus();
    });

    // 프로필 이미지 클릭 시 파일 선택창 열기
    $('#profileImageContainer').on('click', function () {
        $('#profileImageInput').click();
    });

    // 프로필 이미지 변경 시 미리보기
    $('#profileImageInput').on('change', function (event) {
        const file = event.target.files[0];
        if (file) {
            if (file.size > 100 * 1024 * 1024) { // 100MB 제한
                alert("파일 크기가 100MB를 초과할 수 없습니다.");
                $(this).val(""); // 파일 선택 취소
                return;
            }

            if (file.type.startsWith('image/')) {
                const imageURL = URL.createObjectURL(file);
                $('#profileImage').attr('src', imageURL);
            }
        }
    });

    // 드래그앤드롭 이벤트 처리
    $('#profileImageContainer').on('dragover', function (event) {
        event.preventDefault();
        $(this).addClass('dragover');
    }).on('dragleave drop', function (event) {
        event.preventDefault();
        $(this).removeClass('dragover');

        if (event.type === 'drop') {
            const file = event.originalEvent.dataTransfer.files[0];
            if (file) {
                $('#profileImageInput')[0].files = event.originalEvent.dataTransfer.files;
                if (file.type.startsWith('image/')) {
                    const imageURL = URL.createObjectURL(file);
                    $('#profileImage').attr('src', imageURL);
                }
            }
        }
    });

    // 프로필 수정 폼 AJAX 전송
    $("#profileUpdateForm").on('submit', function (e) {
        e.preventDefault();
        let formData = new FormData(this);

        $.ajax({
            url: $(this).attr('action'),
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function() {
                alert("프로필이 변경되었습니다.");
            },
            error: function() {
                alert("프로필 업데이트 실패");
            }
        });
    });

    // 비밀번호 변경 버튼 클릭 이벤트
    $("#passwordUpdateBtn").click(function () {
        const data = {
            currentPassword: $("#currentPassword").val(),
            newPassword: $("#newPassword").val(),
            confirmNewPassword: $("#confirmNewPassword").val()
        };

        if (data.newPassword !== data.confirmNewPassword) {
            alert("새 비밀번호가 일치하지 않습니다.");
            return;
        }

        $.ajax({
            url: "/user/update/password",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (result) {
                if (result) {
                    alert("비밀번호 변경 성공");
                    window.location.href = "/user/login";
                } else {
                    alert("현재 비밀번호를 확인해주세요.");
                }
            },
            error: function () {
                alert("서버 통신 오류");
            }
        });
    });

    // 계정 삭제 버튼 클릭 이벤트
    $(".delete-account-btn").click(function () {
        if (confirm("정말로 계정을 삭제하시겠습니까? 이 작업은 복구할 수 없습니다.")) {
            $.ajax({
                url: "/user/delete",
                type: "POST",
                contentType: "application/json",
                success: function (result) {
                    if (result) {
                        alert("계정이 삭제되었습니다.");
                        window.location.href = "/user/logout";
                    } else {
                        alert("계정 삭제에 실패했습니다.");
                    }
                },
                error: function () {
                    alert("서버 통신 오류");
                }
            });
        }
    });
});
