// 웹소켓 연결 (서버 주소에 맞춰서 연결)
// 보안이 적용된 사이트면 wss://, 아니면 ws:// 를 사용
// 현재 페이지의 프로토콜(http/https)에 맞춰 ws/wss를 결정하고 주소를 가져옴
var protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
var host = window.location.host; // localhost:8080 같은 주소를 가져옴
// 현재 접속한 도메인과 프로토콜을 자동으로 따라가게 설정
var socket = new SockJS('/alarm-ws');
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
    const alarmId = parts[2]; // 서버에서 알림 ID도 같이 보내준다고 가정 (없다면 ID 없이 처리)

    // 화면에 삽입할 HTML 생성
    let alarmHtml = `
    <div class="custom-alarm-item">
            <div style="display: flex; align-items: start; gap: 12px; margin-bottom: 15px;">
                <div style="background: #f0f7f6; padding: 8px; border-radius: 12px;">
                    <i class="bi bi-bell-fill" style="color: #3f8a7e; font-size: 1.2rem;"></i>
                </div>
                <div style="font-size: 15px; color: #333; line-height: 1.5; font-weight: 600; padding-top: 5px;">
                    ${message}
                </div>
            </div>
            <div style="display: flex; gap: 8px; justify-content: flex-end;">
                <button class="alarm-btn-close" onclick="$(this).closest('.custom-alarm-item').fadeOut(300, function(){ $(this).remove(); })">
                    닫기
                </button>
                <button class="alarm-btn-confirm" onclick="readAndMove('${moveUrl}', '${alarmId}')">
                    확인하기
                </button>
            </div>
        </div>
    `;

    // 알림창 바구니(div)에 넣기 (id가 alarmList인 요소가 있다고 가정)
    $('#alarm-container').prepend(alarmHtml);

    // 마이페이지 버튼 숫자를 +1
    updateAlarmBadge();
};

// [추가] 읽음 처리 후 페이지 이동 함수
function readAndMove(url, id) {
    // 1. 서버에 읽음 처리 요청 (AJAX)
    $.ajax({
        url: '/alarm/read/' + id, // 서버의 읽음 처리 API 주소
        method: 'POST',
        beforeSend: function(xhr) {
            const token = $("meta[name='_csrf']").attr("content");
            const header = $("meta[name='_csrf_header']").attr("content");
            if (token && header) xhr.setRequestHeader(header, token);
        },
        success: function() {
            // 2. 읽음 처리 성공 시 페이지 이동
            location.href = url;
        },
        error: function() {
            // 에러가 나더라도 이동은 시켜줍니다
            location.href = url;
        }
    });
}

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

