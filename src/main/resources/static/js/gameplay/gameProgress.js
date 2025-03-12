$(() => {
    // 현재 URL에서 bookId 파라미터 가져오기(전역 변수)
    window.bookId = Number(new URLSearchParams(window.location.search).get("bookId")) || -1;
    window.isGameEnded = false;

    // 현재 화자/이전 화자
    let currentSpeaker = "narration";
    let previousSpeaker = "";

    // JSON 파싱용 버퍼
    let readingJson = false;
    let jsonBuffer = "";

    let contentDiv;
    let messageDiv;

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

    // SSE 응답 처리
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

    // 문자 단위로 처리해서 '{' ~ '}' 구간을 JSON으로, 그 외는 대사 누적
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

    // 말풍선 생성
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

    // 게임 정보 반영
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

    // 화자 정보 반영
    function updateSpeaker(speaker) {
        currentSpeaker = speaker.name;
    }

    function scrollToBottom() {
        requestAnimationFrame(() => {
            $("#chatBox").scrollTop($("#chatBox")[0].scrollHeight);
        });
    }
});