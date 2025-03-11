/* -------------------------------
   (1) JLPT 통계 (게이지 차트)
------------------------------- */
const statsData = window.statsData;
const rankTotals = {
    "전체": 2136,
    "N1": 1139,
    "N2": 710,
    "N3": 182,
    "N4": 76,
    "N5": 29
};

const centerTextPlugin = {
    id: 'centerTextPlugin',
    afterDraw(chart) {
        const opts = chart.config.options.plugins.centerTextPlugin || {};
        const partial = opts.partial || 0;
        const total = opts.total || 1;
        const label = opts.label || 'ALL';
        const percentage = Math.floor((partial / total) * 100);
        const { ctx, chartArea: { top, left, width, height } } = chart;
        const centerX = left + width / 2;
        const centerY = top + height / 2;
        ctx.save();
        ctx.textAlign = 'center';
        ctx.fillStyle = '#333';
        ctx.font = '16px sans-serif';
        ctx.fillText(label, centerX, centerY - 20);
        ctx.font = '16px sans-serif';
        ctx.fillText(`${partial}/${total}`, centerX, centerY + 20);
        ctx.font = '23px sans-serif';
        ctx.fillText(`${percentage}%`, centerX, centerY + 50);
        ctx.restore();
    }
};

function createGaugeChart(canvasId, partial, total, label) {
    const percentage = Math.floor((partial / total) * 100);
    const remainValue = 100 - percentage;
    const ctx = document.getElementById(canvasId).getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            datasets: [{
                data: [percentage, remainValue],
                backgroundColor: ['#EE697D', '#ddd'],
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            cutout: '70%',
            rotation: -150,
            circumference: 300,
            plugins: {
                centerTextPlugin: { partial, total, label },
                legend: { display: false },
                tooltip: { enabled: false }
            },
            animation: {
                duration: 2500,
                animateRotate: true,
                animateScale: false,
                onComplete: (anim) => { anim.chart.config.options.animation = false; }
            }
        },
        plugins: [centerTextPlugin]
    });
}

/* -------------------------------
   (2) 날짜별 Bar 차트 데이터
------------------------------- */
const barChartLabels = window.dailyStatsData.map(ds => ds.acquisitionDate);
const barChartValues = window.dailyStatsData.map(ds => ds.dailyCount);

