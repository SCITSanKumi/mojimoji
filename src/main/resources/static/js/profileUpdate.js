$(document).ready(function () {
    const NICK_REGEX = /^[A-Za-z0-9가-힣\s]{3,20}$/;
    const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    const PW_REGEX = /^[A-Za-z0-9!@#$%^&*]{8,16}$/;

    // 닉네임 검사
    $("#nickname").on("input", function () {
        let nickname = $(this).val().trim();
        let $nicknameCheck = $("#nicknameCheck");

        if (!NICK_REGEX.test(nickname)) {
            $nicknameCheck.text("닉네임은 3~20자의 영문, 숫자, 한글만 사용할 수 있습니다.");
            $nicknameCheck.css("color", "red");
        } else {
            $nicknameCheck.text(""); // 유효한 경우 메시지 숨김
        }
    });

    // 이메일 정규식 체크
    $("#email").on("input", function () {
        let email = $(this).val().trim();
        let $emailCheck = $("#emailCheck");

        if (!EMAIL_REGEX.test(email)) {
            $emailCheck.text("올바른 이메일 주소를 입력하세요.");
            $emailCheck.css("color", "red");
        } else {
            $emailCheck.text(""); // 유효한 경우 메시지 숨김
        }
    });

    // 비밀번호 정규식 체크
    $("#newPassword, #confirmNewPassword").on("input", function () {
        let newPassword = $("#newPassword").val().trim();
        let confirmPassword = $("#confirmNewPassword").val().trim();
        let $newPasswordCheck = $("#newPasswordCheck");
        let $confirmNewPasswordCheck = $("#confirmNewPasswordCheck");

        // 새 비밀번호 유효성 검사
        if (!PW_REGEX.test(newPassword)) {
            $newPasswordCheck.text('8~16자의 영문, 숫자, 특수문자(!@#$%^&*) 만 사용 가능합니다.').css("color", "red");
        } else {
            $newPasswordCheck.text(""); // 유효한 경우 숨김
        }

        // 새 비밀번호 확인 검사
        if (confirmPassword.length > 0 && newPassword !== confirmPassword) {
            $confirmNewPasswordCheck.text("새 비밀번호가 일치하지 않습니다.").css("color", "red");
        } else {
            $confirmNewPasswordCheck.text(""); // 유효한 경우 숨김
        }
    });

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
        let nickname = $("#nickname").val().trim();
        let email = $("#email").val() ? $("#email").val().trim() : null; // 이메일 필드가 없을 경우 null 처리
        let $nicknameCheck = $("#nicknameCheck");
        let $emailCheck = $("#emailCheck");

        let isValid = true;

        // 닉네임 유효성 검사
        if (!NICK_REGEX.test(nickname)) {
            $nicknameCheck.text("닉네임은 3~20자의 영문, 숫자, 한글만 사용할 수 있습니다.");
            $nicknameCheck.css("color", "red");
            isValid = false;
        } else {
            $nicknameCheck.text(""); // 유효한 경우 메시지 숨김
        }

        // 이메일 유효성 검사 (소셜 로그인 사용자는 email 필드 없음)
        if (email !== null && !EMAIL_REGEX.test(email)) {
            $emailCheck.text("올바른 이메일 주소를 입력하세요.");
            $emailCheck.css("color", "red");
            isValid = false;
        } else {
            $emailCheck.text(""); // 유효한 경우 메시지 숨김
        }

        // 유효성 검사에 실패하면 요청을 보내지 않음
        if (!isValid) {
            return;
        }

        $.ajax({
            url: $(this).attr('action'),
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function () {
                alert("프로필이 변경되었습니다.");
                window.location.reload(); // 페이지 새로고침
            },
            error: function (xhr) {
                let errMsg = "";
                if (xhr.responseJSON && xhr.responseJSON.error) {
                    errMsg = xhr.responseJSON.error;
                } else if (xhr.responseText) {
                    errMsg = xhr.responseText;
                }
                errMsg = errMsg.replace("Invalid argument: ", "");
                alert(errMsg);
            }
        });
    });

    // 비밀번호 변경 버튼 클릭 이벤트
    $("#passwordUpdateBtn").click(function () {
        const currentPassword = $("#currentPassword").val().trim();
        const newPassword = $("#newPassword").val().trim();
        const confirmPassword = $("#confirmNewPassword").val().trim();

        if (!newPassword) {
            alert("새 비밀번호를 입력하세요.");
            return;
        }

        if (!PW_REGEX.test(newPassword)) {
            alert("비밀번호 형식을 확인해 주세요.");
            return;
        }

        if (newPassword !== confirmPassword) {
            alert("새 비밀번호가 일치하지 않습니다.");
            return;
        }

        const data = {
            currentPassword: currentPassword,
            newPassword: newPassword,
            confirmNewPassword: confirmPassword
        };

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
                    alert("현재 비밀번호를 확인해 주세요.");
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
