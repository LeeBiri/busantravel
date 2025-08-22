package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.model.Member;
import busantravel.service.MemberService;
import java.awt.*;
import java.io.InputStream;
import javax.swing.*;

public class SignUpDialog {
    private JFrame parent;
    private TravelPlannerApp mainApp;
    private Font jalnanFont;
    private MemberService memberService;

    public SignUpDialog(JFrame parent, TravelPlannerApp mainApp) {
        this.parent = parent;
        this.mainApp = mainApp;
        this.memberService = new MemberService();
        loadFont();
    }

    public void showDialog() {
        JDialog dialog = new JDialog(parent, "회원가입", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(parent);
        dialog.getContentPane().setBackground(Color.WHITE);
        dialog.setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        contentPanel.setBackground(Color.WHITE);

        // 필드 생성
        JTextField nameField = createTextField();
        JTextField idField = createTextField();
        JPasswordField pwField = new JPasswordField();
        pwField.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JTextField emailField = createTextField();

        // 아이디 + 중복확인 버튼
        JPanel idPanel = new JPanel(new BorderLayout());
        idPanel.setBackground(Color.WHITE);
        idPanel.add(idField, BorderLayout.CENTER);
        JButton checkBtn = new JButton("중복확인");
        checkBtn.setFocusPainted(false);
        checkBtn.setFont(jalnanFont.deriveFont(12f));
        checkBtn.setBackground(ColorConstants.ACCENT);
        checkBtn.setForeground(Color.WHITE);
        idPanel.add(checkBtn, BorderLayout.EAST);

        // 필드 그룹
        contentPanel.add(createFormRow("이름", nameField));
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(createFormRow("아이디", idPanel));
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(createFormRow("비밀번호", pwField));
        contentPanel.add(Box.createVerticalStrut(18));
        contentPanel.add(createFormRow("이메일", emailField));
        contentPanel.add(Box.createVerticalStrut(25));

        // 버튼 영역
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new GridLayout(1, 2, 20, 0));
        btnPanel.setBackground(Color.WHITE);

        JButton joinBtn = new JButton("가입하기");
        JButton cancelBtn = new JButton("취소");

        Dimension btnSize = new Dimension(180, 50);
        joinBtn.setPreferredSize(btnSize);
        cancelBtn.setPreferredSize(btnSize);

        joinBtn.setBackground(ColorConstants.ACCENT);
        joinBtn.setForeground(Color.WHITE);
        joinBtn.setFocusPainted(false);
        joinBtn.setFont(jalnanFont.deriveFont(Font.BOLD, 15f));

        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(Color.DARK_GRAY);
        cancelBtn.setFocusPainted(false);
        cancelBtn.setFont(jalnanFont.deriveFont(Font.BOLD, 15f));

        btnPanel.add(joinBtn);
        btnPanel.add(cancelBtn);
        contentPanel.add(btnPanel);

        dialog.add(contentPanel, BorderLayout.CENTER);

        // --- 액션 리스너 ---
        checkBtn.addActionListener(e -> {
            String enteredId = idField.getText().trim();
            if (enteredId.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "아이디를 입력하세요.", "오류", JOptionPane.WARNING_MESSAGE);
            } else if (memberService.checkID(enteredId)) {
                JOptionPane.showMessageDialog(dialog, "이미 존재하는 아이디입니다.", "중복확인", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, "사용 가능한 아이디입니다.", "중복확인", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        joinBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String id = idField.getText().trim();
            String pw = new String(pwField.getPassword());
            String email = emailField.getText().trim();

            if (name.isEmpty() || id.isEmpty() || pw.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "모든 정보를 입력하세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            Member member = new Member(id, name, pw, email);
            if (memberService.register(member)) {
                MemberSession.login(member);
                JOptionPane.showMessageDialog(dialog, name + "님, 회원가입이 완료되었습니다!");
                dialog.dispose();
                mainApp.refreshAuthButtons();
            } else {
                JOptionPane.showMessageDialog(dialog, "이미 존재하는 아이디입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private JPanel createFormRow(String labelText, Component inputField) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        JLabel label = new JLabel(labelText + ": ");
        label.setFont(jalnanFont.deriveFont(Font.PLAIN, 15f));
        label.setPreferredSize(new Dimension(80, 30));
        panel.add(label, BorderLayout.WEST);
        panel.add(inputField, BorderLayout.CENTER);
        return panel;
    }

    private JTextField createTextField() {
        JTextField tf = new JTextField();
        tf.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        tf.setPreferredSize(new Dimension(300, 30));
        return tf;
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(jalnanFont);
            } else {
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }
}
