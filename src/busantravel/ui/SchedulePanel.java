package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.controller.PlanController;
import busantravel.model.TravelPlan;
import java.awt.*;
import java.io.InputStream;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class SchedulePanel extends JPanel {
    private JFrame parentFrame;
    private TravelPlannerApp mainApp;
    private PlanController controller;
    private Font jalnanFont;

    private JPanel upcomingPanel;
    private JPanel pastPanel;
    private JPanel mainContentPanel;

    public SchedulePanel(JFrame parentFrame, TravelPlannerApp mainApp) {
        this.parentFrame = parentFrame;
        this.mainApp = mainApp;
        this.controller = new PlanController(parentFrame, this, MemberSession.getId(), mainApp);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loadFont();

        add(buildLeftPanel(), BorderLayout.WEST);

        mainContentPanel = new JPanel(new BorderLayout());
        mainContentPanel.setBackground(Color.WHITE);
        add(mainContentPanel, BorderLayout.CENTER);
    }

    private JPanel buildLeftPanel() {
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton createTripBtn = new JButton("여행 일정 만들기");
        JButton enterCodeBtn = new JButton("참여 코드 입력");

        Dimension btnSize = new Dimension(180, 50);
        createTripBtn.setMaximumSize(btnSize);
        enterCodeBtn.setMaximumSize(btnSize);

        createTripBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        enterCodeBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

        createTripBtn.setBackground(ColorConstants.ACCENT);
        createTripBtn.setForeground(Color.WHITE);
        enterCodeBtn.setBackground(ColorConstants.ACCENT);
        enterCodeBtn.setForeground(Color.WHITE);

        createTripBtn.addActionListener(e -> controller.handleCreateTrip());
        enterCodeBtn.addActionListener(e -> controller.handleEnterCode());

        leftPanel.add(createTripBtn);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(enterCodeBtn);

        return leftPanel;
    }

    public void renderPlans(List<TravelPlan> upcoming, List<TravelPlan> past) {
        mainContentPanel.removeAll();

        if (upcoming.isEmpty() && past.isEmpty()) {
            showEmptyState();
        } else {
            getComponent(0).setVisible(true);
            mainContentPanel.add(buildCenterSection(upcoming, past), BorderLayout.CENTER);
        }

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
    }

    private JPanel buildCenterSection(List<TravelPlan> upcoming, List<TravelPlan> past) {
        int SECTION_WIDTH = 540;

        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        centerWrapper.setBackground(Color.WHITE);

        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setPreferredSize(new Dimension(SECTION_WIDTH, 680));
        section.setMaximumSize(new Dimension(SECTION_WIDTH, Integer.MAX_VALUE));
        section.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));

        JPanel upcomingContainer = buildTravelContainer("다가오는 여행", SECTION_WIDTH);
        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setBackground(Color.WHITE);
        JScrollPane upcomingScroll = new JScrollPane(upcomingPanel);
        upcomingScroll.setBorder(null);
        upcomingScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        upcomingScroll.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        upcomingContainer.add(upcomingScroll, BorderLayout.CENTER);
        for (int i = 0; i < upcoming.size(); i++) {
            JPanel card = createPlanCard(upcoming.get(i), true);
            if (i == upcoming.size() - 1) card.setBorder(BorderFactory.createEmptyBorder());
            upcomingPanel.add(card);
        }

        JPanel pastContainer = buildTravelContainer("지난 여행", SECTION_WIDTH);
        pastPanel = new JPanel();
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.Y_AXIS));
        pastPanel.setBackground(Color.WHITE);
        JScrollPane pastScroll = new JScrollPane(pastPanel);
        pastScroll.setBorder(null);
        pastScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pastScroll.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        pastContainer.add(pastScroll, BorderLayout.CENTER);
        for (int i = 0; i < past.size(); i++) {
            JPanel card = createPlanCard(past.get(i), false);
            if (i == past.size() - 1) card.setBorder(BorderFactory.createEmptyBorder());
            pastPanel.add(card);
        }

        section.add(upcomingContainer);
        section.add(Box.createVerticalStrut(20));
        section.add(pastContainer);

        centerWrapper.add(section);
        return centerWrapper;
    }

    private JPanel buildTravelContainer(String title, int width) {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(15, 0, 0, 0),
                BorderFactory.createLineBorder(ColorConstants.ACCENT)
            ),
            title,
            TitledBorder.LEFT, TitledBorder.TOP,
            jalnanFont.deriveFont(Font.BOLD, 26f),
            ColorConstants.ACCENT
        ));
        container.setPreferredSize(new Dimension(width, 280));
        return container;
    }

    private void showEmptyState() {
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp == getComponent(0)) { 
                comp.setVisible(false);
            }
        }

        mainContentPanel.removeAll();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel label = new JLabel("생성된 여행이 없습니다");
        label.setFont(jalnanFont.deriveFont(Font.BOLD, 22f));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(new Color(90, 90, 90));

        JButton createBtn = new JButton("여행 일정 만들기");
        JButton joinBtn = new JButton("참여 코드 입력");

        Dimension size = new Dimension(340, 80);

        createBtn.setPreferredSize(size);
        createBtn.setMaximumSize(size);
        createBtn.setMinimumSize(size);
        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        createBtn.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        createBtn.setMargin(new Insets(20, 40, 20, 40));

        joinBtn.setPreferredSize(size);
        joinBtn.setMaximumSize(size);
        joinBtn.setMinimumSize(size);
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        joinBtn.setMargin(new Insets(20, 40, 20, 40)); 

        createBtn.setBackground(ColorConstants.ACCENT);
        joinBtn.setBackground(ColorConstants.ACCENT);
        createBtn.setForeground(Color.WHITE);
        joinBtn.setForeground(Color.WHITE);
        createBtn.setFocusPainted(false);
        joinBtn.setFocusPainted(false);

        createBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        joinBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        createBtn.addActionListener(e -> controller.handleCreateTrip());
        joinBtn.addActionListener(e -> controller.handleEnterCode());

        panel.add(Box.createVerticalGlue());
        panel.add(label);
        panel.add(Box.createVerticalStrut(40));
        panel.add(createBtn);
        panel.add(Box.createVerticalStrut(20));
        panel.add(joinBtn);
        panel.add(Box.createVerticalGlue());

        mainContentPanel.add(panel, BorderLayout.CENTER);
    }


    private JPanel createPlanCard(TravelPlan plan, boolean isUpcoming) {
        int CARD_WIDTH = 480;

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)));
        card.setPreferredSize(new Dimension(CARD_WIDTH, 100));
        card.setMaximumSize(new Dimension(CARD_WIDTH, 100));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        left.setBackground(Color.WHITE);

        JPanel iconPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
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
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));

        JLabel title = new JLabel(plan.getPlan_title());
        title.setFont(jalnanFont.deriveFont(Font.BOLD, 16f));

        String dateRange = plan.getPlan_period_start().toLocalDate() + " - " +
                            plan.getPlan_period_end().toLocalDate();
        JLabel date = new JLabel(dateRange);
        date.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        date.setForeground(Color.GRAY);

        JLabel code = new JLabel("방 코드 : " + plan.getPlan_id());
        code.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        code.setForeground(new Color(90, 90, 90));

        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(2));
        textPanel.add(date);
        textPanel.add(code);

        left.add(iconPanel);
        left.add(textPanel);
        card.add(left, BorderLayout.CENTER);

        if (isUpcoming) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);

            JButton deleteBtn = new JButton("삭제");
            deleteBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
            deleteBtn.setBackground(new Color(220, 220, 220));
            deleteBtn.setFocusPainted(false);
            deleteBtn.addActionListener(e -> controller.handleDeletePlan(plan.getPlan_id()));
            buttonPanel.add(deleteBtn);

            JButton arrowBtn = new JButton(">");
            arrowBtn.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
            arrowBtn.setPreferredSize(new Dimension(60, 100));
            arrowBtn.setFocusPainted(false);
            arrowBtn.setBackground(Color.WHITE);
            arrowBtn.setBorderPainted(false);
            arrowBtn.addActionListener(e -> {
                if (mainApp != null) {
                    mainApp.switchToFullSchedule(plan);
                }
            });
            buttonPanel.add(arrowBtn);

            card.add(buttonPanel, BorderLayout.EAST);
        } else {
            JButton reviewBtn = new JButton("후기 작성");
            reviewBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
            reviewBtn.setBackground(ColorConstants.ACCENT);
            reviewBtn.setForeground(Color.WHITE);
            reviewBtn.setFocusPainted(false);
            reviewBtn.addActionListener(e -> {
                new ReviewPopup(parentFrame, null, () -> controller.refreshRoomLists()).setVisible(true);
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(reviewBtn);
            card.add(buttonPanel, BorderLayout.EAST);
        }

        return card;
    }
    
    public PlanController getController() {
        return controller;
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(jalnanFont);
            } else {
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }
}