/* -------------------------------
   DOMContentLoaded: 모든 이벤트 리스너 등록
------------------------------- */
document.addEventListener('DOMContentLoaded', () => {
    // Gauge 차트 생성
    statsData.forEach(stat => {
        const rank = stat.jlptRank;
        const collected = stat.collected;
        const total = rankTotals[rank] || 0;
        const canvasId = 'gaugeChart' + rank;
        createGaugeChart(canvasId, collected, total, rank);
    });

    // 모든 chart-container에 클릭 이벤트 등록 (중복 제거)
    document.querySelectorAll('.chart-container[data-jlpt-rank]').forEach(container => {
        container.addEventListener('click', function () {
            const rank = this.getAttribute('data-jlpt-rank');
            const url = rank === '전체'
                ? '/kanji/collection'
                : `/kanji/collection?kanjiSort=kanjiId&sortDirection=asc&category=&jlptRank=${encodeURIComponent(rank)}&kanjiSearch=`;
            window.location.href = url;
        });
    });

    // Bar 차트 생성
    const ctxBar = document.getElementById('lineChart').getContext('2d');
    new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: barChartLabels,
            datasets: [{
                label: '일자별 획득 한자 수',
                data: barChartValues,
                backgroundColor: '#EE697D',
                borderColor: '#EE697D',
                borderWidth: 1,
                maxBarThickness: 50
            }]
        },
        options: {
            responsive: true,
            scales: {
                x: { categoryPercentage: 0.5, barPercentage: 0.8 },
                y: { beginAtZero: true }
            },
            animation: { duration: 2500 },
            plugins: { title: { display: false } }
        },
    });

    // (3) 카테고리 목록 페이지네이션
    const ITEMS_PER_PAGE = 5;
    document.querySelectorAll('.horizontal-container').forEach(container => {
        const ul = container.querySelector('ul.horizontal-list');
        const liArray = Array.from(ul.querySelectorAll('li'));
        const prevBtn = container.querySelector('.prev-btn');
        const nextBtn = container.querySelector('.next-btn');
        let currentPage = 0;
        const totalPages = Math.ceil(liArray.length / ITEMS_PER_PAGE);

        function renderPage(page) {
            currentPage = Math.min(Math.max(page, 0), totalPages - 1);
            liArray.forEach(li => li.style.display = 'none');
            liArray.slice(currentPage * ITEMS_PER_PAGE, currentPage * ITEMS_PER_PAGE + ITEMS_PER_PAGE)
                .forEach(li => li.style.display = 'flex');
        }
        renderPage(0);
        prevBtn.addEventListener('click', e => {
            e.stopPropagation();
            if (currentPage > 0) renderPage(currentPage - 1);
        });
        nextBtn.addEventListener('click', e => {
            e.stopPropagation();
            if (currentPage < totalPages - 1) renderPage(currentPage + 1);
        });
    });

    // (4) 카테고리 달성도 ProgressBar 애니메이션
    const progressData = [];
    document.querySelectorAll('.progress-bar-container').forEach(container => {
        const progressBar = container.querySelector('progress');
        const progressText = container.querySelector('.progress-text');
        const serverValue = parseFloat(progressBar.value);
        progressBar.value = 0;
        progressText.textContent = '0%';
        progressData.push({
            progressBar,
            progressText,
            targetValue: serverValue,
            startTime: performance.now()
        });
    });
    const duration = 1500;
    function updateAllProgressBars() {
        const currentTime = performance.now();
        let allCompleted = true;
        progressData.forEach(item => {
            const elapsedTime = currentTime - item.startTime;
            const progress = Math.min(elapsedTime / duration, 1);
            const currentValue = progress * item.targetValue;
            item.progressBar.value = currentValue;
            const percentage = item.progressBar.max > 0 ? Math.floor((currentValue / item.progressBar.max) * 100) : 0;
            item.progressText.textContent = `${percentage}%`;
            if (percentage <= 20) {
                item.progressBar.style.setProperty('--progress-color', '#42405C');
            } else if (percentage <= 40) {
                item.progressBar.style.setProperty('--progress-color', '#727B98');
            } else if (percentage <= 60) {
                item.progressBar.style.setProperty('--progress-color', '#EFA69A');
            } else if (percentage <= 80) {
                item.progressBar.style.setProperty('--progress-color', '#EE697D');
            } else {
                item.progressBar.style.setProperty('--progress-color', '#F0C9D8');
            }
            if (progress < 1) allCompleted = false;
        });
        if (!allCompleted) requestAnimationFrame(updateAllProgressBars);
    }
    updateAllProgressBars();

    // (5) 달성률 정렬 아이콘 클릭 이벤트
    const sortIcon = document.querySelector('.category-sort-icon');
    if (sortIcon) {
        let isDesc = true;
        sortIcon.addEventListener('click', () => {
            const container = document.getElementById('categoryScrollContainer');
            if (!container) return;
            const blocks = Array.from(container.querySelectorAll('.category-block'));
            blocks.sort((a, b) => {
                const ratioA = (parseInt(a.dataset.collected) || 0) / (parseInt(a.dataset.total) || 1);
                const ratioB = (parseInt(b.dataset.collected) || 0) / (parseInt(b.dataset.total) || 1);
                return isDesc ? ratioB - ratioA : ratioA - ratioB;
            });
            blocks.forEach(block => container.appendChild(block));
            isDesc = !isDesc;
        });
    }

    // (6) 카테고리 블록 클릭 -> 이동
    document.querySelectorAll('.category-block').forEach(block => {
        block.addEventListener('click', () => {
            const targetUrl = block.dataset.url;
            if (targetUrl) window.location.href = targetUrl;
        });
    });

    // (7) 한자 상세정보 모달 (kanji-collected/kanji-not-collected)
    document.querySelectorAll('.kanji-collected, .kanji-not-collected').forEach(kanjiEl => {
        kanjiEl.addEventListener('click', e => {
            e.stopPropagation();
            const kanjiId = kanjiEl.getAttribute('data-kanji-id');
            fetch(`/kanji/details?kanjiId=${kanjiId}`)
                .then(response => {
                    if (!response.ok) throw new Error("Network response was not ok");
                    return response.json();
                })
                .then(detail => {
                    const detailModal = document.getElementById('kanjiDetailModal');
                    const modalBody = detailModal.querySelector('.modal-body');
                    let obtainedDisplay = "미수집";
                    if (detail.obtainedAt) {
                        const date = new Date(detail.obtainedAt);
                        const yyyy = date.getFullYear();
                        const mm = String(date.getMonth() + 1).padStart(2, '0');
                        const dd = String(date.getDate()).padStart(2, '0');
                        obtainedDisplay = `${yyyy}-${mm}-${dd}`;
                    }
                    const formattedMeaning = detail.meaning
                        .replace(/;/g, ',')
                        .replace(/\\n/g, '<br/>');
                    modalBody.innerHTML = `
                        <div class="toKanjiList">
                            <div class="detail-header">
                                <div class="detailKanjiId">No. ${detail.kanjiId}</div>
                                <div class="detailJlptRank">${detail.jlptRank}</div>
                            </div>
                            <div class="detailKanjiCard">
                                <span class="detailKanji">${detail.kanji}</span>
                                <div class="detailKanjiExplain">
                                    <div class="detailYomi">${detail.korKunyomi} ${detail.korOnyomi}</div>
                                    <div>음독 : ${detail.jpnOnyomi}</div>
                                    <div>훈독 : ${detail.jpnKunyomi}</div>
                                    <pre>의미: <br/>${formattedMeaning}</pre>
                                </div>
                            </div>
                            <div class="detail-info">
                                <div class="detailCategory">카테고리 : ${detail.category}</div>
                                <div class="detailCreatedAt">날짜: ${obtainedDisplay}</div>
                            </div>
                        </div>
                    `;
                    new bootstrap.Modal(detailModal, {}).show();
                })
                .catch(error => console.error('Error fetching kanji details:', error));
        });
    });
});
