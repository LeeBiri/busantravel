package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.service.TravelPlanMemberService;
import busantravel.service.TravelPlanService;
import java.awt.*;
import javax.swing.*;

/**
 * 다른 사용자가 생성한 여행 일정에 참여하기 위해 참여 코드를 입력하는 대화상자 클래스입니다.
 */
public class EnterCodeDialog extends JDialog {
    private JTextField codeField;
    private Runnable onSuccess;

    /**
     * EnterCodeDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent        부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     * @param schedulePanel 코드를 통해 참여한 후 UI를 갱신할 SchedulePanel 객체.
     * 
     * 
     */
    public EnterCodeDialog(JFrame parent, Runnable onSuccess) {
        super(parent, "참여 코드 입력", true);
        this.onSuccess = onSuccess;
        setSize(350, 150);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(2, 2, 10, 10));
        setResizable(false);

        add(new JLabel("참여 코드:"));
        codeField = new JTextField();
        add(codeField);

        JButton enterBtn = new JButton("입력");
        JButton cancelBtn = new JButton("취소");

        enterBtn.setBackground(ColorConstants.ACCENT);
        enterBtn.setForeground(Color.WHITE);
        enterBtn.setFocusPainted(false);
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(new Color(50, 50, 50));
        cancelBtn.setFocusPainted(false);

        enterBtn.addActionListener((e) -> {
            String enterCode = codeField.getText().trim();

            if (enterCode.isEmpty()) {
                JOptionPane.showMessageDialog(this, "모든 정보를 입력하세요!", "오류", JOptionPane.WARNING_MESSAGE);
            } else {
                if (TravelPlanService.findById(enterCode) == null) {
                    JOptionPane.showMessageDialog(this, "일치하는 여행 계획이 없습니다.", "오류", JOptionPane.WARNING_MESSAGE);
                    codeField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "여행 계획이 추가되었습니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    TravelPlanMemberService.insert(enterCode, MemberSession.getId());
                    if (onSuccess != null) onSuccess.run();
                    this.dispose();
                }
            }
        });

        cancelBtn.addActionListener(e -> this.dispose());

        add(enterBtn);
        add(cancelBtn);
        
    }
}
