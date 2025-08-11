package busantravel.model;

public class Festival {
	private String f_id; //축제 ID
	private String f_name; //장소명
	private String f_intro; //소개문
	private String f_plan; //일정
	private String f_tra; //교통 정보
	private String f_rest; //운영 정보
	private String f_addr; //주소
	private String f_thum; //썸네일
	
	
	
	public Festival(String f_id, String f_name, String f_intro, String f_plan, String f_tra, String f_rest,
			String f_addr, String f_thum) {
		this.f_id = f_id;
		this.f_name = f_name;
		this.f_intro = f_intro;
		this.f_plan = f_plan;
		this.f_tra = f_tra;
		this.f_rest = f_rest;
		this.f_addr = f_addr;
		this.f_thum = f_thum;
	}
	
	public String getF_id() {
		return f_id;
	}
	public void setF_id(String f_id) {
		this.f_id = f_id;
	}
	public String getF_name() {
		return f_name;
	}
	public void setF_name(String f_name) {
		this.f_name = f_name;
	}
	public String getF_intro() {
		return f_intro;
	}
	public void setF_intro(String f_intro) {
		this.f_intro = f_intro;
	}
	public String getF_plan() {
		return f_plan;
	}
	public void setF_plan(String f_plan) {
		this.f_plan = f_plan;
	}
	public String getF_tra() {
		return f_tra;
	}
	public void setF_tra(String f_tra) {
		this.f_tra = f_tra;
	}
	public String getF_rest() {
		return f_rest;
	}
	public void setF_rest(String f_rest) {
		this.f_rest = f_rest;
	}
	public String getF_addr() {
		return f_addr;
	}
	public void setF_addr(String f_addr) {
		this.f_addr = f_addr;
	}

	public String getF_thum() {
		return f_thum;
	}

	public void setF_thum(String f_thum) {
		this.f_thum = f_thum;
	}
}
