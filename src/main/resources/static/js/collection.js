$(function () {
    let currentPage = 1, hasMore = true;
    const gridContainer = document.getElementById('cardContainer');
    const activeLayer = document.getElementById('activeLayer');
    let activeCard = null;
    const originalData = new Map();
    let isAnimating = false;

    // 무한 스크롤 & 스크롤 화살표
    $(window).on("scroll", function () {
        if ($(window).scrollTop() + $(window).height() >= $(document).height() - 100 && hasMore) {
            loadNextPage();
        }
        $("#scrollArrow").toggle($(window).scrollTop() + $(window).height() < $(document).height() - 10);
        if (activeCard) keepCardCentered(activeCard);
    });
    $("#scrollArrow").on("click", function () {
        $("html, body").animate({ scrollTop: $(window).scrollTop() + 300 }, 400);
    });
    function updateArrow() { /* 이미 위에서 처리 */ }



    function loadNextPage() {
        currentPage++;
        let queryParams = $("#searchForm").serialize() + "&page=" + currentPage;
        $.ajax({
            url: "/kanji/collectionAjax", method: "GET", data: queryParams,
            success: function (html) {
                if ($.trim(html).length < 10) { hasMore = false; }
                $("#cardContainer").append(html);
                // 새로 추가된 카드에 VanillaTilt 적용
                VanillaTilt.init(document.querySelectorAll("#cardContainer .kanjiCard, #cardContainer .notKanjiCard"), {
                    max: 15, speed: 400, glare: true, "max-glare": 0.2,
                });

                if (document.querySelectorAll('.border-lv0').length) {
                    gsap.to('.border-lv0', {
                        boxShadow: '0 0 20px rgba(125,125,125,0.8)',
                        duration: 2,
                        repeat: -1,
                        yoyo: true,
                        ease: 'power1.inOut'
                    });
                }
                if (document.querySelectorAll('.border-lv1').length) {
                    gsap.to('.border-lv1', {
                        boxShadow: '0 0 20px rgba(0,99,255,0.8)',
                        duration: 2,
                        repeat: -1,
                        yoyo: true,
                        ease: 'power1.inOut'
                    });
                }
                if (document.querySelectorAll('.border-lv2').length) {
                    gsap.to('.border-lv2', {
                        boxShadow: '0 0 20px rgba(148,0,211,0.8)',
                        duration: 2,
                        repeat: -1,
                        yoyo: true,
                        ease: 'power1.inOut'
                    });
                }
                if (document.querySelectorAll('.border-lv3').length) {
                    gsap.to('.border-lv3', {
                        boxShadow: '0 0 20px rgba(255,165,0,0.8)',
                        duration: 2,
                        repeat: -1,
                        yoyo: true,
                        ease: 'power1.inOut'
                    });
                }

                $(document).on('mouseenter', '.border-lv0, .border-lv1, .border-lv2, .border-lv3', function () {
                    gsap.to(this, { scale: 1.04, duration: 0.3, ease: "power1.out" });
                });
                $(document).on('mouseleave', '.border-lv0, .border-lv1, .border-lv2, .border-lv3', function () {
                    gsap.to(this, { scale: 1, duration: 0.3, ease: "power1.out" });
                });

            },
            error: function () { hasMore = false; }
        });
    }

    // 모든 레벨에 대해 hover 시 scale 효과 통합
    $(document).on('mouseenter', '.border-lv1, .border-lv2, .border-lv3', function () {
        gsap.to(this, { scale: 1.04, duration: 0.3, ease: "power1.out" });
    });
    $(document).on('mouseleave', '.border-lv1, .border-lv2, .border-lv3', function () {
        gsap.to(this, { scale: 1, duration: 0.3, ease: "power1.out" });
    });

    // 활성 카드 처리
    function activateCard(card) {
        // tilt 효과 제거하여 중앙 이동 시 깔끔하게 보이도록
        if (card.vanillaTilt) { card.vanillaTilt.destroy(); }
        if (isAnimating) return; isAnimating = true; activeCard = card;
        storeIfNeeded(card);
        const orig = originalData.get(card);
        // placeholder 생성
        const placeholder = document.createElement('div');
        placeholder.className = 'placeholder';
        placeholder.style.width = orig.rect.width + 'px';
        placeholder.style.height = orig.rect.height + 'px';
        orig.container.replaceChild(placeholder, card);
        orig.placeholder = placeholder;
        activeLayer.appendChild(card);
        const fixedLeft = orig.rect.left - window.scrollX;
        const fixedTop = orig.rect.top - window.scrollY;
        setFixedPosition(card, fixedLeft, fixedTop, orig.rect.width, orig.rect.height);
        const { targetLeft, targetTop, targetWidth, targetHeight } = getCenterPos(orig.rect);
        gsap.fromTo(card, { rotationY: 0 }, {
            duration: 0.8, rotationY: 360, left: targetLeft, top: targetTop,
            width: targetWidth, height: targetHeight, ease: "power2.inOut",
            onUpdate: function () { if (this.progress() > 0.5) showBack(card); },
            onComplete: () => { isAnimating = false; gsap.set(card, { clearProps: "transform" }) }
        });
    }

    function restoreCard(card, callback) {
        if (isAnimating && !callback) return;
        if (!originalData.has(card)) { if (callback) callback(); return; }
        isAnimating = true;
        const orig = originalData.get(card);
        gsap.fromTo(card, { rotationY: 360 }, {
            duration: 0.8, rotationY: 720, left: orig.rect.left - window.scrollX,
            top: orig.rect.top - window.scrollY, width: orig.rect.width, height: orig.rect.height,
            ease: "power2.inOut", onUpdate: function () { if (this.progress() > 0.5) showFront(card); },
            onComplete: () => {
                finalizeRestore(card); activeCard = null; isAnimating = false;
                // 재초기화: 복귀한 카드에 다시 VanillaTilt 적용
                VanillaTilt.init(card, { max: 15, speed: 400, glare: true, "max-glare": 0.2 });
                if (callback) callback();
            }
        });
    }

    function swapCards(oldCard, newCard) {
        if (isAnimating) return; isAnimating = true;
        storeIfNeeded(oldCard); storeIfNeeded(newCard);
        const oldOrig = originalData.get(oldCard);
        const newOrig = originalData.get(newCard);
        const placeholder = document.createElement('div');
        placeholder.className = 'placeholder';
        placeholder.style.width = newOrig.rect.width + 'px';
        placeholder.style.height = newOrig.rect.height + 'px';
        newOrig.container.replaceChild(placeholder, newCard);
        newOrig.placeholder = placeholder;
        activeLayer.appendChild(newCard);
        setFixedPosition(newCard, newOrig.rect.left - window.scrollX, newOrig.rect.top - window.scrollY, newOrig.rect.width, newOrig.rect.height);
        const newCenter = getCenterPos(newOrig.rect);
        const oldRect = oldOrig.rect;
        const tl = gsap.timeline({
            onComplete: () => { finalizeRestore(oldCard); activeCard = newCard; isAnimating = false; }
        });
        tl.fromTo(oldCard, { rotationY: 360 }, {
            duration: 0.6, rotationY: 720, left: oldRect.left - window.scrollX, top: oldRect.top - window.scrollY,
            width: oldRect.width, height: oldRect.height, ease: "power2.inOut",
            onUpdate: function () { if (this.progress() > 0.5) showFront(oldCard); }
        }, 0);
        tl.fromTo(newCard, { rotationY: 0 }, {
            duration: 0.6, rotationY: 360, left: newCenter.targetLeft, top: newCenter.targetTop,
            width: newCenter.targetWidth, height: newCenter.targetHeight, ease: "power2.inOut",
            onUpdate: function () { if (this.progress() > 0.5) showBack(newCard); }
        }, 0);
    }

    function keepCardCentered(card) {
        if (!originalData.has(card)) return;
        const orig = originalData.get(card);
        const { targetLeft, targetTop } = getCenterPos(orig.rect);
        gsap.to(card, { duration: 0.3, left: targetLeft, top: targetTop, ease: "power1.out" });
    }

    function finalizeRestore(card) {
        if (!originalData.has(card)) return;
        const orig = originalData.get(card);
        card.style.position = ''; card.style.left = ''; card.style.top = '';
        card.style.width = ''; card.style.height = ''; card.style.zIndex = '';
        showFront(card);
        if (orig.placeholder && orig.container.contains(orig.placeholder)) {
            orig.container.replaceChild(card, orig.placeholder);
        } else { orig.container.appendChild(card); }
        originalData.delete(card);
    }

    function storeIfNeeded(card) {
        if (!originalData.has(card)) {
            originalData.set(card, { rect: getCardRect(card), container: card.parentElement });
        }
    }

    function showFront(card) {
        const front = card.querySelector('.card-front');
        const back = card.querySelector('.card-back');
        if (front) front.style.display = 'block';
        if (back) back.classList.remove('active');
    }
    function showBack(card) {
        const front = card.querySelector('.card-front');
        const back = card.querySelector('.card-back');
        if (front) front.style.display = 'none';
        if (back) back.classList.add('active');
    }
    function setFixedPosition(card, left, top, w, h) {
        card.style.position = 'fixed';
        card.style.left = left + 'px';
        card.style.top = top + 'px';
        card.style.width = w + 'px';
        card.style.height = h + 'px';
        card.style.zIndex = 9999;
    }
    function getCenterPos(rect) {
        const scale = 1.7;
        const w = rect.width * scale;
        const h = rect.height * scale;
        const l = (window.innerWidth - w) / 2;
        const t = (window.innerHeight - h) / 2;
        return { targetLeft: l, targetTop: t, targetWidth: w, targetHeight: h };
    }
    function getCardRect(card) {
        const rect = card.getBoundingClientRect();
        return { left: rect.left + window.scrollX, top: rect.top + window.scrollY, width: rect.width, height: rect.height };
    }

    // 카드 클릭 이벤트
    gridContainer.addEventListener('click', (e) => {
        const card = e.target.closest('.kanjiCard, .notKanjiCard');
        if (!card) return;
        e.stopPropagation();
        if (isAnimating) return;
        if (activeCard && activeCard !== card) { swapCards(activeCard, card); }
        else if (activeCard === card) { return; }
        else { activateCard(card); }
    });
    document.addEventListener('click', (e) => {
        if (activeCard && !activeCard.contains(e.target)) restoreCard(activeCard);
    });
    window.addEventListener('resize', () => { if (activeCard) keepCardCentered(activeCard); });
});

