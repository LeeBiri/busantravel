package busantravel.ui;

import busantravel.controller.PlanController;
import busantravel.model.TravelPlan;
import java.awt.*;
import java.io.InputStream;
import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * 사용자의 여행 일정 목록을 관리하고 표시하는 패널입니다.
 * '다가오는 여행'과 '지난 여행'으로 구분하여 보여주며,
 * 새로운 여행을 생성하거나 참여 코드로 기존 여행에 참여하는 기능을 제공합니다.
 */
public class SchedulePanel extends JPanel {
    private JFrame parentFrame;  // 다이얼로그 표시 등에 사용되는 부모 프레임
    private TravelPlannerApp mainApp; // 화면 전환을 위한 메인 애플리케이션 참조
    private JPanel upcomingPanel; // 다가오는 여행 목록을 표시하는 패널
    private JPanel pastPanel; // 지난 여행 목록을 표시하는 패널
    PlanController controller; // 여행 생성 및 참여 관련 로직을 처리하는 컨트롤러
    private Font jalnanFont;

    /**
     * SchedulePanel의 새 인스턴스를 생성합니다.
     *
     * @param parentFrame 부모 JFrame 객체.
     * @param mainApp     메인 애플리케이션(TravelPlannerApp)의 참조.
     */
    public SchedulePanel(JFrame parentFrame, TravelPlannerApp mainApp) {
        this.parentFrame = parentFrame;
        this.mainApp = mainApp;
        this.controller = new PlanController(parentFrame, this, "aaa");
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loadFont();
        // 좌측 버튼 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton createTripBtn = new JButton("여행 일정 만들기");
        createTripBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        JButton enterCodeBtn = new JButton("참여 코드 입력");
        enterCodeBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

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
        section.setBorder(BorderFactory.createEmptyBorder(80, 0, 0, 0));

        // ❸ "다가오는 여행" 컨테이너
        JPanel upcomingContainer = new JPanel(new BorderLayout());
        upcomingContainer.setBackground(Color.WHITE);
        upcomingContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(15, 0, 0, 0),
                        BorderFactory.createLineBorder(ColorConstants.ACCENT)
                ),
                "다가오는 여행",
                TitledBorder.LEFT, TitledBorder.TOP,
                jalnanFont.deriveFont(Font.BOLD, 26f),
                ColorConstants.ACCENT
        ));
        upcomingContainer.setPreferredSize(new Dimension(SECTION_WIDTH, 280));

        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setBackground(Color.WHITE);

        JScrollPane upcomingScroll = new JScrollPane(upcomingPanel);
        upcomingScroll.setBorder(null);
        upcomingScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        upcomingScroll.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        upcomingScroll.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        upcomingContainer.add(upcomingScroll, BorderLayout.CENTER);

        // ❹ "지난 여행" 컨테이너
        JPanel pastContainer = new JPanel(new BorderLayout());
        pastContainer.setBackground(Color.WHITE);
        pastContainer.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createEmptyBorder(15, 0, 0, 0),
                        BorderFactory.createLineBorder(ColorConstants.ACCENT)
                ),
                "지난 여행",
                TitledBorder.LEFT, TitledBorder.TOP,
                jalnanFont.deriveFont(Font.BOLD, 26f),
                ColorConstants.ACCENT
        ));
        pastContainer.setPreferredSize(new Dimension(SECTION_WIDTH, 280));

        pastPanel = new JPanel();
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.Y_AXIS));
        pastPanel.setBackground(Color.WHITE);

        JScrollPane pastScroll = new JScrollPane(pastPanel);
        pastScroll.setBorder(null);
        pastScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pastScroll.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        pastScroll.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        pastContainer.add(pastScroll, BorderLayout.CENTER);

        // ❺ 섹션에 컨테이너 추가
        section.add(upcomingContainer);
        section.add(Box.createVerticalStrut(20));
        section.add(pastContainer);

        centerWrapper.add(section);

        // 가운데 래퍼를 CENTER에 붙이기
        add(centerWrapper, BorderLayout.CENTER);
    }

    /**
     * 다가오는 여행과 지난 여행 목록을 받아와 화면에 렌더링합니다.
     * 각 목록에 대해 카드 형태의 UI를 생성하여 패널에 추가합니다.
     *
     * @param upcoming 다가오는 여행 계획 목록
     * @param past     지난 여행 계획 목록
     */
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

    /**
     * 개별 여행 계획을 표시하는 카드 UI를 생성합니다.
     *
     * @param plan       표시할 여행 계획 정보 (TravelPlan 객체)
     * @param isUpcoming 다가오는 여행인지 여부 (true일 경우 '일정 계획하기' 등 버튼 추가)
     * @return 생성된 카드 JPanel 객체
     */
    private JPanel createPlanCard(TravelPlan plan, boolean isUpcoming) {
        loadFont();
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

            // 화살표 오른쪽 정렬
            JPanel arrowPanel = new JPanel(new BorderLayout());
            arrowPanel.setBackground(Color.WHITE);
            arrowPanel.add(arrowBtn, BorderLayout.CENTER);
            arrowPanel.setPreferredSize(new Dimension(60, 100));

            card.add(arrowPanel, BorderLayout.EAST);
        }


        return card;
    }

    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f); // 기본 크기 14로 설정
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(jalnanFont);
            } else {
                System.err.println("폰트 파일을 찾을 수 없습니다! 기본 폰트 사용.");
                jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
            }
        } catch (Exception e) {
            System.err.println("폰트 로드 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            jalnanFont = new Font("맑은 고딕", Font.PLAIN, 14);
        }
    }
}
