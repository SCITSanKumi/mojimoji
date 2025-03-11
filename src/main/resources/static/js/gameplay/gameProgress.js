$(() => {
    // 현재 URL에서 bookId 파라미터 가져오기
    let bookId = Number(new URLSearchParams(window.location.search).get("bookId")) || -1;
    let isGameEnded = false;

    // 현재 화자/이전 화자
    let currentSpeaker = "narration";
    let previousSpeaker = "";

    // JSON 파싱용 버퍼
    let readingJson = false;
    let jsonBuffer = "";

    // 퀴즈 관련 변수
    let questionList = [];
    let currentIndex = 0;
    let correctAnswerIndexes = [];
    let score = 0;

    const chatBox = $("#chatBox");

    // 이벤트 리스너
    $("#submit-btn").click(sendUserMessage);
    $("#user-input").on("keydown", (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            sendUserMessage();
        }
    });

    // 게임 시작
    $("#start-btn").click(() => {
        $.ajax({
            type: "GET",
            url: `/game/start/${bookId}`,
            success: (response) => {
                bookId = response.bookId;
                $("#start-btn").addClass("d-none");
                $(".show-at-game-start").removeClass("d-none");

                // 첫 안내 문구(json 포함)
                handleChunkText(response.message);
            },
            error: (err) => {
                alert("게임 시작 중 오류가 발생했습니다.");
            }
        });
    });

    // 메시지 전송
    function sendUserMessage() {
        let inputField = $("#user-input");
        let inputText = inputField.val().trim();

        if (inputText === "" || bookId === -1) {
            return;
        }
        // 플레이어 말풍선
        currentSpeaker = "player";
        appendMessage(inputText);
        inputField.val("");

        fetch(`/game/send/${bookId}`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/x-www-form-urlencoded"
                },
                body: new URLSearchParams({
                    message: inputText
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error("메시지 전송 중 오류");
                }
                return processResponseStream(response);
            })
            .catch(error => {
                console.error("fetch error:", error);
                alert(error);
            });
    }

    /**
     * SSE 응답 처리
     */
    async function processResponseStream(response) {
        const reader = response.body
            .pipeThrough(new TextDecoderStream())
            .getReader();

        while (true) {
            const {
                done,
                value
            } = await reader.read();
            if (done) {
                break;
            }
            if (!value) {
                continue;
            }

            // data: 제거 + 개행 제거
            let cleaned = value.replace(/^data:/gm, "").replace(/\r?\n/g, "");
            if (!cleaned.trim()) {
                continue;
            }

            // 문자 단위로 검사
            handleChunkText(cleaned);
        }
    }

    /**
     * 문자 단위로 처리해서 '{' ~ '}' 구간을 JSON으로, 그 외는 대사 누적
     */
    function handleChunkText(str) {
        for (let i = 0; i < str.length; i++) {
            let ch = str[i];

            if (!readingJson) {
                // JSON 시작 전
                if (ch === '{') {
                    readingJson = true;
                    jsonBuffer = "{";
                } else {
                    // 일반 텍스트
                    appendMessage(ch);
                }
            } else {
                // JSON 읽는 중
                jsonBuffer += ch;

                if (ch === '}') {
                    // JSON 완성
                    readingJson = false;
                    parseJson(jsonBuffer);
                    jsonBuffer = "";
                }
            }
        }
    }

    /**
     * JSON 문자열을 실제 객체로 파싱
     * {"hp":...} -> 게임정보, {"name":"..."} -> 화자 변경
     */
    function parseJson(jsonStr) {
        try {
            let parsed = JSON.parse(jsonStr);

            if (parsed.name !== undefined) {
                updateSpeaker(parsed);
            } else if (parsed.hp !== undefined) {
                updateGameStats(parsed);
            } else {
                console.warn("unknown json =>", parsed);
            }
        } catch (e) {
            console.error("error =>", e);
        }
    }

    let messageDiv
    let contentDiv

    /**
     * 말풍선 생성
     */
    function appendMessage(text) {
        if (currentSpeaker !== previousSpeaker) {
            previousSpeaker = currentSpeaker;

            messageDiv = $("<div>").addClass("message");
            contentDiv = $("<div>").addClass("message-content");

            if (currentSpeaker === "narration") {
                messageDiv.addClass("narration-message");
                contentDiv.addClass("narration-content");
            } else if (currentSpeaker === "player") {
                messageDiv.addClass("player-message");
                contentDiv.addClass("player-content");
            } else {
                let headerDiv = $("<div>").addClass("npc-header");
                let profileImg = $("<img>")
                    .attr("src", "https://i.namu.wiki/i/8ya30FvrlxJ3M5ymG057r_Xrp7tg_QC65K8mmwLjjKwIo8GbiAFNWNpTH1iwPGhOwRf9Fdpbr0ixrVPK6JoDBRD5_b5aWA0Ex88s8DARnrIKIWX_pXErLW9j71xW5CZW_M4za1O75ZVX4Rv4kfnyhw.webp")
                    .addClass("npc-profile");
                let nameSpan = $("<span>").text(currentSpeaker).addClass("npc-name");
                headerDiv.append(profileImg).append(nameSpan);
                messageDiv.append(headerDiv);

                messageDiv.addClass("npc-message");
                contentDiv.addClass("npc-content");
            }

            messageDiv.append(contentDiv);
            $("#chatBox").append(messageDiv);
        }

        contentDiv.append(text);
        scrollToBottom();
    }

    /**
     * 게임 정보 반영
     */
    function updateGameStats(gameState) {
        if (typeof gameState.hp === "number") {
            $("#healthBar").css("width", gameState.hp + "%").text(gameState.hp);
        }
        if (typeof gameState.mp === "number") {
            $("#mentalBar").css("width", gameState.mp + "%").text(gameState.mp);
        }
        if (gameState.isEnded) {
            isGameEnded = true;

            $("#input-group").addClass("d-none");
            $("#end-btn").removeClass("d-none");
        }
    }

    /**
     * 화자 정보 반영
     */
    function updateSpeaker(speaker) {
        currentSpeaker = speaker.name;
    }

    function scrollToBottom() {
        requestAnimationFrame(() => {
            chatBox.scrollTop(chatBox[0].scrollHeight);
        });
    }

    // 힌트 버튼 클릭 시 한자 카드 컨텍스트 메뉴 토글 (입력창 오른쪽에 위치한 힌트 버튼 기준)
    $("#hint-btn").click(function(e) {
        e.preventDefault();
        let pos = $(this).offset();
        $("#kanjiCard").css({
            top: pos.top + $(this).outerHeight() + 5,
            left: pos.left
        }).toggle();
    });

    // 각 한자 카드에 대해 드래그 설정 (마우스 떼면 제자리로 돌아감: revert: true)
    $(".kanji-card").draggable({
        revert: true
    });

    // chat-container을 droppable로 설정
    $(".chat-container").droppable({
        drop: function(event, ui) {
            // 드래그된 한자 카드에서 선택한 한자 텍스트 추출
            let selectedKanji = ui.draggable.find(".card-kanji").text();
            $("#kanjiCard").hide();
            // AJAX로 힌트 요청 후 결과를 입력창에 채워 넣음
            $.ajax({
                url: `/game/hint/${bookId}`,
                method: "GET",
                data: {
                    kanji: selectedKanji
                },
                success: (response) => {
                    $("#user-input").val(response);
                },
                error: (e) => {
                    alert("힌트 요청 중 오류 발생");
                }
            });
        }
    });

        /** 게임 종료 및 퀴즈 시작 */
        $("#end-btn").click(() => {
            if (!isGameEnded) return;

            $.ajax({
                url: `/game/end/${bookId}`,
                type: "GET",
                success: (response) => {
                    if (!response || !response.kanjis?.length) {
                        alert("퀴즈 데이터를 불러올 수 없습니다.");
                        return;
                    }
                    questionList = response.kanjis;
                    currentIndex = 0;
                    score = 0;
                    $.each(questionList, function (index, item) {
                        $('.quizNow').append(`○ `);
                    })


                    $("#quiz-modal").modal("show");
                    startQuiz();
                },
                error: () => alert("게임 종료 중 오류 발생")
            });
        });

    // ===== 퀴즈 로직 =====
    /** 퀴즈 시작 */
    function startQuiz() {
        if (currentIndex < questionList.length) {
            let quizItem = $(".quiz-item");
            let question = questionList[currentIndex];
            let quizNowProc = $('.quizNow').html();
            quizNowProc = quizNowProc.replace('○', '●');
            $('.quizNow').html(quizNowProc);

            setTimeout(() => {
                $('.quizScroll').scrollLeft($('.quizScroll')[0].scrollWidth);
            }, 25);

            quizItem.find(".kanji-question").text(`${question.kanji}`);
            quizItem.find(".kunyomi-input, .onyomi-input").val("");

            quizItem.find(".answer-btn").off("click").on("click", () => checkAnswer(question));


        } else {
            endQuiz();
        }
    }

    /** 정답 확인 */
    function checkAnswer(question) {
        let kunyomiAnswer = $(".kunyomi-input").val().trim();
        let onyomiAnswer = $(".onyomi-input").val().trim();
        let quizNowProc = $('.quizNow').html();
        quizNowProc = quizNowProc.replace('○', '●');
        $('.quizNow').html(quizNowProc);



        if (kunyomiAnswer.toLowerCase() === question.korKunyomi.toLowerCase() &&
            onyomiAnswer.toLowerCase() === question.korOnyomi.toLowerCase()) {
            correctAnswerIndexes.push(currentIndex);
            score++;

            $.ajax({
                type: "POST",
                url: "/kanji/addCollection",
                data: { "kanjiId": question.kanjiId },
                success: function () {
                    $('#quizProc').html('정답입니다! 컬렉션에 추가되었습니다.')
                    quizNowProc = quizNowProc.replace('●', `<div class= "quizEnd" data-kanjiId="${question.kanjiId}"
        data-kanji="${question.kanji}" data-category="${question.category}" data-jlptRank="${question.jlptRank}"
        data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
        data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
        data-meaning="${question.meaning}" data-createdAt="카드 수집 완료" style = "color: black; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:16px; border:1px solid #ccc; padding:5px; margin-right:7px; text-align: center; width:51.6px; height:75.6px; display: inline-block;" > ${question.kanji} <br> ${question.jlptRank}</div > `);
                    $('.quizNow').html(quizNowProc);
                },
                error: () => $('#quizProc').html('컬렉션 추가 중 오류 발생')

            });
        } else {
            $.ajax({
                type: "POST",
                url: "/kanji/wrongCountUp",
                data: { "kanjiId": question.kanjiId },
                success: function () {
                    $('#quizProc').css('color', 'red');
                    $('#quizProc').html(`오답입니다! <br> ${question.kanji} : ${question.korKunyomi} ${question.korOnyomi}`);
                    quizNowProc = quizNowProc.replace('●', `<div class= "quizEnd" data-kanjiId="${question.kanjiId}"
        data-kanji="${question.kanji}" data-category="${question.category}" data-jlptRank="${question.jlptRank}"
        data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
        data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
        data-meaning="${question.meaning}" data-createdAt="오답노트 추가 완료" style = "background-color: lightgray; opacity: 0.5; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:16px; border:1px solid #ccc; padding:5px; margin-right:7px; text-align: center; width:51.6px; height:75.6px; display: inline-block;" > ${question.kanji} <br> ${question.jlptRank}</div > `);
                    $('.quizNow').html(quizNowProc);
                }
            });
        }


        // 한자카드 모달

        $(document).on('click', '.kanjiModal', function (e) {
            if (!$(e.target).parents('.modal_popup').length && !$(e.target).is('.modal_popup')) {
                if ($(".kanjiModal").css("display") == 'block') {

                    $(".kanjiModal").css("display", "none");
                }
            }
        });



        $(document).on('click', '.quizEnd', function () {
            // $('.quizEnd').on('click', function () {
            let kanjiId = $(this).attr('data-kanjiId');
            let kanji = $(this).data('kanji');
            let category = $(this).data('category');
            let jlptRank = $(this).data('jlptrank');
            let korKunyomi = $(this).data('korkunyomi');
            let korOnyomi = $(this).attr('data-korOnyomi');
            let jpnKunyomi = $(this).data('jpnkunyomi');
            let jpnOnyomi = $(this).data('jpnonyomi');

            let meaning = $(this).attr('data-meaning').replace(/\\n/g, '<br>');
            let createdAt = $(this).data('createdat');

            $(".detailKanjiId").text('No.' + `${kanjiId}`);
            $(".detailKanji").text(`${kanji}`);
            $(".detailCategory").text('카테고리 : ' + `${category}`);
            $(".detailJlptRank").text(`${jlptRank}`);
            $(".detailYomi").text(`${korKunyomi}` + ' ' + `${korOnyomi}`);
            $(".detailJpnKunyomi").text('훈독 : ' + `${jpnKunyomi}`);
            $(".detailJpnOnyomi").text('음독 : ' + `${jpnOnyomi}`);
            $(".detailMeaning").html('의미 :<br>' + `${meaning}`);
            $(".detailCreatedAt").text(`${createdAt}`);



            $(".kanjiModal").css("display", "block");


        });



        $("#scoreDisplay").text(`점수: ${score}`);
        currentIndex++;
        if (currentIndex < questionList.length) startQuiz();
        else endQuiz();
    }



    /** 퀴즈 종료 */
    function endQuiz() {
        let resultHtml = questionList.map((question, index) => {
            let color = correctAnswerIndexes.includes(index) ? "white" : "lightgray; opacity : 0.5";
            let inCreatedAt = correctAnswerIndexes.includes(index) ? "카드 수집 완료" : "오답노트 추가 완료";
            return `<div div class= "quizEnd" data-kanjiId="${question.kanjiId}"
        data-kanji="${question.kanji}" data-category="${question.category}" data-jlptRank="${question.jlptRank}"
        data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
        data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
        data-meaning="${question.meaning}" data-createdAt="${inCreatedAt}" style = "background-color: ${color}; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:65px; border:1px solid #ccc; padding:5px; margin-right:20px; text-align: center; width:77.4px; height:113.4px; display: inline-block;"> ${question.kanji}</div > `;
        }).join(" ");




        $("#quiz-container").hide();
        $('.kanjiQuiz').text('퀴즈 종료!');
        $("#game-result").removeClass("d-none").html(`
            <span span style = "margin-right : 15px; font-size: 20px;" > 맞힌 한자 개수: <strong>${score}</strong>개</span >
            <span style="margin-left : 15px; font-size: 20px;">틀린 한자 개수: <strong>${questionList.length - score}</strong>개</span>
            <div style="height:30px"></div>
        <div class="quizScroll">${resultHtml}</div><br>

            <div class="text-center mt-3">
                <button id="to-main" class="btn btn-secondary" disabled" style="margin-right : 15px">메인으로</button>
                <button id="share-story" class="btn btn-secondary" disabled"style="margin-left : 15px">스토리 공유하기</button>
            </div >
                `);

        $("#to-main").off("click").on("click", () => location.href = "/");
        $("#share-story").off("click").on("click", shareStory);
        $('.quizEnd').on('click', function () {
            let kanjiId = $(this).attr('data-kanjiId');
            let kanji = $(this).data('kanji');
            let category = $(this).data('category');
            let jlptRank = $(this).data('jlptrank');
            let korKunyomi = $(this).data('korkunyomi');
            let korOnyomi = $(this).attr('data-korOnyomi');
            let jpnKunyomi = $(this).data('jpnkunyomi');
            let jpnOnyomi = $(this).data('jpnonyomi');

            let meaning = $(this).attr('data-meaning').replace(/\\n/g, '<br>');
            let createdAt = $(this).data('createdAt');

            $(".detailKanjiId").text('No.' + `${kanjiId}`);
            $(".detailKanji").text(`${kanji}`);
            $(".detailCategory").text('카테고리 : ' + `${category}`);
            $(".detailJlptRank").text(`${jlptRank}`);
            $(".detailYomi").text(`${korKunyomi}` + ' ' + `${korOnyomi}`);
            $(".detailJpnKunyomi").text('훈독 : ' + `${jpnKunyomi}`);
            $(".detailJpnOnyomi").text('음독 : ' + `${jpnOnyomi}`);
            $(".detailMeaning").html('의미 :<br>' + `${meaning}`);


            $(".kanjiModal").css("display", "block");

        })}

    function shareStory() {
        if (bookId === -1) {
            alert("게임이 시작되지 않았거나 책 정보가 없습니다.");
            return;
        }
        $.post("/board/myStory/share", {
                bookId
            })
            .done(() => {
                alert("스토리가 성공적으로 공유되었습니다!");
                location.href = "/board/story/list";
            })
            .fail(() => {
                alert("스토리 공유 중 오류가 발생했습니다.");
            });
    }

    // 모달 강제 종료 방지
    $('#quiz-modal').on('hide.bs.modal', e => {
        e.preventDefault();
        e.stopPropagation();
        return false;
    });
});