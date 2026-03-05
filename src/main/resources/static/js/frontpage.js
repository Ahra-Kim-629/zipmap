/**
 * 대동여집도 메인 페이지 전용 스크립트
 */
document.addEventListener("DOMContentLoaded", function() {
    // 1. 공지사항 모달 초기화
    initNoticeModal();

    // 2. 게시글 작성 시간 상대 시간으로 업데이트
    updateRelativeTimes();
});

/* ==========================================
   1. 공지사항 모달(Carousel) 관련 함수
   ========================================== */
function initNoticeModal() {
    const noticeModal = document.getElementById('noticeCarouselModal');
    if (!noticeModal) return;

    // '오늘 하루 보지 않기' 쿠키 확인 후 모달 노출
    const userId = window.currentUserId || 'GUEST';
    const version = window.noticeVersion || 0;
    const cookieName = `hideNoticeCarousel_${userId}_v${version}`;
    if (!getCookie(cookieName)) {
        const modalInstance = new bootstrap.Modal(noticeModal);
        modalInstance.show();
    }

    // 체크박스 이벤트 바인딩
    const hideCheck = document.getElementById('hideNoticeToday');
    if (hideCheck) {
        hideCheck.addEventListener('change', function(e) {
            if (e.target.checked) {
                setCookieUntilMidnight(cookieName, 'Y');
                setTimeout(() => {
                    bootstrap.Modal.getInstance(noticeModal).hide();
                }, 500);
            }
        });
    }
}

/* ==========================================
   2. 상대 시간(Time Ago) 관련 함수
   ========================================== */
function updateRelativeTimes() {
    // .text-muted-date 클래스를 가진 모든 요소를 찾아 변환
    document.querySelectorAll('.text-muted-date').forEach(el => {
        const dateVal = el.getAttribute('data-date');
        if (dateVal) {
            el.innerText = timeAgo(dateVal);
        }
    });
}

function timeAgo(dateString) {
    const now = new Date();
    const past = new Date(dateString);
    const diffInMs = now - past;
    const diffInSec = Math.floor(diffInMs / 1000);
    const diffInMin = Math.floor(diffInSec / 60);
    const diffInHr = Math.floor(diffInMin / 60);
    const diffInDay = Math.floor(diffInHr / 24);

    if (diffInSec < 60) return "방금 전";
    if (diffInMin < 60) return `${diffInMin}분 전`;
    if (diffInHr < 24) return `${diffInHr}시간 전`;
    if (diffInDay < 7) return `${diffInDay}일 전`;

    // 7일 이상은 YYYY.MM.DD 형식으로 표시
    return `${past.getFullYear()}.${String(past.getMonth() + 1).padStart(2, '0')}.${String(past.getDate()).padStart(2, '0')}`;
}

/* ==========================================
   3. 쿠키(Cookie) 유틸리티 함수
   ========================================== */
function setCookieUntilMidnight(name, value) {
    const date = new Date();
    // 오늘 밤 23시 59분 59초로 설정 (자정 초기화 방식)
    date.setHours(23, 59, 59, 999);
    document.cookie = name + '=' + value + ';expires=' + date.toUTCString() + ';path=/';
}

function getCookie(name) {
    const value = "; " + document.cookie;
    const parts = value.split("; " + name + "=");
    if (parts.length === 2) return parts.pop().split(";").shift();
}
