package busantravel.ui;

import busantravel.model.TravelPlan;
import java.awt.*;
import javax.swing.*;

public class TripSchedulePanel extends JPanel {
    private TravelPlannerApp mainApp;

    public TripSchedulePanel(TravelPlannerApp mainApp) {
        this.mainApp = mainApp;
        setLayout(new BorderLayout());
        add(new JLabel("TripSchedulePanel: Select a trip to see details."), BorderLayout.CENTER);
    }

    public void loadTripRoom(TravelPlan plan) {
        removeAll();
        setBackground(Color.WHITE);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 상단: 여행 제목과 날짜
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(plan.getPlan_title());
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel dateLabel = new JLabel(plan.getPlan_period_start() + " - " + plan.getPlan_period_end());
        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        topPanel.add(titleLabel);
        topPanel.add(Box.createVerticalStrut(5));
        topPanel.add(dateLabel);

        add(topPanel, BorderLayout.NORTH);

        // 중앙: 참여 코드
        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerPanel.setOpaque(false);
        JLabel codeLabel = new JLabel("참여 코드: " + plan.getPlan_id());
        codeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        centerPanel.add(codeLabel);

        add(centerPanel, BorderLayout.CENTER);

        // 하단: 계획하기 버튼
        JButton planButton = new JButton("일정 계획하기");
        planButton.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        planButton.setBackground(new Color(222, 146, 124));
        planButton.setForeground(Color.WHITE);
        planButton.setFocusPainted(false);
        planButton.addActionListener(e -> mainApp.switchToPlanTrip(plan));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(planButton);

        add(bottomPanel, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }
}
