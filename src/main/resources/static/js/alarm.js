// 웹소켓 연결 (서버 주소에 맞춰서 연결)
// 보안이 적용된 사이트면 wss://, 아니면 ws:// 를 사용
// 현재 페이지의 프로토콜(http/https)에 맞춰 ws/wss를 결정하고 주소를 가져옴
var protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
var host = window.location.host; // localhost:8080 같은 주소를 가져옴
var socket = new WebSocket(protocol + '//' + host + '/alarm-ws');


// 연결 성공시 실행
socket.onopen = function() {
    console.log('실시간 알림 서버와 연결되었습니다.');
};

// 서버로부터 메시지(알림)를 받았을 때 실행
socket.onmessage = function(event) {
    var alarmMessage = event.data;
    console.log('새 알림 도착: ' + alarmMessage);

    // 알림창을 띄우는 코드
    alert("🔔 알림: " + alarmMessage);
};

// 연결이 끊겼을 때 실행
socket.onclose = function() {
    console.log('서버 연결이 종료되었습니다.');
};