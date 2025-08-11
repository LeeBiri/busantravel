package busantravel.model;

public class Lodging {
	private String l_id; //숙소 ID
	private String l_place; //장소명
	private String l_intro; //소개문
	private String l_num; // 연락처
	private String l_tra; //교통 정보
	private String l_infor; //운영 정보
	private String l_addr; //주소
	
	
	public Lodging(String l_id, String l_place, String l_intro, String l_num, String l_tra, String l_infor,
			String l_addr) {
		this.l_id = l_id;
		this.l_place = l_place;
		this.l_intro = l_intro;
		this.l_num = l_num;
		this.l_tra = l_tra;
		this.l_infor = l_infor;
		this.l_addr = l_addr;
	}
	
	public String getL_id() {
		return l_id;
	}
	public void setL_id(String l_id) {
		this.l_id = l_id;
	}
	public String getL_place() {
		return l_place;
	}
	public void setL_place(String l_place) {
		this.l_place = l_place;
	}
	public String getL_intro() {
		return l_intro;
	}
	public void setL_intro(String l_intro) {
		this.l_intro = l_intro;
	}
	public String getL_num() {
		return l_num;
	}
	public void setL_num(String l_num) {
		this.l_num = l_num;
	}
	public String getL_tra() {
		return l_tra;
	}
	public void setL_tra(String l_tra) {
		this.l_tra = l_tra;
	}
	public String getL_infor() {
		return l_infor;
	}
	public void setL_infor(String l_infor) {
		this.l_infor = l_infor;
	}
	public String getL_addr() {
		return l_addr;
	}
	public void setL_addr(String l_addr) {
		this.l_addr = l_addr;
	}
}
