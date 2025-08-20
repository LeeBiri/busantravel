package busantravel.model;

public class Checklist {
	private int checklist_id; //체크리스트 번호
	private String plan_id; //계획 ID
	private String creator_id; //생성자 ID
	private String checklist_content; //체크리스트 내용
	private boolean is_completed; //글제목
	private boolean is_public; //글내용
	
	
	public Checklist(int checklist_id, String plan_id, String creator_id, String checklist_content,
			boolean is_completed, boolean is_public) {
		this.checklist_id = checklist_id;
		this.plan_id = plan_id;
		this.creator_id = creator_id;
		this.checklist_content = checklist_content;
		this.is_completed = is_completed;
		this.is_public = is_public;
	}
	
	public int getChecklist_id() {
		return checklist_id;
	}
	public void setChecklist_id(int checklist_id) {
		this.checklist_id = checklist_id;
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
	public String getChecklist_content() {
		return checklist_content;
	}
	public void setChecklist_content(String checklist_content) {
		this.checklist_content = checklist_content;
	}
	public boolean isIs_completed() {
		return is_completed;
	}
	public void setIs_completed(boolean is_completed) {
		this.is_completed = is_completed;
	}
	public boolean isIs_public() {
		return is_public;
	}
	public void setIs_public(boolean is_public) {
		this.is_public = is_public;
	}
	
	
}
