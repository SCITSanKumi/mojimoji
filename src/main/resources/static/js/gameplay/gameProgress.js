$(() => {
    // 현재 URL에서 bookId 파라미터 가져오기(전역 변수)
    window.bookId = Number(new URLSearchParams(window.location.search).get("bookId")) || -1;
    window.isGameEnded = false;

    const npcImages = {
        "마을 이장": "/image/npc_images/People1-7.png",
        "퇴역 용사2": "/image/npc_images/People3-8.png",
        "퇴역 용사": "/image/npc_images/Actor1-3.png",
        "마을 사람": "/image/npc_images/People2-6.png",
        "무기점": "/image/npc_images/People4-5.png",
        "용사": "/image/npc_images/Actor1-1.png",
        "마왕": "/image/npc_images/Monster-8.png",
        "승려": "/image/npc_images/Actor2-7.png",
        "마법사": "/image/npc_images/Actor2-5.png",
        "도적": "/image/npc_images/Actor1-5.png",
        "전사": "/image/npc_images/Actor1-3.png",
        "궁수": "/image/npc_images/Actor1-6.png",
        "해적": "/image/npc_images/People2-4.png",
        "무도가": "/image/npc_images/People2-1.png",
        "축구 선수": "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcT40kfM7DbbZanc_rgJk_bIRPudIUpJVYtQOg&s",
        "고죠 사토루": "https://i.namu.wiki/i/_x0r8tR6SjcSjIKCT_6Zsfl9WIXngll2_-229D7dNKkL_hAUOlUA8cK5ChdyWebjLQMJpJ7xkobRCCq0xj0khbdQggnkDSnXUIu6Tvy_AfCXUXrTKJ5B4vlqb7gpLHyVfGgqi9n_ibE9AJsjqimUnw.webp"
    };

    // 기존 게임 상태 저장용 객체
    let previousGameState = {
        hp: null,
        gold: null,
        current_location: null
    };

    // 현재 화자/이전 화자
    let currentSpeaker = "narration";
    let previousSpeaker = "";

    // 메시지 전송 상태 변수
    let isSending = false;

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

                // 주소창에도 파라미터 반영
                const newUrl = new URL(window.location);
                newUrl.searchParams.set("bookId", bookId);
                window.history.pushState({}, '', newUrl);

                $("#start-btn").addClass("d-none");
                $(".show-at-game-start").removeClass("d-none");

                // 첫 안내 문구(json 포함)
                handleChunkText(response.message);
                scrollToUp()
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

        if (isSending || inputText === "" || bookId === -1) {
            return;
        }

        isSending = true; // 전송 시작 (추가 전송 방지)

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
            })
            .finally(() => {
                isSending = false; // 전송 완료 후 다시 전송 가능하도록 변경
            });
    }

    // SSE 응답 처리
    async function processResponseStream(response) {
        const reader = response.body
            .pipeThrough(new TextDecoderStream())
            .getReader();

        while (true) {
            const {
                done, value
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
            } else if (currentSpeaker === "teacher") {
                messageDiv.addClass("narration-message");
                contentDiv.addClass("teacher-content");
            } else if (currentSpeaker === "player") {
                messageDiv.addClass("player-message");
                contentDiv.addClass("player-content");
            } else {
                // 해당 NPC의 이미지 가져오기 (없으면 기본 이미지)
                let profileImgSrc = "/image/npc_images/Actor1-1_silhouette.png";

                for (const [key, value] of Object.entries(npcImages)) {
                    if (currentSpeaker.includes(key)) {
                        profileImgSrc = value;
                        break;
                    }
                }

                let headerDiv = $("<div>").addClass("npc-header");
                let profileImg = $("<img>").attr("src", profileImgSrc).addClass("npc-profile");
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
        if (typeof gameState.gold === "number") {
            if (previousGameState.gold !== gameState.gold) {
                previousGameState.gold = gameState.gold;
                flashEffect($("#goldAmount"));
                $("#goldAmount").text(gameState.gold);
            }
        }
        if (typeof gameState.current_location === "string") {
            if (previousGameState.current_location !== gameState.current_location) {
                previousGameState.current_location = gameState.current_location;
                flashEffect($("#currentLocation"));
                $("#currentLocation").text(gameState.current_location);
            }
        }
        if (Array.isArray(gameState.inventory)) {
            let inventoryHTML = "";
            gameState.inventory.forEach(item => {
                inventoryHTML += `<li class="list-group-item">${item}</li>`;
            });

            if (gameState.inventory.length > 0) {
                $("#inventory-panel").show();
            } else {
                $("#inventory-panel").hide();
            }

            $("#inventory-list").html(inventoryHTML);
        }
        // 게임 종료 시 UI 변경
        if (gameState.isEnded) {
            isGameEnded = true;

            $(".input-group").addClass("d-none");
            $("#end-btn").removeClass("d-none");
        }
    }

    // UI 변경 효과
    function flashEffect(element) {
        element.css({
            "transition": "all 0.5s ease",
            "background-color": "#adb5bd",
        });

        setTimeout(() => {
            element.css({
                "background-color": "transparent", // 원래 배경색
            });
        }, 300); // 0.3초 후 원래 상태로 되돌림
    }

    // 화자 정보 반영
    function updateSpeaker(speaker) {
        currentSpeaker = speaker.name;
    }

    function scrollToUp() {
        requestAnimationFrame(() => {
            $("#chatBox").scrollTop(0);
        });
    }

    function scrollToBottom() {
        requestAnimationFrame(() => {
            $("#chatBox").scrollTop($("#chatBox")[0].scrollHeight);
        });
    }
});