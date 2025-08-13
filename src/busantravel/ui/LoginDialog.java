package busantravel.ui;

import javax.swing.*;

import busantravel.controller.LoginController;

import java.awt.*;
import busantravel.ui.ColorConstants;

/**
 * 사용자 로그인을 위한 대화상자를 생성하고 관리하는 클래스입니다.
 * 아이디와 비밀번호 입력을 받아 로그인을 처리합니다.
 */
public class LoginDialog {
    private JFrame parent;
    private JTextField idField;
    private JTextField pwField;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JButton loginBtn;
    private JButton cancelBtn;

    /**
     * LoginDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent 부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     */
    public LoginDialog(JFrame parent) {
        this.parent = parent;
    }

    /**
     * 로그인 대화상자를 화면에 표시합니다.
     * UI 컴포넌트를 초기화하고, 이벤트 리스너를 설정한 후 대화상자를 보이게 합니다.
     */
    public void showDialog() {
        JDialog loginDialog = new JDialog(parent, "로그인", true);
        loginDialog.setSize(350, 230); // 다이얼로그 크기 조정
        loginDialog.setLocationRelativeTo(parent);
        loginDialog.setLayout(new GridLayout(3, 2, 15, 15)); // 행/열 간격 조정
        loginDialog.getContentPane().setBackground(Color.WHITE); // 배경색

        idField = new JTextField();
        pwField = new JPasswordField();

        // 라벨 및 필드 추가
        idLabel = new JLabel("아이디:");
        idLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JLabel pwLabel = new JLabel("비밀번호:");
        pwLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        loginDialog.add(idLabel);
        loginDialog.add(idField);
        loginDialog.add(pwLabel);
        loginDialog.add(pwField);

        loginBtn = new JButton("로그인");
        cancelBtn = new JButton("취소");

        // 버튼 스타일 설정
        loginBtn.setBackground(ColorConstants.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(new Color(50, 50, 50));
        cancelBtn.setFocusPainted(false);
        
        LoginController controller = new LoginController(this, loginDialog);
        loginBtn.addActionListener(e -> controller.handleLogin());

        cancelBtn.addActionListener(e -> loginDialog.dispose());

        loginDialog.add(loginBtn);
        loginDialog.add(cancelBtn);

        loginDialog.setVisible(true);
    }
    
    /**
     * 아이디 입력 필드에 입력된 값을 반환합니다.
     *
     * @return 사용자가 입력한 아이디 문자열.
     */
    public String getId() {
    	return idField.getText();
    }
    
    /**
     * 비밀번호 입력 필드에 입력된 값을 반환합니다.
     *
     * @return 사용자가 입력한 비밀번호 문자열.
     */
    public String getPw() {
    	return pwField.getText();
    }
}
