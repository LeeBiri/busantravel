package busantravel.service;

import busantravel.auth.MemberSession;
import busantravel.dao.MemberDAO;
import busantravel.model.Member;

public class MemberService { 
    public static MemberDAO memberDAO;

    public MemberService() {
        this.memberDAO = new MemberDAO();
    }

	public static Member login(String id, String pw) {
		Member m = memberDAO.getMember(id, pw);
        if (m != null) {
            MemberSession.login(m);
        }
        return m;
	}
	
	public static boolean register(Member member) {
		return memberDAO.register(member);
	}

	public static boolean checkID(String id) {
		return memberDAO.checkID(id);
	}

	public static boolean deleteId(String id, String pw) {
		boolean flag = memberDAO.deleteId(id, pw);
		if (flag && MemberSession.isLoggedIn() && MemberSession.getId().equals(id)) {
            MemberSession.logout();
        }
        return flag;
	}

	public static boolean updateId(Member member) {
		boolean flag = memberDAO.updateId(member);
		if (flag && MemberSession.isLoggedIn() && MemberSession.getId().equals(member.getM_id())) {
			MemberSession.login(member);
		}
		return flag;
	}
}
