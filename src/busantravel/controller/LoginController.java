package busantravel.controller;

import busantravel.model.Member;
import busantravel.service.MemberService;
import busantravel.ui.LoginDialog;
import busantravel.ui.TravelPlannerApp;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

public class LoginController {

	LoginDialog view;
	JDialog dialog;
	TravelPlannerApp mainApp;

	public LoginController(LoginDialog view, JDialog dialog) {
		this.view = view;
		this.dialog = dialog;
	}

	public void handleLogin() {
		String id = view.getId();
		String pw = view.getPw();
		mainApp = view.getMainApp();

		if (id.isEmpty() || pw.isEmpty()) {
			JOptionPane.showMessageDialog(dialog, "아이디와 비밀번호를 입력해주세요.", "로그인 오류", JOptionPane.WARNING_MESSAGE);
		} else {
			Member member = MemberService.login(id, pw);
			if (member != null) {
				JOptionPane.showMessageDialog(dialog, member.getM_name() + "님 환영합니다!");
				mainApp.refreshAuthButtons();
				dialog.dispose();
			} else {
				JOptionPane.showMessageDialog(dialog, "아이디 또는 비밀번호가 올바르지 않습니다.", "로그인 오류", JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
