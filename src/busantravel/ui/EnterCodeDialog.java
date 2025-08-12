package busantravel.ui;

import java.awt.*;
import javax.swing.*;

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

        // [기존 기능] enterBtn 클릭 시
        // 1. 코드 입력값이 비어 있는지 검증 → 비어 있으면 경고창 표시
        // 2. TripRoomManager.getInstance() 호출
        // 3. 입력한 코드로 getRoom(code) 호출하여 방 검색
        // 4. 방이 존재하면:
        //    - 방 정보(제목, 기간, 방 코드) 팝업으로 표시
        //    - SchedulePanel.refreshRoomLists() 호출하여 UI 갱신
        //    - 다이얼로그 닫기
        // 5. 방이 존재하지 않으면 오류 메시지 표시

        // [기존 기능] cancelBtn 클릭 시
        // 1. 현재 다이얼로그 닫기(dispose)

        add(enterBtn);
        add(cancelBtn);
    }
}
