package busantravel.ui;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * 새로운 여행 일정을 생성하기 위한 모달 대화상자 클래스입니다.
 * 사용자로부터 여행 제목, 시작 날짜, 종료 날짜를 입력받습니다.
 */
public class CreateTripDialog extends JDialog {
    private JTextField titleField;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private boolean confirmed = false;

    /**
     * CreateTripDialog의 새 인스턴스를 생성합니다.
     *
     * @param parent 부모 JFrame 객체. 대화상자는 이 프레임에 종속됩니다.
     */
    public CreateTripDialog(JFrame parent) {
        super(parent, "여행 일정 만들기", true);
        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(5, 2, 10, 10));
        setResizable(false);

        add(new JLabel("여행 제목:"));
        titleField = new JTextField();
        add(titleField);

        add(new JLabel("시작 날짜:"));
        startDateChooser = new JDateChooser();
        add(startDateChooser);

        add(new JLabel("종료 날짜:"));
        endDateChooser = new JDateChooser();
        add(endDateChooser);

        JButton createBtn = new JButton("만들기");
        JButton cancelBtn = new JButton("취소");
        createBtn.setBackground(ColorConstants.ACCENT);
        createBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        cancelBtn.setBackground(new Color(240, 240, 240));
        cancelBtn.setForeground(new Color(50, 50, 50));
        cancelBtn.setFocusPainted(false);
        
        createBtn.addActionListener(e -> {
            if (titleField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "여행 제목을 입력하세요.");
                return;
            }
            Date start = startDateChooser.getDate();
            Date end = endDateChooser.getDate();
            if (start == null || end == null) {
                JOptionPane.showMessageDialog(this, "날짜를 선택하세요.");
                return;
            }
            if (end.before(start)) {
                JOptionPane.showMessageDialog(this, "종료 날짜는 시작 날짜 이후여야 합니다.");
                return;
            }
            confirmed = true;
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        add(createBtn);
        add(cancelBtn);
    }
    
    /**
     * 사용자가 '만들기' 버튼을 눌러 입력을 확정했는지 여부를 반환합니다.
     *
     * @return 사용자가 입력을 확정했다면 true, 그렇지 않으면 false.
     */
    public boolean isConfirmed() {
        return confirmed;
    }

    /**
     * 사용자가 입력한 여행 제목을 반환합니다.
     *
     * @return 공백이 제거된 여행 제목 문자열.
     */
    public String getTitleInput() {
        return titleField.getText().trim();
    }

    /**
     * 사용자가 선택한 시작 날짜를 LocalDate 객체로 반환합니다.
     *
     * @return 선택된 시작 날짜.
     */
    public LocalDate getStartDate() {
        Date date = startDateChooser.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 사용자가 선택한 종료 날짜를 LocalDate 객체로 반환합니다.
     *
     * @return 선택된 종료 날짜.
     */
    public LocalDate getEndDate() {
        Date date = endDateChooser.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
