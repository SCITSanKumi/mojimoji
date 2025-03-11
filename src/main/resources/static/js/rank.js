document.addEventListener('DOMContentLoaded', function () {
    const currentUserId = $("#currentUserId").val();


    let celebrating = false;
    // confetti 초기화 (we attach the event on dynamic elements later)
    function attachCelebrateBtnEvents(containerSelector) {
        // Use delegated event listener so even dynamic celebrate-btns work.
        $(containerSelector).off('click', '.celebrate-btn').on('click', '.celebrate-btn', function () {
            if (celebrating) return;
            celebrating = true;
            const end = Date.now() + (3 * 1000);
            const colors = ['#FFD700', '#FFF', '#FF6B6B'];
            (function frame() {
                confetti({ particleCount: 3, angle: 60, spread: 55, origin: { x: 0 }, colors: colors });
                confetti({ particleCount: 3, angle: 120, spread: 55, origin: { x: 1 }, colors: colors });
                if (Date.now() < end) {
                    requestAnimationFrame(frame);
                } else {
                    celebrating = false; // 애니메이션 종료 후 플래그 해제
                }
            }());
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
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${second.kanjiScore.toFixed(1)}</span></div>
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
                            
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${first.kanjiScore.toFixed(1)}</span></div>
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
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.totalScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${third.kanjiScore.toFixed(1)}</span></div>
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
                        ${currentUserId == record.userId
                            ? `<a href="/mypage">`
                            : `<a href="/user/otherProfile?userId=${record.userId}">`}
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td><i class="fas fa-scroll"></i> ${record.kanjiScore.toFixed(1)}</td>
                        <td><i class="fas fa-book"></i> ${record.bookScore.toFixed(1)}</td>
                        <td><i class="fas fa-thumbs-up"></i> ${record.likeScore.toFixed(1)}</td>
                        <td>${record.totalScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.players-list').html(html);
                // Reattach modal events for table rows
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
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${second.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-percent"></i><span>${second.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-layer-group"></i><span>${second.kanjiCollectionBonus}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${first.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-percent"></i><span>${first.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-layer-group"></i><span>${first.kanjiCollectionBonus}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.kanjiScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-scroll"></i><span>${third.kanjiCollections}</span></div>
                            <div class="stat"><i class="fas fa-percent"></i><span>${third.collectionPercentage.toFixed(1)}</span></div>
                            <div class="stat"><i class="fas fa-layer-group"></i><span>${third.kanjiCollectionBonus}</span></div>
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
                            ${currentUserId == record.userId
                            ? `<a href="/mypage">`
                            : `<a href="/user/otherProfile?userId=${record.userId}">`}
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td><i class="fas fa-scroll"></i> ${record.kanjiCollections} / 2136</td>
                        <td>${record.collectionPercentage.toFixed(1)}%</td>
                        <td>${record.kanjiCollectionBonus}</td>
                        <td>${record.kanjiScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.kanji-list').html(html);
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
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-book"></i><span>${second.books}</span></div>
                            <div class="stat"><i class="fas fa-book-open"></i><span>${second.bookLines}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-book"></i><span>${first.books}</span></div>
                            <div class="stat"><i class="fas fa-book-open"></i><span>${first.bookLines}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.bookScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-book"></i><span>${third.books}</span></div>
                            <div class="stat"><i class="fas fa-book-open"></i><span>${third.bookLines}</span></div>
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
                            ${currentUserId == record.userId
                            ? `<a href="/mypage">`
                            : `<a href="/user/otherProfile?userId=${record.userId}">`}
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td><i class="fas fa-book"> ${record.books}</td>
                        <td><i class="fas fa-book-open"> ${record.bookLines}</td>
                        <td>${record.bookScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.story-list').html(html);
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
                        </div>
                        <h3>${second.nickname}</h3>
                        <p class="score">${second.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-thumbs-up"></i><span>${second.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-eye"></i><span>${second.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-comments"></i><span>${second.sharedBookReplies}</span></div>
                        </div>
                    </div>`;
                const htmlFirst = `
                    <div class="player first" id="firstPlaceCard">
                        <div class="crown"><i class="fas fa-crown animate__animated animate__swing animate__infinite"></i></div>
                        <div class="rank">1</div>
                        <div class="avatar">
                            <img src="${first.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${first.nickname}</h3>
                        <p class="score">${first.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-thumbs-up"></i><span>${first.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-eye"></i><span>${first.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-comments"></i><span>${first.sharedBookReplies}</span></div>
                        </div>
                        <button class="celebrate-btn"><i class="fas fa-gift"></i> Celebrate</button>
                    </div>`;
                const htmlThird = `
                    <div class="player third">
                        <div class="rank">3</div>
                        <div class="avatar">
                            <img src="${third.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                        </div>
                        <h3>${third.nickname}</h3>
                        <p class="score">${third.likeScore.toFixed(1)}</p>
                        <div class="stats">
                        <div class="stat"><i class="fas fa-thumbs-up"></i><span>${third.totalHit}</span></div>
                            <div class="stat"><i class="fas fa-eye"></i><span>${third.totalGaechu}</span></div>
                            <div class="stat"><i class="fas fa-comments"></i><span>${third.sharedBookReplies}</span></div>
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
                            ${currentUserId == record.userId
                            ? `<a href="/mypage">`
                            : `<a href="/user/otherProfile?userId=${record.userId}">`}
                            <div class="player-info">
                                <div class="user-avatar">
                                    <img src="${record.profileUrl || 'https://cdn2.iconfinder.com/data/icons/people-80/96/Picture1-1024.png'}" alt="Avatar">
                                </div>
                                <div class="name">${record.nickname}</div>
                            </div>
                        </td>
                        <td> ${record.sharedBooks}</td>
                        <td><i class="fas fa-thumbs-up"> ${record.totalHit}</td>
                        <td><i class="fas fa-eye"> ${record.totalGaechu}</td>
                        <td><i class="fas fa-comments"> ${record.sharedBookReplies}</td>
                        <td>${record.likeScore.toFixed(1)}</td>
                    </tr>`;
                });
                $('.likes-list').html(html);
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

});
