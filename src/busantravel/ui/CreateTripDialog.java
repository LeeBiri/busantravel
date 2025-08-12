package busantravel.ui;

import javax.swing.*;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class CreateTripDialog extends JDialog {
    private JTextField titleField;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
    private boolean confirmed = false;

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
    
    public boolean isConfirmed() {
        return confirmed;
    }

    public String getTitleInput() {
        return titleField.getText().trim();
    }

    public LocalDate getStartDate() {
        Date date = startDateChooser.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDate getEndDate() {
        Date date = endDateChooser.getDate();
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
