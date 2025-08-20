package busantravel.service;

import busantravel.auth.MemberSession;
import busantravel.dao.MemberDAO;
import busantravel.model.Member;

	//로그인 검사
public class MemberService { 
	public static Member login(String id, String pw) {
		Member m = MemberDAO.getMember(id, pw);
        if (m != null) {
            MemberSession.login(m);
        }
        return m;
	}
	
	// 회원가입
	public static boolean register(Member member) {
		return MemberDAO.register(member);
	}

	// ID 중복 체크
	public static boolean checkID(String id) {
		return MemberDAO.checkID(id);
	}
	//탈퇴 및 로그아웃
	public static boolean deleteId(String id, String pw) {
		boolean flag = MemberDAO.deleteId(id, pw);
		if (flag && MemberSession.isLoggedIn() && MemberSession.getId().equals(id)) {
            MemberSession.logout();
        }
        return flag;
	}

	//회원정보수정
	public static boolean updateId(Member member) {
		boolean flag = MemberDAO.updateId(member);
		if (flag && MemberSession.isLoggedIn() && MemberSession.getId().equals(member.getM_id())) {
			MemberSession.login(member);
		}
		return flag;
	}
}
