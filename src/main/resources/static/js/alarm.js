// 웹소켓 연결 (서버 주소에 맞춰서 연결)
// 보안이 적용된 사이트면 wss://, 아니면 ws:// 를 사용
// 현재 페이지의 프로토콜(http/https)에 맞춰 ws/wss를 결정하고 주소를 가져옴
var protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
var host = window.location.host; // localhost:8080 같은 주소를 가져옴
// 현재 접속한 도메인과 프로토콜을 자동으로 따라가게 설정
const socket = new SockJS('/alarm-ws')
// 연결 성공시 실행
socket.onopen = function() {
    console.log('실시간 알림 서버와 연결되었습니다.');
};

// 서버로부터 메시지(알림)를 받았을 때 실행
socket.onmessage = function(event) {
    // 받은 데이터를 메시지와 URL로 분리
    const parts = event.data.split('|');
    const message = parts[0];
    const moveUrl = parts[1] || '#';  // URL이 없으면 기본값 #

    // 화면에 삽입할 HTML 생성
    let alarmHtml = `
    <div class="alarm-item" style="background: #ffffff; border-left: 5px solid #ff8a3d; border-radius: 12px; padding: 16px; margin-bottom: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.08); animation: slideIn 0.3s ease-out;">
        <div style="display: flex; align-items: start; gap: 10px; margin-bottom: 12px;">
            <i class="bi bi-bell-fill" style="color: #ff8a3d; font-size: 1.1rem;"></i>
            <div style="font-size: 14px; color: #333; line-height: 1.5; font-weight: 500;">${message}</div>
        </div>
        <div style="display: flex; gap: 8px; justify-content: flex-end;">
            <button onclick="location.href='${moveUrl}'"
                style="background: #ff8a3d; color: white; border: none; padding: 6px 14px; border-radius: 20px; font-size: 12px; font-weight: bold; cursor: pointer; transition: background 0.2s;">
                확인하기
            </button>
            <button onclick="$(this).closest('.alarm-item').fadeOut(300, function(){ $(this).remove(); })"
                style="background: #f4f4f4; color: #888; border: none; padding: 6px 14px; border-radius: 20px; font-size: 12px; cursor: pointer;">
                닫기
            </button>
        </div>
    </div>
    `;

    // 알림창 바구니(div)에 넣기 (id가 alarmList인 요소가 있다고 가정)
    $('#alarm-container').prepend(alarmHtml);

    // 마이페이지 버튼 숫자를 +1
    updateAlarmBadge();
};

// 연결이 끊겼을 때 실행
socket.onclose = function() {
    console.log('서버 연결이 종료되었습니다.');
};

// 페이지 로드시 안읽은 알림 갯수 업데이트
$(document).ready(function() {
    updateAlarmBadge();
});

function updateAlarmBadge() {
    $.ajax({
        url: '/alarm/unread-count',
        method: 'GET',
        success: function(count) {
            const badge = $('#unread-count');
            if (count > 0) {
                badge.text(count);
               badge.show().css({
                   'display': 'inline-block',
                   'background': '#ff4d4f',
                   'color': 'white',
                   'border-radius': '50%',
                   'padding': '2px 6px',
                   'font-size': '10px',
                   'font-weight': 'bold'
               });
            } else {
                badge.hide();
            }
        },
        error: function(e) {
            console.error("뱃지 업데이트 실패", e);
        }
    });
}

