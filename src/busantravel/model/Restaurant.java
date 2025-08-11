package busantravel.model;

public class Restaurant {
	private String r_id; //음식점 ID
	private String r_place; //장소명
	private String r_intro; //소개문
	private String r_num; // 연락처
	private String r_rest; //운영 정보
	private String r_addr; //주소
	private String r_thum;

	public Restaurant(String r_id, String r_place, String r_intro, String r_num, String r_rest,
			String r_addr, String r_thum) {
		this.r_id = r_id;
		this.r_place = r_place;
		this.r_intro = r_intro;
		this.r_num = r_num;
		this.r_rest = r_rest;
		this.r_addr = r_addr;
		this.r_thum = r_thum;
	}
	
	public String getR_id() {
		return r_id;
	}
	public void setR_id(String r_id) {
		this.r_id = r_id;
	}
	public String getR_place() {
		return r_place;
	}
	public void setR_place(String r_place) {
		this.r_place = r_place;
	}
	public String getR_intro() {
		return r_intro;
	}
	public void setR_intro(String r_intro) {
		this.r_intro = r_intro;
	}
	public String getR_num() {
		return r_num;
	}
	public void setR_num(String r_num) {
		this.r_num = r_num;
	}
	public String getR_rest() {
		return r_rest;
	}
	public void setR_rest(String r_rest) {
		this.r_rest = r_rest;
	}
	public String getR_addr() {
		return r_addr;
	}
	public void setR_addr(String r_addr) {
		this.r_addr = r_addr;
	}

	public String getR_thum() {
		return r_thum;
	}

	public void setR_thum(String r_thum) {
		this.r_thum = r_thum;
	}
}
