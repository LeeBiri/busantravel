package busantravel.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class ScheduledPlace {
    private String plan_id; //계획 ID
    private String p_id; //여행지 번호
    private String p_type; //여행지 구분
    private LocalDateTime scheduled_date; //방문 예정 일자
    private int visit_order; //방문 예정 순서

    // 기존 생성자 (새로운 일정 추가 시 사용)
    public ScheduledPlace(String plan_id, String p_id, String p_type, LocalDateTime scheduled_date, int visit_order) {
        this.plan_id = plan_id;
        this.p_id = p_id;
        this.p_type = p_type;
        this.scheduled_date = scheduled_date;
        this.visit_order = visit_order;
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
    public int getVisit_order() {
        return visit_order;
    }
    public void setVisit_order(int visit_order) {
        this.visit_order = visit_order;
    }

    
}
