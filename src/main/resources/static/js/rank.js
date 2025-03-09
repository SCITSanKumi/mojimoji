document.addEventListener('DOMContentLoaded', function () {
    // particles.js 초기화
    if (typeof particlesJS !== 'undefined') {
        particlesJS("particles-js", {
            "particles": {
                "number": {
                    "value": 80,
                    "density": {
                        "enable": true,
                        "value_area": 800
                    }
                },
                "color": { "value": "#ffffff" },
                "shape": {
                    "type": "circle",
                    "stroke": { "width": 0, "color": "#000000" },
                    "polygon": { "nb_sides": 5 }
                },
                "opacity": {
                    "value": 0.5,
                    "random": false,
                    "anim": { "enable": false, "speed": 1, "opacity_min": 0.1, "sync": false }
                },
                "size": {
                    "value": 3,
                    "random": true,
                    "anim": { "enable": false, "speed": 40, "size_min": 0.1, "sync": false }
                },
                "line_linked": {
                    "enable": true,
                    "distance": 150,
                    "color": "#ffffff",
                    "opacity": 0.4,
                    "width": 1
                },
                "move": {
                    "enable": true,
                    "speed": 2,
                    "direction": "none",
                    "random": false,
                    "straight": false,
                    "out_mode": "out",
                    "bounce": false,
                    "attract": { "enable": false, "rotateX": 600, "rotateY": 1200 }
                }
            },
            "interactivity": {
                "detect_on": "canvas",
                "events": {
                    "onhover": { "enable": true, "mode": "grab" },
                    "onclick": { "enable": true, "mode": "push" },
                    "resize": true
                },
                "modes": {
                    "grab": { "distance": 140, "line_linked": { "opacity": 1 } },
                    "bubble": { "distance": 400, "size": 40, "duration": 2, "opacity": 8, "speed": 3 },
                    "repulse": { "distance": 200, "duration": 0.4 },
                    "push": { "particles_nb": 4 },
                    "remove": { "particles_nb": 2 }
                }
            },
            "retina_detect": true
        });
    }

    // confetti 초기화 (we attach the event on dynamic elements later)
    function attachCelebrateBtnEvents(containerSelector) {
        // Use delegated event listener so even dynamic celebrate-btns work.
        $(containerSelector).off('click', '.celebrate-btn').on('click', '.celebrate-btn', function () {
            const end = Date.now() + (3 * 1000);
            const colors = ['#FFD700', '#FFF', '#FF6B6B'];
            (function frame() {
                confetti({ particleCount: 3, angle: 60, spread: 55, origin: { x: 0 }, colors: colors });
                confetti({ particleCount: 3, angle: 120, spread: 55, origin: { x: 1 }, colors: colors });
                if (Date.now() < end) {
                    requestAnimationFrame(frame);
                }
            }());
        });
    }

    // 모달 이벤트 재설정 함수: 동적 요소에 대해 모달 클릭 이벤트도 재설정합니다.
    function attachModalEvents(containerSelector) {
        // Remove any previous handlers to avoid duplicates
        $(containerSelector).off('click', '.player, tr').on('click', '.player, tr', function () {
            let playerName, playerAvatar, playerStory, playerKanji, playerStreak, playerRating, playerScore, playerStories;
            // Check if this row is from the top players section (has class 'player')
            if ($(this).hasClass('player')) {
                playerName = $(this).find('h3').text();
                playerAvatar = $(this).find('.avatar img').attr('src');
                playerStory = $(this).find('.stat:nth-child(1) span').text();
                playerKanji = $(this).find('.stat:nth-child(2) span').text();
                playerStreak = $(this).find('.badge span').text() || "25";
                playerRating = $(this).find('.stat:nth-child(3) span').text();
                playerScore = $(this).find('.score').text().replace(/,/g, '');
                playerStories = Math.floor(parseInt(playerKanji) / 3);
            } else {
                const playerInfo = $(this).find('.player-info');
                playerName = playerInfo.find('.name').text().replace(/\s+\(.*\)$/, '');
                playerAvatar = playerInfo.find('img').attr('src');
                // Assume you have a global playerData array to match by name.
                const player = playerData.find(p => p.name === playerName) || playerData[0];
                playerStory = player.kanjiCount;
                playerKanji = player.quizScore + '%';
                playerStreak = player.streak;
                playerRating = player.rating;
                playerScore = player.totalScore;
                playerStories = player.storiesCompleted || Math.floor(player.kanjiCount / 3);
            }
            $('#modalUserAvatar').attr('src', playerAvatar);
            $('#modalUserName').text(playerName);
            $('#modalUserStreak').text(playerStreak);
            $('#modalUserRating').text(playerRating);
            $('#modalUserKanji').text(playerStory);
            $('#modalUserQuiz').text(playerKanji);
            $('#modalUserScore').text(playerScore);
            $('#modalUserStories').text(playerStories);
            $('#modalUserKanjiCounts').text(playerKanji);
            const storyProgressPercentage = (playerStories / 50) * 100;
            const kanjiProgressPercentage = (parseInt(playerKanji) / 2136) * 100;
            $('#modalStoryProgress').css('width', storyProgressPercentage + '%').attr('aria-valuenow', storyProgressPercentage);
            $('#modalKanjiProgress').css('width', kanjiProgressPercentage + '%').attr('aria-valuenow', kanjiProgressPercentage);
            // Show the modal
            const userProfileModal = new bootstrap.Modal(document.getElementById('userProfileModal'));
            userProfileModal.show();
        });
    }

    // 상위 3명 데이터를 로드하는 함수 (전체 순위 탭)
    function loadOverallTopPlayers() {
        $.ajax({
            url: '/rank/overall',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                // Assume data is sorted by totalScore descending.
                const topThree = data.slice(0, 3);
                // Rearrange order: 2등, 1등, 3등
                const second = topThree[1];
                const first = topThree[0];
                const third = topThree[2];
                const htmlSecond = `
                    <div class="player second">
                        <div class="rank">2</div>
                        <div class="avatar">
                            <img src="${second.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${second.totalGaechu}</span></div>
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${second.kanjiScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${second.bookScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${second.likeScore.toFixed(1)}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${first.totalGaechu}</span></div>
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${first.kanjiScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${first.bookScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${first.likeScore.toFixed(1)}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${third.totalGaechu}</span></div>
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${third.kanjiScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${third.bookScore.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${third.likeScore.toFixed(1)}</span></div>
                        </div>
                    </div>`;
                // Combine in desired order (2nd, 1st, 3rd)
                const topPlayersHtml = `<div class="top-players animate__animated animate__fadeInUp">
                    ${htmlSecond}
                    ${htmlFirst}
                    ${htmlThird}
                </div>`;
                $('.overall-top-players').html(topPlayersHtml);
                // Reattach dynamic events for celebrate buttons and modals inside overall top players container
                attachCelebrateBtnEvents('.overall-top-players');
                attachModalEvents('.overall-top-players');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching overall top players: ", error);
            }
        });
    }

    // Load overall rankings for table
    function loadOverallRankings() {
        $.ajax({
            url: '/rank/overall',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                let html = '';
                data.forEach((record, index) => {
                    let rankClass = "";
                    if (index === 0) {
                        rankClass = "rank-badge gold";
                    } else if (index === 1) {
                        rankClass = "rank-badge silver";
                    } else if (index === 2) {
                        rankClass = "rank-badge bronze";
                    }
                    html += `<tr class="animate__animated animate__fadeIn rank-${index + 1}">
                        <td><span class="${rankClass}">${index + 1}</span></td>
                        <td>
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td><i class="fas fa-graduation-cap"></i> ${record.kanjiScore.toFixed(1)}</td>
                        <td><i class="fas fa-book"></i> ${record.bookScore.toFixed(1)}</td>
                        <td><i class="fas fa-thumbs-up"></i> ${record.likeScore.toFixed(1)}</td>
                        <td>${record.totalScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.players-list').html(html);
                // Reattach modal events for table rows
                attachModalEvents('.players-list');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching overall ranking data: ", error);
            }
        });
    }

    // 상위 3명 데이터를 로드하는 함수 (한자 순위 탭)
    function loadKanjiTopPlayers() {
        $.ajax({
            url: '/rank/kanji',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                // Assume data is sorted by totalScore descending.
                const topThree = data.slice(0, 3);
                // Rearrange order: 2등, 1등, 3등
                const second = topThree[1];
                const first = topThree[0];
                const third = topThree[2];
                const htmlSecond = `
                    <div class="player second">
                        <div class="rank">2</div>
                        <div class="avatar">
                            <img src="${second.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${second.collectionPercentage.toFixed(1)}</span></div>
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${second.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${second.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${second.kanjiCollectionBonus}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${first.collectionPercentage.toFixed(1)}</span></div>
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${first.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${first.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${first.kanjiCollectionBonus}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${third.collectionPercentage.toFixed(1)}</span></div>
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${third.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${third.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${third.kanjiCollectionBonus}</span></div>
                        </div>
                    </div>`;
                // Combine in desired order (2nd, 1st, 3rd)
                const topPlayersHtml = `<div class="top-players animate__animated animate__fadeInUp">
                    ${htmlSecond}
                    ${htmlFirst}
                    ${htmlThird}
                </div>`;
                $('.kanji-top-players').html(topPlayersHtml);
                // Reattach dynamic events for celebrate buttons and modals inside overall top players container
                attachCelebrateBtnEvents('.kanji-top-players');
                attachModalEvents('.kanji-top-players');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching kanji top players: ", error);
            }
        });
    }

    function loadKanjiRankings() {
        $.ajax({
            url: '/rank/kanji',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                let html = '';
                data.forEach((record, index) => {
                    let rankClass = "";
                    if (index === 0) {
                        rankClass = "rank-badge gold";
                    } else if (index === 1) {
                        rankClass = "rank-badge silver";
                    } else if (index === 2) {
                        rankClass = "rank-badge bronze";
                    }
                    html += `<tr class="animate__animated animate__fadeIn rank-${index + 1}">
                        <td><span class="${rankClass}">${index + 1}</span></td>
                        <td>
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td><i class="fas fa-graduation-cap"></i> ${record.kanjiCollections} / 2136</td>
                        <td>${record.collectionPercentage.toFixed(1)}%</td>
                        <td>${record.kanjiScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.kanji-list').html(html);
                attachModalEvents('.kanji-list');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching kanji ranking data: ", error);
            }
        });
    }

    // 상위 3명 데이터를 로드하는 함수 (스토리 순위 탭)
    function loadStoryTopPlayers() {
        $.ajax({
            url: '/rank/story',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                // Assume data is sorted by totalScore descending.
                const topThree = data.slice(0, 3);
                // Rearrange order: 2등, 1등, 3등
                const second = topThree[1];
                const first = topThree[0];
                const third = topThree[2];
                const htmlSecond = `
                    <div class="player second">
                        <div class="rank">2</div>
                        <div class="avatar">
                            <img src="${second.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${second.bookLines}</span></div>
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${second.books}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${second.bookLines}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${first.bookLines}</span></div>
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${first.books}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${first.bookLines}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${third.bookLines}</span></div>
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${third.books}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${third.bookLines}</span></div>
                        </div>
                    </div>`;
                // Combine in desired order (2nd, 1st, 3rd)
                const topPlayersHtml = `<div class="top-players animate__animated animate__fadeInUp">
                    ${htmlSecond}
                    ${htmlFirst}
                    ${htmlThird}
                </div>`;
                $('.story-top-players').html(topPlayersHtml);
                // Reattach dynamic events for celebrate buttons and modals inside overall top players container
                attachCelebrateBtnEvents('.story-top-players');
                attachModalEvents('.story-top-players');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching story top players: ", error);
            }
        });
    }

    function loadStoryRankings() {
        $.ajax({
            url: '/rank/story',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                let html = '';
                data.forEach((record, index) => {
                    let rankClass = "";
                    if (index === 0) {
                        rankClass = "rank-badge gold";
                    } else if (index === 1) {
                        rankClass = "rank-badge silver";
                    } else if (index === 2) {
                        rankClass = "rank-badge bronze";
                    }
                    html += `<tr class="animate__animated animate__fadeIn rank-${index + 1}">
                        <td><span class="${rankClass}">${index + 1}</span></td>
                        <td>
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td>${record.books}</td>
                        <td>${record.bookLines}</td>
                        <td>${record.bookScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.story-list').html(html);
                attachModalEvents('.story-list');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching story ranking data: ", error);
            }
        });
    }

    // 상위 3명 데이터를 로드하는 함수 (스토리 순위 탭)
    function loadLikesTopPlayers() {
        $.ajax({
            url: '/rank/likes',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                // Assume data is sorted by totalScore descending.
                const topThree = data.slice(0, 3);
                // Rearrange order: 2등, 1등, 3등
                const second = topThree[1];
                const first = topThree[0];
                const third = topThree[2];
                const htmlSecond = `
                    <div class="player second">
                        <div class="rank">2</div>
                        <div class="avatar">
                            <img src="${second.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${second.sharedBooks}</span></div>
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${second.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${second.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${second.sharedBookReplies}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${first.sharedBooks}</span></div>
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${first.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${first.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${first.sharedBookReplies}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                            <div class="badge"><i class="fas fa-fire"></i><span>${third.sharedBooks}</span></div>
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-graduation-cap"></i><span>${third.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-book"></i><span>${third.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-thumbs-up"></i><span>${third.sharedBookReplies}</span></div>
                        </div>
                    </div>`;
                // Combine in desired order (2nd, 1st, 3rd)
                const topPlayersHtml = `<div class="top-players animate__animated animate__fadeInUp">
                    ${htmlSecond}
                    ${htmlFirst}
                    ${htmlThird}
                </div>`;
                $('.likes-top-players').html(topPlayersHtml);
                // Reattach dynamic events for celebrate buttons and modals inside overall top players container
                attachCelebrateBtnEvents('.likes-top-players');
                attachModalEvents('.likes-top-players');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching story top players: ", error);
            }
        });
    }

    function loadLikesRankings() {
        $.ajax({
            url: '/rank/likes',
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                let html = '';
                data.forEach((record, index) => {
                    let rankClass = "";
                    if (index === 0) {
                        rankClass = "rank-badge gold";
                    } else if (index === 1) {
                        rankClass = "rank-badge silver";
                    } else if (index === 2) {
                        rankClass = "rank-badge bronze";
                    }
                    html += `<tr class="animate__animated animate__fadeIn rank-${index + 1}">
                        <td><span class="${rankClass}">${index + 1}</span></td>
                        <td>
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td>${record.sharedBooks}</td>
                        <td>${record.totalHit}</td>
                        <td>${record.totalGaechu}</td>
                        <td>${record.sharedBookReplies}</td>
                        <td>${record.likeScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.likes-list').html(html);
                attachModalEvents('.likes-list');
            },
            error: function (xhr, status, error) {
                console.error("Error fetching likes ranking data: ", error);
            }
        });
    }

    // 초기 로딩: Overall 탭 데이터 로드 (상위 3명와 전체 테이블)
    loadOverallTopPlayers();
    loadOverallRankings();

    // jQuery 탭 클릭 이벤트: 각 탭 클릭 시 해당 함수 호출 및 동적 이벤트 재설정
    $('#overall-tab').on('click', function () {
        loadOverallTopPlayers();
        loadOverallRankings();
    });
    $('#kanji-tab').on('click', function () {
        loadKanjiTopPlayers();
        loadKanjiRankings();
    });
    $('#story-tab').on('click', function () {
        loadStoryTopPlayers();
        loadStoryRankings();
    });
    $('#likes-tab').on('click', function () {
        loadLikesTopPlayers();
        loadLikesRankings();
    });

    // 공통: 스크롤 애니메이션 초기화
    window.addEventListener('scroll', function () {
        const elements = document.querySelectorAll('.player, .card, .header-content');
        elements.forEach(element => {
            const elementPosition = element.getBoundingClientRect().top;
            const screenPosition = window.innerHeight / 1.3;
            if (elementPosition < screenPosition && !element.classList.contains('animate__animated')) {
                element.classList.add('animate__animated', 'animate__fadeIn');
            }
        });
    });
    setTimeout(() => {
        // Initial check for scroll animations
        const elements = document.querySelectorAll('.card, .leaderboard-title, .filters, header h2, .tab-pane');
        elements.forEach(element => {
            const elementPosition = element.getBoundingClientRect().top;
            const screenPosition = window.innerHeight / 1.3;
            if (elementPosition < screenPosition && !element.classList.contains('animate__animated')) {
                element.classList.add('animate__animated', 'animate__fadeIn');
            }
        });
    }, 200);

    // Bootstrap 툴팁 초기화
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.forEach(tooltipTriggerEl => {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Top Players 애니메이션 for any existing .player elements
    const topPlayers = document.querySelectorAll('.player');
    topPlayers.forEach((player, index) => {
        player.style.opacity = '0';
        player.style.transform = 'translateY(20px)';
        setTimeout(() => {
            player.style.transition = 'all 0.6s ease-out';
            player.style.opacity = '1';
            player.style.transform = 'translateY(0)';
            const scoreElement = player.querySelector('.score');
            if (scoreElement) {
                const finalScore = parseInt(scoreElement.textContent.replace(/,/g, ''));
                let startScore = 0;
                const duration = 2000;
                const frameRate = 20;
                const increment = Math.ceil(finalScore / (duration / frameRate));
                scoreElement.textContent = '0';
                const counter = setInterval(() => {
                    startScore += increment;
                    if (startScore >= finalScore) {
                        startScore = finalScore;
                        clearInterval(counter);
                    }
                    scoreElement.textContent = startScore.toLocaleString();
                }, frameRate);
            }
        }, 300 + index * 200);
    });

});
