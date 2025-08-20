package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.model.Member;
import busantravel.service.MemberService;
import java.awt.*;
import javax.swing.*;

/**
 * 사용자 회원가입을 위한 대화상자를 생성하고 관리하는 클래스입니다.
 * 이름, 아이디, 비밀번호, 이메일 등 가입에 필요한 정보를 입력받습니다.
 */
public class SignUpDialog {
    private JFrame parent;
    private TravelPlannerApp mainApp;

    /**
     * SignUpDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent 부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     */
    public SignUpDialog(JFrame parent, TravelPlannerApp mainApp) {
        this.parent = parent;
        this.mainApp = mainApp;
    }

    /**
     * 회원가입 대화상자를 화면에 표시합니다.
     * UI 컴포넌트를 초기화하고, 이벤트 리스너를 설정한 후 대화상자를 보이게 합니다.
     */
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
            if (MemberService.checkID(enteredId)) {
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
                return;
            }

            Member member = new Member(id, name, pw, email);
            boolean success = MemberService.register(member);
            if (success) {
                MemberSession.login(member); // 자동 로그인
                JOptionPane.showMessageDialog(signUpDialog, name + "님, 회원가입이 완료되었습니다!\n환영합니다!");
                signUpDialog.dispose();
                mainApp.refreshAuthButtons(); // 우측 상단 로그인/회원가입 → 마이페이지/로그아웃
            } else {
                JOptionPane.showMessageDialog(signUpDialog, "이미 존재하는 아이디입니다.", "회원가입 오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> signUpDialog.dispose());
        signUpDialog.setVisible(true);
    }
}
