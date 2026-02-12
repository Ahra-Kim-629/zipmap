document.addEventListener("DOMContentLoaded", function() {
    // 모든 모달 요소를 찾아 쿠키 확인 후 노출
    document.querySelectorAll('.modal[id^="modal_"]').forEach(modalEl => {
        const id = modalEl.id.split('_')[1];
        if (!getCookie('hideModal_' + id)) {
            const modalInstance = new bootstrap.Modal(modalEl);
            modalInstance.show();
        }
    });
});

// 체크박스 클릭 시 쿠키 설정 후 모달 닫기
function closeModalWithCookie(id) {
    setCookie('hideModal_' + id, 'Y', 1); // 1일 저장
    const modalEl = document.getElementById('modal_' + id);
    const modalInstance = bootstrap.Modal.getInstance(modalEl);

    // 약간의 지연 후 닫아서 체크 상태를 인지하게 함
    setTimeout(() => {
        modalInstance.hide();
    }, 300);
}

// 쿠키 관련 함수 (동일)
function setCookie(name, value, days) {
    const date = new Date();
    date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
    document.cookie = name + '=' + value + ';expires=' + date.toUTCString() + ';path=/';
}

function getCookie(name) {
    const value = "; " + document.cookie;
    const parts = value.split("; " + name + "=");
    if (parts.length === 2) return parts.pop().split(";").shift();
}