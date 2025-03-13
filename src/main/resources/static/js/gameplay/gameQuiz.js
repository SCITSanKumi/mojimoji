
$(() => {
    let questionList = [];
    let currentIndex = 0;
    let correctAnswerIndexes = [];
    let score = 0;

    // 게임 종료 및 퀴즈 시작
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

    // 퀴즈 시작
    function startQuiz() {
        if (currentIndex < questionList.length) {
            let quizItem = $(".quiz-item");
            let question = questionList[currentIndex];

            let quizNowProc = $('.quizNow').html();
            quizNowProc = quizNowProc.replace('○', '●');
            $('.quizNow').html(quizNowProc);


            setTimeout(() => {
                $('.quizScroll').scrollLeft($('.quizScroll')[0].scrollWidth * (currentIndex/questionList.length)-100);
            }, 25);

            quizItem.find(".kanji-question").text(`${question.kanji}`);
            quizItem.find(".kunyomi-input, .onyomi-input").val("");
            quizItem.find(".answer-btn").off("click").on("click", () => checkAnswer(question));
        } else {
            endQuiz();
        }
    }

    // 정답 확인
    function checkAnswer(question) {
        let kunyomiAnswer = $(".kunyomi-input").val().trim();
        let onyomiAnswer = $(".onyomi-input").val().trim();
        let quizNowProc = $('.quizNow').html();
        quizNowProc = quizNowProc.replace('○', '●');
        $('.quizNow').html(quizNowProc);

        if (kunyomiAnswer === question.korKunyomi &&
            onyomiAnswer === question.korOnyomi) {
            correctAnswerIndexes.push(currentIndex);
            score++;

            $.ajax({
                type: "POST",
                url: "/kanji/addCollection",
                data: { "kanjiId": question.kanjiId },
                success: function () {
                    $('#quizProc').css('color', 'black');
                    $('#quizProc').html('정답입니다! 컬렉션에 추가되었습니다. <br> ㅤ')
                    quizNowProc = quizNowProc.replace('●', `<div class= "quizEnd" data-kanjiId="${question.kanjiId}"
                    data-kanji="${question.kanji}" data-category="${question.category}" data-jlptrank="${question.jlptRank}"
                    data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
                    data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
                    data-meaning="${question.meaning}" data-createdAt="카드 수집 완료" data-bookmark="${question.bookmarked}" style = "color: black; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:16px; border:1px solid #ccc; padding:5px; margin-right:7px; text-align: center; width:51.6px; height:75.6px; display: inline-block;" > ${question.kanji} <br> ${question.jlptRank}</div > `);
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
                    data-kanji="${question.kanji}" data-category="${question.category}" data-jlptrank="${question.jlptRank}"
                    data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
                    data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
                    data-meaning="${question.meaning}" data-createdAt="오답노트 추가 완료" data-bookmark="${question.bookmarked}" style = "background-color: lightgray; opacity: 0.5; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:16px; border:1px solid #ccc; padding:5px; margin-right:7px; text-align: center; width:51.6px; height:75.6px; display: inline-block;" > ${question.kanji} <br> ${question.jlptRank}</div > `);
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
            let kanjiId = $(this).data('kanjiid');
            let kanji = $(this).data('kanji');
            let category = $(this).data('category');
            let jlptRank = $(this).data('jlptrank');
            let korKunyomi = $(this).data('korkunyomi');
            let korOnyomi = $(this).data('koronyomi');
            let jpnKunyomi = $(this).data('jpnkunyomi');
            let jpnOnyomi = $(this).data('jpnonyomi');
            let bookMark = $(this).attr('data-bookmark');

            console.log(bookMark);

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

            if (bookMark == 1) {
                $('.bookMark').text('★')
                $('.bookMark').css('color', 'gold');
                $(this).attr('data-bookmark', 1);
                question.bookmarked = 1;
            } else {
                $('.bookMark').text('☆')
                $('.bookMark').css('color', 'black');
                $(this).attr('data-bookmark', 0);
                question.bookmarked = 0;
            }

            $(".kanjiModal").css("display", "block");
        });

        $("#scoreDisplay").text(`점수: ${score}`);

        currentIndex++;

        if (currentIndex < questionList.length) startQuiz();
        else endQuiz();
    }

    // 퀴즈 종료
    function endQuiz() {
        let resultHtml = questionList.map((question, index) => {
            let color = correctAnswerIndexes.includes(index) ? "white" : "lightgray; opacity : 0.5";
            let inCreatedAt = correctAnswerIndexes.includes(index) ? "카드 수집 완료" : "오답노트 추가 완료";
            return `<div div class= "quizEnd" data-kanjiId="${question.kanjiId}"
                data-kanji="${question.kanji}" data-category="${question.category}" data-jlptrank="${question.jlptRank}"
                data-korKunyomi="${question.korKunyomi}" data-korOnyomi="${question.korOnyomi}"
                data-jpnKunyomi="${question.jpnKunyomi}" data-jpnOnyomi="${question.jpnOnyomi}"
                data-meaning="${question.meaning}" data-createdAt="${inCreatedAt}" data-bookmark="${question.bookmarked}" style = "background-color: ${color}; border-radius: 10px; transition:transform 0.2s; font-weight: 500; font-size:65px; border:1px solid #ccc; padding:5px; margin-right:20px; text-align: center; width:77.4px; height:113.4px; display: inline-block;"> ${question.kanji}</div > `;
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

$(document).off('click', '.bookMark').on('click', '.bookMark', function (e) {
    let kanjiId = $(this).parents('.toKanjiList').find('.detailKanjiId').text().slice(3);
    let $this = $(this);

    if ($this.text().trim() === '☆') {
        $.ajax({
            url: "/kanji/addBookMark",
            method: "POST",
            data: { "kanjiId": kanjiId },
            success: function () {
                $this.text('★');
                $this.css('color', 'gold');
                $(`.quizEnd[data-kanjiid=${kanjiId}]`).attr('data-bookmark', 1);
            },
            error: () => alert("북마크 추가에 실패했습니다.")
        });
    } else {
        $.ajax({
            url: "/kanji/deleteBookMark",
            method: "POST",
            data: { "kanjiId": kanjiId },
            success: function () {
                $this.text('☆');
                $this.css('color', 'black');
                $(`.quizEnd[data-kanjiid=${kanjiId}]`).attr('data-bookmark', 0);

            },
            error: () => alert("북마크 삭제에 실패했습니다.")
        });
    }
});