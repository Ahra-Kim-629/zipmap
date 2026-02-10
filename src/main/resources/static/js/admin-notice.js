document.addEventListener('DOMContentLoaded', function() {
    // 1. 시작 날짜 설정
    const startPicker = flatpickr("#startDate", {
        enableTime: false,
        dateFormat: "Y-m-d",
        locale: "ko",
        minDate: "today", // 시작일은 오늘부터만 선택 가능
        onChange: function(selectedDates, dateStr) {
            // 시작 날짜가 선택되면 종료 날짜의 최소일을 선택된 시작 날짜로 제한
            endPicker.set('minDate', dateStr);
        }
    });

    // 2. 종료 날짜 설정
    const endPicker = flatpickr("#endDate", {
        enableTime: false,
        dateFormat: "Y-m-d",
        locale: "ko",
        minDate: "today" // 기본적으로 오늘부터 선택 가능
        onChange: function(selectedDates, dateStr) {
            // 종료 날짜가 선택되면 시작 날짜의 최대일을 선택된 종료 날짜로 제한
            startPicker.set('maxDate', dateStr);
        }
    });
});