// 초기 VanillaTilt 적용
VanillaTilt.init(document.querySelectorAll(".kanjiCard, .notKanjiCard"), {
    max: 15, speed: 400, glare: true, "max-glare": 0.2,
});

if (document.querySelectorAll('.border-lv0').length) {
    gsap.to('.border-lv0', {
        boxShadow: '0 0 20px rgba(125,125,125,0.8)',
        duration: 2,
        repeat: -1,
        yoyo: true,
        ease: 'power1.inOut'
    });
}
if (document.querySelectorAll('.border-lv1').length) {
    gsap.to('.border-lv1', {
        boxShadow: '0 0 20px rgba(0,99,255,0.8)',
        duration: 2,
        repeat: -1,
        yoyo: true,
        ease: 'power1.inOut'
    });
}
if (document.querySelectorAll('.border-lv2').length) {
    gsap.to('.border-lv2', {
        boxShadow: '0 0 20px rgba(148,0,211,0.8)',
        duration: 2,
        repeat: -1,
        yoyo: true,
        ease: 'power1.inOut'
    });
}
if (document.querySelectorAll('.border-lv3').length) {
    gsap.to('.border-lv3', {
        boxShadow: '0 0 20px rgba(255,165,0,0.8)',
        duration: 2,
        repeat: -1,
        yoyo: true,
        ease: 'power1.inOut'
    });
}

document.addEventListener("visibilitychange", function () {
    if (document.hidden) {
        gsap.globalTimeline.pause();
    } else {
        gsap.globalTimeline.resume();
    }
});


// 페이지 로드 시 스크립트 실행
document.addEventListener('DOMContentLoaded', function () {
    const scrollToTopBtn = document.getElementById('scrollToTopBtn');

    // 스크롤 이벤트 리스너 추가
    window.addEventListener('scroll', function () {
        if (window.scrollY > 300) {
            scrollToTopBtn.style.display = 'flex';
        } else {
            scrollToTopBtn.style.display = 'none';
        }
    });

    // 버튼 클릭 시 맨 위로 스크롤
    scrollToTopBtn.addEventListener('click', function () {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    });
});