package busantravel.ui;

import busantravel.controller.PlanController;
import busantravel.model.TravelPlan;

import java.awt.*;
import javax.swing.*;
import busantravel.ui.ColorConstants;
import javax.swing.border.TitledBorder;

public class SchedulePanel extends JPanel {
    private JFrame parentFrame;  // 원래 화면 전환, 다이얼로그 표시 등에 사용
    private TravelPlannerApp mainApp; // 원래 다른 화면으로 전환할 때 사용
    private JPanel upcomingPanel;
    private JPanel pastPanel;
    PlanController controller;

    public SchedulePanel(JFrame parentFrame, TravelPlannerApp mainApp) {
        this.parentFrame = parentFrame;
        this.mainApp = mainApp;
        this.controller = new PlanController(parentFrame, this, "aaa");
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // 좌측 버튼 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton createTripBtn = new JButton("여행 일정 만들기");
        JButton enterCodeBtn = new JButton("참여 코드 입력");

        Dimension btnSize = new Dimension(180, 50);
        createTripBtn.setMaximumSize(btnSize);
        enterCodeBtn.setMaximumSize(btnSize);

        createTripBtn.setBackground(ColorConstants.ACCENT);
        createTripBtn.setForeground(Color.WHITE);
        enterCodeBtn.setBackground(ColorConstants.ACCENT);
        enterCodeBtn.setForeground(Color.WHITE);

        leftPanel.add(createTripBtn);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(enterCodeBtn);

        add(leftPanel, BorderLayout.WEST);

        createTripBtn.addActionListener(e -> controller.handleCreateTrip());
        enterCodeBtn.addActionListener(e -> controller.handleEnterCode());

     // ❶ 오른쪽: ‘고정폭 섹션’을 가운데 놓기 위한 래퍼
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(Color.WHITE);

        // ❷ 실제 섹션(고정 폭)
        int SECTION_WIDTH = 540; // 스샷처럼 적당히 좁게
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setPreferredSize(new Dimension(SECTION_WIDTH, 680)); // 높이는 상황에 맞게
        section.setMaximumSize(new Dimension(SECTION_WIDTH, Integer.MAX_VALUE));
        section.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        // ❸ "다가오는 여행" 컨테이너
        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setBackground(Color.WHITE);
        upcomingPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorConstants.ACCENT),
                "다가오는 여행",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 16),
                ColorConstants.ACCENT
        ));

        // ❹ "지난 여행" 컨테이너
        pastPanel = new JPanel();
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.Y_AXIS));
        pastPanel.setBackground(Color.WHITE);
        pastPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(ColorConstants.ACCENT),
                "지난 여행",
                TitledBorder.LEFT, TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 16),
                ColorConstants.ACCENT
        ));

        // ❺ 섹션에 스크롤 달아서 추가
        JScrollPane upcomingScroll = new JScrollPane(upcomingPanel);
        upcomingScroll.setBorder(null);
        upcomingScroll.setPreferredSize(new Dimension(SECTION_WIDTH, 280));
        upcomingScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JScrollPane pastScroll = new JScrollPane(pastPanel);
        pastScroll.setBorder(null);
        pastScroll.setPreferredSize(new Dimension(SECTION_WIDTH, 280));
        pastScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        section.add(upcomingScroll);
        section.add(Box.createVerticalStrut(20));
        section.add(pastScroll);

        centerWrapper.add(section);

        // 가운데 래퍼를 CENTER에 붙이기
        add(centerWrapper, BorderLayout.CENTER);
    }

    public void renderPlans(java.util.List<TravelPlan> upcoming, java.util.List<TravelPlan> past) {
        upcomingPanel.removeAll();
        pastPanel.removeAll();

        // 다가오는 여행
        for (int i = 0; i < upcoming.size(); i++) {
            JPanel card = createPlanCard(upcoming.get(i), true);
            if (i == upcoming.size() - 1) {
                // 마지막 항목은 가로선 제거
                card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            upcomingPanel.add(card);
        }

        // 지난 여행
        for (int i = 0; i < past.size(); i++) {
            JPanel card = createPlanCard(past.get(i), false);
            if (i == past.size() - 1) {
                card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
            pastPanel.add(card);
        }

        upcomingPanel.revalidate();
        upcomingPanel.repaint();
        pastPanel.revalidate();
        pastPanel.repaint();
    }

    private JPanel createPlanCard(TravelPlan plan, boolean isUpcoming) {
        int CARD_WIDTH = 480;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        card.setPreferredSize(new Dimension(CARD_WIDTH, 100));
        card.setMaximumSize(new Dimension(CARD_WIDTH, 100));
        card.setAlignmentX(Component.CENTER_ALIGNMENT); // 중앙 정렬

        // 왼쪽 묶음
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10)); // ← 여백 넓힘
        left.setBackground(Color.WHITE);

        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(new Color(210, 225, 238));
                g.fillOval(6, 6, 26, 26);
            }
        };
        iconPanel.setOpaque(false);
        iconPanel.setPreferredSize(new Dimension(40, 40));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0)); // 내부 여백

        JLabel title = new JLabel(plan.getPlan_title());
        title.setFont(new Font("맑은 고딕", Font.BOLD, 14));

        String dateRange = plan.getPlan_period_start().toLocalDate() + " - " +
                           plan.getPlan_period_end().toLocalDate();
        JLabel date = new JLabel(dateRange);
        date.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        date.setForeground(Color.GRAY);

        JLabel code = new JLabel("방 코드 : " + plan.getPlan_id());
        code.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        code.setForeground(new Color(90, 90, 90));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(date);
        textPanel.add(code);

        left.add(iconPanel);
        left.add(textPanel);
        card.add(left, BorderLayout.CENTER);

        if (isUpcoming) {
            JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 10));
            btns.setBackground(Color.WHITE);

            JButton planBtn = new JButton("일정 계획하기");
            planBtn.setBackground(ColorConstants.ACCENT);
            planBtn.setForeground(Color.WHITE);
            planBtn.setFocusPainted(false);

            planBtn.addActionListener(e -> {
                PlanTripPanel tripPanel = new PlanTripPanel(
                    plan.getPlan_period_start(),
                    plan.getPlan_period_end(),
                    mainApp,
                    plan.getPlan_title()
                );

                JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(SchedulePanel.this);
                frame.setContentPane(tripPanel);
                frame.revalidate();
                frame.repaint();
            });


            JButton checklistBtn = new JButton("체크리스트");
            checklistBtn.setBackground(new Color(100, 150, 240));
            checklistBtn.setForeground(Color.WHITE);

            btns.add(planBtn);
            btns.add(checklistBtn);

            card.add(btns, BorderLayout.EAST);
        }

        return card;
    }

}
