package busantravel.model;

import java.time.LocalDateTime;

public class ScheduledPlace {
	private String checklist_id; //계획 ID
	private String p_id; //여행지 번호
	private String p_type; //여행지 구분
	private LocalDateTime scheduled_date; //방문 예정 일자
	private LocalDateTime expected_dutarion; //예상 체류 시간
	private int visit_order; //방문 예정 순서
	
	public ScheduledPlace(String checklist_id, String p_id, String p_type, LocalDateTime scheduled_date,
			LocalDateTime expected_dutarion, int visit_order) {
		super();
		this.checklist_id = checklist_id;
		this.p_id = p_id;
		this.p_type = p_type;
		this.scheduled_date = scheduled_date;
		this.expected_dutarion = expected_dutarion;
		this.visit_order = visit_order;
	}
	
	public String getChecklist_id() {
		return checklist_id;
	}
	public void setChecklist_id(String checklist_id) {
		this.checklist_id = checklist_id;
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
