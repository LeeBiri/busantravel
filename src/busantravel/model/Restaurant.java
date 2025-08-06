package busantravel.model;

public class Restaurant {
	private String r_id; //음식점 ID
	private String r_place; //장소명
	private String r_intro; //소개문
	private String r_num; // 연락처
	private String r_tra; //교통 정보
	private String r_infor; //운영 정보
	private String r_addr; //주소
	
	public Restaurant(String r_id, String r_place, String r_intro, String r_num, String r_tra, String r_infor,
			String r_addr) {
		this.r_id = r_id;
		this.r_place = r_place;
		this.r_intro = r_intro;
		this.r_num = r_num;
		this.r_tra = r_tra;
		this.r_infor = r_infor;
		this.r_addr = r_addr;
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
	public String getR_tra() {
		return r_tra;
	}
	public void setR_tra(String r_tra) {
		this.r_tra = r_tra;
	}
	public String getR_infor() {
		return r_infor;
	}
	public void setR_infor(String r_infor) {
		this.r_infor = r_infor;
	}
	public String getR_addr() {
		return r_addr;
	}
	public void setR_addr(String r_addr) {
		this.r_addr = r_addr;
	}
	
	
}
