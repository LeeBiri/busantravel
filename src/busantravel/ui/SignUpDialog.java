package busantravel.ui;

import javax.swing.*;
import java.awt.*;
import busantravel.ui.ColorConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SignUpDialog {
    private JFrame parent;

    public SignUpDialog(JFrame parent) {
        this.parent = parent;
    }

    public void showDialog() {
        JDialog signUpDialog = new JDialog(parent, "회원가입", true);
        signUpDialog.setSize(450, 350); // 크기 조정
        signUpDialog.setLocationRelativeTo(parent);
        signUpDialog.setLayout(new GridLayout(5, 2, 15, 15)); // 간격 조정
        signUpDialog.getContentPane().setBackground(Color.WHITE);

        // 폰트 설정
        Font labelFont = new Font("맑은 고딕", Font.PLAIN, 14);
        Font fieldFont = new Font("맑은 고딕", Font.PLAIN, 14);

        JTextField nameField = new JTextField();
        nameField.setFont(fieldFont);
        JTextField idField = new JTextField();
        idField.setFont(fieldFont);
        JPasswordField pwField = new JPasswordField();
        pwField.setFont(fieldFont);
        JTextField emailField = new JTextField();
        emailField.setFont(fieldFont);

        // 이름 필드
        JLabel nameLabel = new JLabel("이름:");
        nameLabel.setFont(labelFont);
        signUpDialog.add(nameLabel);
        signUpDialog.add(nameField);

        // 아이디 필드와 중복확인 버튼
        JLabel idLabel = new JLabel("아이디:");
        idLabel.setFont(labelFont);
        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.setBackground(Color.WHITE);
        idPanel.add(idField, BorderLayout.CENTER);
        JButton checkIdBtn = new JButton("중복확인");
        checkIdBtn.setBackground(new Color(240, 240, 240));
        checkIdBtn.setForeground(new Color(50, 50, 50));
        checkIdBtn.setFocusPainted(false);
        checkIdBtn.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        idPanel.add(checkIdBtn, BorderLayout.EAST);
        signUpDialog.add(idLabel);
        signUpDialog.add(idPanel);

        // 비밀번호 필드
        JLabel pwLabel = new JLabel("비밀번호:");
        pwLabel.setFont(labelFont);
        signUpDialog.add(pwLabel);
        signUpDialog.add(pwField);

        // 이메일 필드
        JLabel emailLabel = new JLabel("이메일:");
        emailLabel.setFont(labelFont);
        signUpDialog.add(emailLabel);
        signUpDialog.add(emailField);

        JButton signUpBtn = new JButton("가입하기");
        JButton cancelBtn = new JButton("취소");

        // 가입하기/취소 버튼 스타일
        signUpBtn.setBackground(ColorConstants.ACCENT);
        signUpBtn.setForeground(Color.WHITE);
        signUpBtn.setFocusPainted(false);
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(new Color(50, 50, 50));
        cancelBtn.setFocusPainted(false);

        signUpDialog.add(signUpBtn);
        signUpDialog.add(cancelBtn);

        // --- 액션 리스너 ---
        checkIdBtn.addActionListener(e -> {
            String enteredId = idField.getText().trim();
            if (enteredId.isEmpty()) {
                JOptionPane.showMessageDialog(signUpDialog, "아이디를 입력하세요!");
            } else if (enteredId.equalsIgnoreCase("admin") || enteredId.equals("testuser")) { // 예시 중복 아이디
                JOptionPane.showMessageDialog(signUpDialog, "이미 존재하는 아이디입니다.", "중복확인", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(signUpDialog, "사용 가능한 아이디입니다!", "중복확인", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        signUpBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());
            String email = emailField.getText().trim();

            if (name.isEmpty() || id.isEmpty() || pw.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(signUpDialog, "모든 정보를 입력하세요!", "회원가입 오류", JOptionPane.WARNING_MESSAGE);
            } else {
                // 실제 회원가입 처리 로직 (데이터베이스 저장 등)은 추후 구현
                JOptionPane.showMessageDialog(signUpDialog, name + "님, 회원가입이 완료되었습니다!\n환영합니다!");
                signUpDialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> signUpDialog.dispose());
        signUpDialog.setVisible(true);
    }
}
