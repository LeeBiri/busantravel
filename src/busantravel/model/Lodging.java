package busantravel.model;

public class Lodging {
	private String l_id; //숙소 ID
	private String l_place; //장소명
	private String l_num; // 연락처
	private String l_addr; //주소
	private String l_thum; //썸네일 이미지 URL
	
	
	public Lodging(String l_id, String l_place, String l_intro, String l_num, String l_tra, String l_infor,
			String l_addr, String l_thum) {
		this.l_id = l_id;
		this.l_place = l_place;
		this.l_num = l_num;
		this.l_addr = l_addr;
		this.l_thum = l_thum;
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
	public String getL_num() {
		return l_num;
	}
	public void setL_num(String l_num) {
		this.l_num = l_num;
	}
	public String getL_addr() {
		return l_addr;
	}
	public void setL_addr(String l_addr) {
		this.l_addr = l_addr;
	}
	public String getL_thum() {
		return l_thum;
	}
	public void setL_thum(String l_thum) {
		this.l_thum = l_thum;
	}

}
