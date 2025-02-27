
let nickCheck = false;
let IdCheck = false;
let EmailCheck = false;
let PwCheck = false;

$(function () {
    $('#nickname').change(nicknameCheck);
    $('#username').change(usernameCheck);
    $('#email').change(emailCheck);
    $('#password').change(passwordCheck);
    $('#sign-up-btn').on('click', signUp);
});

// 1) 닉네임 검사 + 중복 체크
function nicknameCheck() {
    let nickname = $('#nickname').val().trim();
    let nameCheck = $('#nicknameCheck');

    // 닉네임 정규식: 한글, 영문, 숫자, 공백 허용, 길이 3~20
    const NICK_REGEX = /^[A-Za-z0-9가-힣\s]{3,20}$/;

    if (!NICK_REGEX.test(nickname)) {
        $('#nickname').css('border-color', 'red');
        nameCheck.text('3~20자의 한글, 영문, 숫자, 공백 만 사용가능합니다.');
        nickCheck = false;
        return false;
    } else {
        $('#nickname').css('border-color', '');
        nameCheck.text('');
    }

    // 닉네임 중복 체크 (GET)
    $.ajax({
        type: 'GET',
        url: '/user/nickname-check',
        data: { nickname: nickname },
        dataType: 'json',
        success: function (response) {
            if (response === true) {
                $('#nickname').css('border-color', 'green');
                nameCheck.text('');
                nickCheck = true;
            } else {
                $('#nickname').css('border-color', 'red');
                nameCheck.text('이미 사용중인 닉네임입니다.');
                nickCheck = false;
            }
        },
        error: function (xhr, status, error) {
            console.error('에러 발생:', error);
        }
    });
}

// 2) 아이디 검사 + 중복 체크
function usernameCheck() {
    let username = $('#username').val().trim();
    let idCheck = $('#idCheck');

    // 아이디 정규식: 영문, 숫자만 허용, 길이 5~11
    const ID_REGEX = /^[A-Za-z0-9]{5,11}$/;

    if (!ID_REGEX.test(username)) {
        $('#username').css('border-color', 'red');
        idCheck.text('5~11자의 영문, 숫자 만 사용가능합니다.');
        IdCheck = false;
        return false;
    } else {
        $('#username').css('border-color', '');
        idCheck.text('');
    }

    // 아이디 중복 체크 (GET)
    $.ajax({
        type: 'GET',
        url: '/user/id-check',
        data: { username: username },
        dataType: 'json',
        success: function (response) {
            if (response === true) {
                $('#username').css('border-color', 'green');
                idCheck.text('');
                IdCheck = true;
            } else {
                $('#username').css('border-color', 'red');
                idCheck.text('이미 사용중인 아이디입니다.');
                IdCheck = false;
            }
        },
        error: function (xhr, status, error) {
            console.error('에러 발생:', error);
        }
    });
}

// 3) 이메일 검사 + 중복 체크
function emailCheck() {
    let email = $('#email').val().trim();
    let emailMsg = $('#emailCheck');

    const EMAIL_REGEX = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
    if (!EMAIL_REGEX.test(email)) {
        $('#email').css('border-color', 'red');
        emailMsg.text('이메일 형식이 올바르지 않습니다.');
        EmailCheck = false;
        return false;
    } else {
        $('#email').css('border-color', '');
        emailMsg.text('');
    }

    $.ajax({
        type: 'GET',
        url: '/user/email-check',
        data: { email: email },
        dataType: 'json',
        success: function (response) {
            if (response === true) {
                $('#email').css('border-color', 'green');
                emailMsg.text('');
                EmailCheck = true;
            } else {
                $('#email').css('border-color', 'red');
                emailMsg.text('이미 사용중인 이메일입니다.');
                EmailCheck = false;
            }
        },
        error: function (xhr, status, error) {
            console.error('에러 발생:', error);
        }
    });
}

// 4) 비밀번호 검사
function passwordCheck() {
    let password = $('#password').val().trim();
    let pwMsg = $('#pwCheck');

    // 비밀번호 정규식: 영문, 숫자, 일부 특수문자 허용, 길이 8~16
    // -> 모든 조합 필수 X, 포함 가능.
    const PW_REGEX = /^[A-Za-z0-9!@#$%^&*]{8,16}$/;

    if (!PW_REGEX.test(password)) {
        $('#password').css('border-color', 'red');
        pwMsg.text('8~16자의 영문, 숫자, 특수문자(!@#$%^&*) 만 사용 가능합니다.');
        PwCheck = false;
        return false;
    } else {
        $('#password').css('border-color', 'green');
        pwMsg.text('');
        PwCheck = true;
    }
}

function showToast(message) {
    const toast = document.getElementById('toast');
    toast.innerText = message;
    toast.classList.add('show');
    setTimeout(() => toast.classList.remove('show'), 3000);
}

function signUp() {
    if (!nickCheck) {
        showToast('닉네임을 확인해주세요.');
        return false;
    }
    if (!IdCheck) {
        showToast('아이디를 확인해주세요.');
        return false;
    }
    if (!EmailCheck) {
        showToast('이메일을 확인해주세요.');
        return false;
    }
    if (!PwCheck) {
        showToast('비밀번호를 확인해주세요.');
        return false;
    }


    let data = {
        nickname: $('#nickname').val().trim(),
        username: $('#username').val().trim(),
        email: $('#email').val().trim(),
        password: $('#password').val().trim()
    };

    $.ajax({
        type: 'POST',
        url: '/user/sign-up',
        contentType: 'application/json',
        data: JSON.stringify(data),
        success: function (response) {
            if (response === true) {
                window.location.replace('/user/login?success=true&message=' + encodeURIComponent('회원가입 성공'));
            } else {
                showToast('회원가입에 실패했습니다.');
            }
        },
        error: function (xhr, status, error) {
            console.error('에러 발생:', error);
        }
    });
}

