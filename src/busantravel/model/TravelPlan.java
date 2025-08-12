package busantravel.model;

import java.time.LocalDateTime;

public class TravelPlan {
	private String plan_id; //계획 ID
	private String creator_id; //생성자 ID
	private String plan_title; // 계획 제목
	private LocalDateTime plan_period_start; // 여행 시작일
	private LocalDateTime plan_period_end; //여행 종료일
	public TravelPlan(String plan_id, String creator_id, String plan_title, LocalDateTime plan_period_start,
			LocalDateTime plan_period_end) {
		this.plan_id = plan_id;
		this.creator_id = creator_id;
		this.plan_title = plan_title;
		this.plan_period_start = plan_period_start;
		this.plan_period_end = plan_period_end;
	}
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public String getCreator_id() {
		return creator_id;
	}
	public void setCreator_id(String creator_id) {
		this.creator_id = creator_id;
	}
	public String getPlan_title() {
		return plan_title;
	}
	public void setPlan_title(String plan_title) {
		this.plan_title = plan_title;
	}
	public LocalDateTime getPlan_period_start() {
		return plan_period_start;
	}
	public void setPlan_period_start(LocalDateTime plan_period_start) {
		this.plan_period_start = plan_period_start;
	}
	public LocalDateTime getPlan_period_end() {
		return plan_period_end;
	}
	public void setPlan_period_end(LocalDateTime plan_period_end) {
		this.plan_period_end = plan_period_end;
	}
	
	
}
