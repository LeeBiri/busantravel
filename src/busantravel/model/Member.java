package busantravel.model;

public class Member {
	private String m_id; //회원 ID
	private String m_name; //회원 이름
	private String m_password; //회원 비밀번호
	private String m_email; //회원 이메일
	
	public Member() {
	
	}
	
	public Member(String m_id, String m_name, String m_password, String m_email) {
		this.m_id = m_id;
		this.m_name = m_name;
		this.m_password = m_password;
		this.m_email = m_email;
	}
	
	public String getM_id() {
		return m_id;
	}
	public void setM_id(String m_id) {
		this.m_id = m_id;
	}
	public String getM_name() {
		return m_name;
	}
	public void setM_name(String m_name) {
		this.m_name = m_name;
	}
	public String getM_password() {
		return m_password;
	}
	public void setM_password(String m_password) {
		this.m_password = m_password;
	}
	public String getM_email() {
		return m_email;
	}
	public void setM_email(String m_email) {
		this.m_email = m_email;
	}
	
	
}
