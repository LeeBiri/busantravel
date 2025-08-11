package busantravel.ui;

import javax.swing.*;
import java.awt.*;

public class EnterCodeDialog extends JDialog {
    private JTextField codeField;
    private SchedulePanel schedulePanel;

    public EnterCodeDialog(JFrame parent, SchedulePanel schedulePanel) {
        super(parent, "참여 코드 입력", true);
        this.schedulePanel = schedulePanel;
        setSize(350, 150);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(2, 2, 10, 10));
        setResizable(false);

        add(new JLabel("참여 코드:"));
        codeField = new JTextField();
        add(codeField);

        JButton enterBtn = new JButton("입력");
        JButton cancelBtn = new JButton("취소");

        enterBtn.addActionListener(e -> {
            String code = codeField.getText().trim();
            if (code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "참여 코드를 입력해주세요.", "입력 오류", JOptionPane.WARNING_MESSAGE);
                return;
            }

            TripRoomManager manager = TripRoomManager.getInstance();
            TripRoom room = manager.getRoom(code);

            if (room != null) {
                JOptionPane.showMessageDialog(this,
                        "방에 입장했습니다!\n" +
                        "제목: " + room.getTitle() + "\n" +
                        "기간: " + room.getStartDate() + " ~ " + room.getEndDate() + "\n" +
                        "방 코드: " + room.getRoomCode());
                    schedulePanel.refreshRoomLists();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "존재하지 않는 코드입니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dispose());

        add(enterBtn);
        add(cancelBtn);
    }
}
