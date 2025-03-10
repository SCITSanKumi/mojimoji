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
    $("#userInput").on("keydown", (event) => {
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
                console.error("[gameStart] error:", err);
                alert("게임 시작 중 오류가 발생했습니다.");
            }
        });
    });

    // 메시지 전송
    function sendUserMessage() {
        let inputField = $("#userInput");
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

    // 마치기 버튼 -> 퀴즈
    $("#end-btn").click(() => {
        if (!isGameEnded) return;

        $.get(`/game/end/${bookId}`, (response) => {
            if (!response || !response.kanjis?.length) {
                alert("퀴즈 데이터를 불러올 수 없습니다.");
                return;
            }
            questionList = response.kanjis;
            currentIndex = 0;
            score = 0;
            $("#quiz-modal").modal("show");
            startQuiz();
        }).fail((err) => {
            console.error("[end-btn] /game/end fail:", err);
            alert("게임 종료 중 오류");
        });
    });

    // ===== 퀴즈 로직 =====
    function startQuiz() {
        if (currentIndex < questionList.length) {
            let quizItem = $(".quiz-item");
            let question = questionList[currentIndex];
            quizItem.find(".kanji-question").text(`문제: ${question.kanji}`);
            quizItem.find(".kunyomi-input, .onyomi-input").val("");

            quizItem.find(".answer-btn").off("click").on("click", () => checkAnswer(question));
        } else {
            endQuiz();
        }
    }

    function checkAnswer(question) {
        let kunyomiAnswer = $(".kunyomi-input").val().trim();
        let onyomiAnswer = $(".onyomi-input").val().trim();

        if (kunyomiAnswer.toLowerCase() === question.korKunyomi.toLowerCase() &&
            onyomiAnswer.toLowerCase() === question.korOnyomi.toLowerCase()) {
            correctAnswerIndexes.push(currentIndex);
            score++;
            $.post("/kanji/addCollection", {
                    kanjiId: question.kanjiId
                })
                .done(() => {
                    alert("정답! 한자 컬렉션에 추가되었습니다.");
                })
                .fail(() => alert("컬렉션 추가 중 오류 발생"));
        } else {
            alert("오답입니다!");
        }

        currentIndex++;

        if (currentIndex < questionList.length) {
            startQuiz();
        } else {
            endQuiz();
        }
    }

    function endQuiz() {
        let resultHtml = questionList.map((q, i) => {
            let color = correctAnswerIndexes.includes(i) ? "blue" : "red";
            return `<span style="color:${color}">${q.kanji}</span>`;
        }).join(" ");

        $("#quiz-container").hide();
        $("#game-result").removeClass("d-none").html(`
    <h3 class="text-center">퀴즈 종료!</h3>
    <p><strong>퀴즈 결과:</strong> ${resultHtml}</p>
    <p>맞힌 한자: <strong>${score}</strong>개</p>
    <p>틀린 한자: <strong>${questionList.length - score}</strong>개</p>
    <div class="text-center mt-3">
        <button id="to-main" class="btn btn-primary">메인으로</button>
        <button id="share-story" class="btn btn-secondary">스토리 공유하기</button>
    </div>
    `);

        $("#to-main").off("click").on("click", () => location.href = "/");
        $("#share-story").off("click").on("click", shareStory);
    }

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