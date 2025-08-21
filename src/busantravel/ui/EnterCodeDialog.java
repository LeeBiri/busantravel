package busantravel.ui;

import java.awt.*;
import javax.swing.*;

/**
 * 다른 사용자가 생성한 여행 일정에 참여하기 위해 참여 코드를 입력하는 대화상자 클래스입니다.
 */
public class EnterCodeDialog extends JDialog {
    private JTextField codeField;
    private SchedulePanel schedulePanel;

    /**
     * EnterCodeDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent        부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     * @param schedulePanel 코드를 통해 참여한 후 UI를 갱신할 SchedulePanel 객체.
     */
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

        /**
         * '입력' 버튼 클릭 시 호출됩니다.
         * 1. 코드 입력값이 비어 있는지 검증하여 비어 있으면 경고창을 표시합니다.
         * 2. TripRoomManager의 인스턴스를 가져옵니다.
         * 3. 입력된 코드를 사용하여 여행방을 검색합니다.
         * 4. 방이 존재하면 방 정보를 팝업으로 표시하고, SchedulePanel의 목록을 새로고침한 후 대화상자를 닫습니다.
         * 5. 방이 존재하지 않으면 오류 메시지를 표시합니다.
         */
        // enterBtn.addActionListener(...) 구현 생략

        /**
         * '취소' 버튼 클릭 시 현재 대화상자를 닫습니다.
         */
        // cancelBtn.addActionListener(...) 구현 생략

        add(enterBtn);
        add(cancelBtn);
    }
}
