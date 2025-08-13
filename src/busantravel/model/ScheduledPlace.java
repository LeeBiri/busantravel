package busantravel.model;

import java.time.LocalDateTime;

public class ScheduledPlace {
    private int schedule_id; // 일정 장소 ID (PK)
    private String plan_id; //계획 ID
    private String p_id; //여행지 번호
    private String p_type; //여행지 구분
    private LocalDateTime scheduled_date; //방문 예정 일자
    private LocalDateTime expected_dutarion; //예상 체류 시간
    private int visit_order; //방문 예정 순서

    // 기존 생성자 (새로운 일정 추가 시 사용)
    public ScheduledPlace(String plan_id, String p_id, String p_type, LocalDateTime scheduled_date,
            LocalDateTime expected_dutarion, int visit_order) {
        this.plan_id = plan_id;
        this.p_id = p_id;
        this.p_type = p_type;
        this.scheduled_date = scheduled_date;
        this.expected_dutarion = expected_dutarion;
        this.visit_order = visit_order;
    }

    // DB 조회 결과를 모델 객체로 변환 시 사용할 생성자
    public ScheduledPlace(int schedule_id, String plan_id, String p_id, String p_type, LocalDateTime scheduled_date,
            LocalDateTime expected_dutarion, int visit_order) {
        this.schedule_id = schedule_id;
        this.plan_id = plan_id;
        this.p_id = p_id;
        this.p_type = p_type;
        this.scheduled_date = scheduled_date;
        this.expected_dutarion = expected_dutarion;
        this.visit_order = visit_order;
    }

    public int getSchedule_id() {
        return schedule_id;
    }

    public void setSchedule_id(int schedule_id) {
        this.schedule_id = schedule_id;
    }

    public String getplan_id() {
        return plan_id;
    }
    public void setplan_id(String plan_id) {
        this.plan_id = plan_id;
    }
    public String getP_id() {
        return p_id;
    }
    public void setP_id(String p_id) {
        this.p_id = p_id;
    }
    public String getP_type() {
        return p_type;
    }
    public void setP_type(String p_type) {
        this.p_type = p_type;
    }
    public LocalDateTime getScheduled_date() {
        return scheduled_date;
    }
    public void setScheduled_date(LocalDateTime scheduled_date) {
        this.scheduled_date = scheduled_date;
    }
    public LocalDateTime getExpected_dutarion() {
        return expected_dutarion;
    }
    public void setExpected_dutarion(LocalDateTime expected_dutarion) {
        this.expected_dutarion = expected_dutarion;
    }
    public int getVisit_order() {
        return visit_order;
    }
    public void setVisit_order(int visit_order) {
        this.visit_order = visit_order;
    }
}
