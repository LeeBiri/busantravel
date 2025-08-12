package busantravel.model;

public class TravelPlanMember {
	private String plan_id; //계획 ID
	private String m_id; //회원 ID
	private String role; //권한
	
	public TravelPlanMember(String plan_id, String m_id, String role) {
		super();
		this.plan_id = plan_id;
		this.m_id = m_id;
		this.role = role;
	}
	
	public String getPlan_id() {
		return plan_id;
	}
	public void setPlan_id(String plan_id) {
		this.plan_id = plan_id;
	}
	public String getM_id() {
		return m_id;
	}
	public void setM_id(String m_id) {
		this.m_id = m_id;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	
}
