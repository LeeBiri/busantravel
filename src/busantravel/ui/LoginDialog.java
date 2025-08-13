package busantravel.ui;

import javax.swing.*;

import busantravel.controller.LoginController;

import java.awt.*;
import busantravel.ui.ColorConstants;

public class LoginDialog {
    private JFrame parent;
    private JTextField idField;
    private JTextField pwField;
    private JLabel idLabel;
    private JLabel pwLabel;
    private JButton loginBtn;
    private JButton cancelBtn;

    public LoginDialog(JFrame parent) {
        this.parent = parent;
    }

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
    
    public String getId() {
    	return idField.getText();
    }
    
    public String getPw() {
    	return pwField.getText();
    }
}