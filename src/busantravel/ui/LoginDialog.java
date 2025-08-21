package busantravel.ui;

import busantravel.controller.LoginController;
import java.awt.*;
import java.io.InputStream;
import javax.swing.*;

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
    private TravelPlannerApp mainApp;
    private Runnable onLoginSuccess;

    /**
     * LoginDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent 부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     */
    public LoginDialog(JFrame parent, TravelPlannerApp mainApp) {
        this.parent = parent;
        this.mainApp = mainApp;
    }

    public LoginDialog(JFrame parent, TravelPlannerApp mainApp, Runnable onLoginSuccess) {
        this.parent = parent;
        this.mainApp = mainApp;
        this.onLoginSuccess = onLoginSuccess;
    }


    /**
     * 로그인 대화상자를 화면에 표시합니다.
     * UI 컴포넌트를 초기화하고, 이벤트 리스너를 설정한 후 대화상자를 보이게 합니다.
     */
    public void showDialog() {
        JDialog loginDialog = new JDialog(parent, "로그인", true);
        loginDialog.setSize(400, 280);
        loginDialog.setLocationRelativeTo(parent);
        loginDialog.setLayout(new BorderLayout());
        loginDialog.getContentPane().setBackground(Color.WHITE);

        Font labelFont;
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                labelFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(labelFont);
            } else {
                labelFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            labelFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }

        // 중앙 입력 패널
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(2, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        inputPanel.setBackground(Color.WHITE);

        idLabel = new JLabel("아이디:");
        idLabel.setFont(labelFont);
        idField = new JTextField();
        idField.setFont(labelFont);

        pwLabel = new JLabel("비밀번호:");
        pwLabel.setFont(labelFont);
        pwField = new JPasswordField();
        pwField.setFont(labelFont);

        inputPanel.add(idLabel);
        inputPanel.add(idField);
        inputPanel.add(pwLabel);
        inputPanel.add(pwField);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 2, 20, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        buttonPanel.setBackground(Color.WHITE);

        loginBtn = new JButton("로그인");
        loginBtn.setFont(labelFont.deriveFont(Font.BOLD, 14f));
        loginBtn.setBackground(ColorConstants.ACCENT);
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);

        cancelBtn = new JButton("취소");
        cancelBtn.setFont(labelFont.deriveFont(Font.BOLD, 14f));
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(new Color(50, 50, 50));
        cancelBtn.setFocusPainted(false);

        buttonPanel.add(loginBtn);
        buttonPanel.add(cancelBtn);

        loginDialog.add(inputPanel, BorderLayout.CENTER);
        loginDialog.add(buttonPanel, BorderLayout.SOUTH);

        // 이벤트
        LoginController controller = new LoginController(this, loginDialog);
        loginBtn.addActionListener(e -> {
            boolean success = controller.handleLogin();
            if (success) {
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
                loginDialog.dispose();
            }
        });

        cancelBtn.addActionListener(e -> loginDialog.dispose());
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

    public TravelPlannerApp getMainApp() {
        return mainApp;
    }
}
