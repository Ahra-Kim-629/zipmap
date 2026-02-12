document.addEventListener("DOMContentLoaded", function() {
    // 페이지 로드 시 모든 팝업 체크
    document.querySelectorAll('.main-popup').forEach(popup => {
        const id = popup.id.split('_')[1];
        if (getCookie('hidePopup_' + id)) {
            popup.style.display = 'none'; // 쿠키 있으면 숨김
        }
    });
});

// 팝업 닫기 함수
function closePopup(id, isTodayHide) {
    if (isTodayHide) {
        setCookie('hidePopup_' + id, 'Y', 1); // 1일 동안 유지되는 쿠키 저장
    }
    document.getElementById('popup_' + id).style.display = 'none';
}

// 쿠키 설정 함수
function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = name + '=' + value + ';expires=' + date.toUTCString() + ';path=/';
}

// 쿠키 가져오기 함수
function getCookie(name) {
    const value = "; " + document.cookie;
    const parts = value.split("; " + name + "=");
    if (parts.length === 2) return parts.pop().split(";").shift();
}