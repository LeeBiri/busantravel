package busantravel.model;

public class Tourist {
	private String t_id; //관광지 ID
	private String t_place; //장소명
	private String t_intro; //소개문
	private String t_num; // 연락처
	private String t_tra; //교통 정보
	private String t_rest; //운영 정보
	private String t_addr; //주소
	private String t_thum; //썸네일
	
	public Tourist() {
		
	}

	public Tourist(String t_id, String t_place, String t_intro, String t_num, String t_tra, String t_rest,
			String t_addr, String t_thum) {
		this.t_id = t_id;
		this.t_place = t_place;
		this.t_intro = t_intro;
		this.t_num = t_num;
		this.t_tra = t_tra;
		this.t_rest = t_rest;
		this.t_addr = t_addr;
		this.t_thum = t_thum;
	}
	
	public String getT_id() {
		return t_id;
	}
	public void setT_id(String t_id) {
		this.t_id = t_id;
	}
	public String getT_place() {
		return t_place;
	}
	public void setT_place(String t_place) {
		this.t_place = t_place;
	}
	public String getT_intro() {
		return t_intro;
	}
	public void setT_intro(String t_intro) {
		this.t_intro = t_intro;
	}
	public String getT_num() {
		return t_num;
	}
	public void setT_num(String t_num) {
		this.t_num = t_num;
	}
	public String getT_tra() {
		return t_tra;
	}
	public void setT_tra(String t_tra) {
		this.t_tra = t_tra;
	}
	public String getT_rest() {
		return t_rest;
	}
	public void setT_rest(String t_rest) {
		this.t_rest = t_rest;
	}
	public String getT_addr() {
		return t_addr;
	}
	public void setT_addr(String t_addr) {
		this.t_addr = t_addr;
	}

	public String getT_thum() {
		return t_thum;
	}

	public void setT_thum(String t_thum) {
		this.t_thum = t_thum;
	}
}
