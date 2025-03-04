$(document).ready(function () {
    $('#changePasswordModal').on('shown.bs.modal', function () {
        $(this).removeAttr('inert');
        $(this).find('input, button').first().focus();
    });

    $('#changePasswordModal').on('hidden.bs.modal', function () {
        if (document.activeElement) {
            document.activeElement.blur();
        }
        $('#profileUpdateBtn').focus();
    });

    $('#deleteAccountModal').on('shown.bs.modal', function () {
        $(this).removeAttr('inert');
        $(this).find('button, input').first().focus();
    });
 
    $('#deleteAccountModal').on('hidden.bs.modal', function () {
        if (document.activeElement) {
            document.activeElement.blur();
        }
        $('#profileUpdateBtn').focus();
    });

    // 프로필 수정 버튼 클릭 이벤트
    $("#profileUpdateBtn").click(function () {
        const data = {
            nickname: $("#nickname").val(),
            email: $("#email").val()
        };

        $.ajax({
            url: "/user/update",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (result) {
                if (result) {
                    alert("프로필이 변경 되었습니다.");
                    location.reload();
                } else {
                    alert("프로필 업데이트 실패");
                }
            },
            error: function () {
                alert("서버 통신 오류");
            }
        });
    });

    // 비밀번호 변경 버튼 클릭 이벤트 (모달 내)
    $("#passwordUpdateBtn").click(function () {
        const currentPassword = $("#currentPassword").val();
        const newPassword = $("#newPassword").val();
        const confirmNewPassword = $("#confirmNewPassword").val();

        if (newPassword !== confirmNewPassword) {
            alert("새 비밀번호가 일치하지 않습니다.");
            return;
        }

        const data = {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmNewPassword: confirmNewPassword
        };

        $.ajax({
            url: "/user/update/password",
            type: "POST",
            contentType: "application/json",
            data: JSON.stringify(data),
            success: function (result) {
                if (result === true) {
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
    $(document).on('click', '.delete-account-btn', function () {
        console.log("계정 삭제 버튼 클릭됨");
        if (confirm("정말로 계정을 삭제하시겠습니까? 이 작업은 복구할 수 없습니다.")) {
            $.ajax({
                url: "/user/delete",
                type: "POST",
                contentType: "application/json",
                success: function (result) {
                    if (result === true) {
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
