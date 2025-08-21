package busantravel.model;

public class Interest {
	private String m_id; //회원ID
	private String p_id; //여행지 번호
	private String p_type; //여행지 구분
	
	public Interest(String m_id, String p_id, String p_type) {
		super();
		this.m_id = m_id;
		this.p_id = p_id;
		this.p_type = p_type;
	}

	public String getM_id() {
		return m_id;
	}

	public void setM_id(String m_id) {
		this.m_id = m_id;
	}

	public String getP_id() {
		return p_id;
	}

	public void setP_id(String p_id) {
		this.p_id = p_id;
	}

	public String getP_type() {
		return p_type;
	}

	public void setP_type(String p_type) {
		this.p_type = p_type;
	}
	
	
}
