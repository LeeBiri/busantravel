package busantravel.ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchedulePanel extends JPanel {
    private JFrame parentFrame;
    private TravelPlannerApp mainApp; // TravelPlannerApp 참조 (화면 전환을 위해 필요)
    private JPanel upcomingPanel;
    private JPanel pastPanel;

    // SchedulePanel 생성자: JFrame과 TravelPlannerApp 참조를 인자로 받음
    public SchedulePanel(JFrame parentFrame, TravelPlannerApp mainApp) {
        this.parentFrame = parentFrame;
        this.mainApp = mainApp; // mainApp 참조 저장
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // 좌측 버튼 패널
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton createTripBtn = new JButton("여행 일정 만들기");
        JButton enterCodeBtn = new JButton("참여 코드 입력");

        createTripBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        enterCodeBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension btnSize = new Dimension(180, 50);
        createTripBtn.setMaximumSize(btnSize);
        createTripBtn.setPreferredSize(btnSize);
        enterCodeBtn.setMaximumSize(btnSize);
        enterCodeBtn.setPreferredSize(btnSize);

        createTripBtn.setBackground(new Color(222, 146, 124));
        createTripBtn.setForeground(Color.WHITE);
        createTripBtn.setFocusPainted(false);
        enterCodeBtn.setBackground(new Color(222, 146, 124));
        enterCodeBtn.setForeground(Color.WHITE);
        enterCodeBtn.setFocusPainted(false);

        createTripBtn.addActionListener(e -> {
            CreateTripDialog dialog = new CreateTripDialog(parentFrame);
            dialog.setVisible(true);
            refreshRoomLists();
        });

        enterCodeBtn.addActionListener(e -> {
            EnterCodeDialog dialog = new EnterCodeDialog(parentFrame, this);
            dialog.setVisible(true);
        });

        leftPanel.add(createTripBtn);
        leftPanel.add(Box.createVerticalStrut(15));
        leftPanel.add(enterCodeBtn);

        add(leftPanel, BorderLayout.WEST);

        // 오른쪽 전체 패널
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 상하좌우 패딩은 유지
        rightPanel.setOpaque(false);

        // 다가오는 여행 패널
        upcomingPanel = new JPanel();
        upcomingPanel.setLayout(new BoxLayout(upcomingPanel, BoxLayout.Y_AXIS));
        upcomingPanel.setBackground(Color.WHITE);
        // 핑크색 TitledBorder 유지
        upcomingPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(222, 146, 124)),
                "다가오는 여행",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 18),
                new Color(222, 146, 124)
        ));

        // 지난 여행 패널
        pastPanel = new JPanel();
        pastPanel.setLayout(new BoxLayout(pastPanel, BoxLayout.Y_AXIS));
        pastPanel.setBackground(Color.WHITE);
        // 핑크색 TitledBorder 유지
        pastPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(222, 146, 124)),
                "지난 여행",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("맑은 고딕", Font.BOLD, 18),
                new Color(222, 146, 124)
        ));

        // 스크롤 가능한 스크롤패널 (크기 확장 및 테두리 제거)
        JScrollPane upcomingScroll = new JScrollPane(upcomingPanel);
        upcomingScroll.setPreferredSize(new Dimension(400, 350));
        upcomingScroll.getVerticalScrollBar().setUnitIncrement(16);
        upcomingScroll.setBorder(BorderFactory.createEmptyBorder()); // JScrollPane 자체의 테두리 제거

        JScrollPane pastScroll = new JScrollPane(pastPanel);
        pastScroll.setPreferredSize(new Dimension(400, 350));
        pastScroll.getVerticalScrollBar().setUnitIncrement(16);
        pastScroll.setBorder(BorderFactory.createEmptyBorder()); // JScrollPane 자체의 테두리 제거

        // 여행 목록 세로 배치 패널
        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.add(upcomingScroll);
        listPanel.add(Box.createVerticalStrut(20)); // 스크롤 패널 사이 간격
        listPanel.add(pastScroll);
        listPanel.setOpaque(false);
        listPanel.setBorder(BorderFactory.createEmptyBorder()); // listPanel의 테두리 제거

        rightPanel.add(listPanel, BorderLayout.CENTER);

        rightPanel.setBorder(BorderFactory.createEmptyBorder()); // rightPanel의 테두리 제거

        // 중앙 정렬을 위한 래퍼 패널
        JPanel centerWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centerWrapper.setBackground(Color.WHITE);
        centerWrapper.setOpaque(true);
        centerWrapper.add(rightPanel);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder()); // centerWrapper의 테두리 제거

        // 최상위 스크롤 패널로 전체 감싸기
        JScrollPane mainScrollPane = new JScrollPane(centerWrapper);
        mainScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(20);
        mainScrollPane.setBorder(BorderFactory.createEmptyBorder()); // mainScrollPane의 테두리 제거

        add(mainScrollPane, BorderLayout.CENTER);

        refreshRoomLists();
    }

    public void refreshRoomLists() {
        upcomingPanel.removeAll();
        pastPanel.removeAll();

        TripRoomManager manager = TripRoomManager.getInstance();
        Map<String, TripRoom> rooms = manager.getAllRooms();

        LocalDate today = LocalDate.now();

        List<TripRoom> upcoming = new ArrayList<>();
        List<TripRoom> past = new ArrayList<>();

        for (TripRoom room : rooms.values()) {
            // 종료일이 오늘과 같거나 이전이면 지난 여행으로 분류
            if (room.getEndDate().isBefore(today) || room.getEndDate().isEqual(today)) {
                past.add(room);
            } else {
                upcoming.add(room);
            }
        }

        for (TripRoom room : upcoming) {
            upcomingPanel.add(createRoomCard(room, true));
        }
        for (TripRoom room : past) {
            pastPanel.add(createRoomCard(room, false));
        }

        upcomingPanel.revalidate();
        upcomingPanel.repaint();
        pastPanel.revalidate();
        pastPanel.repaint();
    }

    private JPanel createRoomCard(TripRoom room, boolean isUpcoming) {
        JPanel container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        container.setBackground(Color.WHITE);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        Color circleColor = room.getEndDate().isBefore(LocalDate.now()) ? new Color(255, 223, 200) : new Color(173, 216, 230);
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(circleColor);
                g.fillOval(0, 0, 40, 40);
            }
        };
        circle.setPreferredSize(new Dimension(40, 40));
        circle.setOpaque(false);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        textPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        JLabel titleLabel = new JLabel(room.getTitle());
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));

        JLabel dateLabel = new JLabel(room.getStartDate() + " - " + room.getEndDate());
        dateLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        dateLabel.setForeground(Color.GRAY);

        JLabel codeLabel = new JLabel("방 코드 : " + room.getRoomCode());
        codeLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        codeLabel.setForeground(Color.GRAY);

        textPanel.add(titleLabel);
        textPanel.add(dateLabel);
        textPanel.add(codeLabel);

        JPanel leftContents = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftContents.setOpaque(false);
        leftContents.add(circle);
        leftContents.add(textPanel);

        card.add(leftContents, BorderLayout.WEST);
        container.add(card);

        if (isUpcoming) {
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
            buttonPanel.setBackground(Color.WHITE);

            JButton planButton = new JButton("일정 계획하기");
            planButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            planButton.setBackground(new Color(222, 146, 124));
            planButton.setForeground(Color.WHITE);
            planButton.setFocusPainted(false);
            planButton.addActionListener(e -> mainApp.switchToPlanTrip(room));

            JButton checklistButton = new JButton("체크리스트");
            checklistButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
            checklistButton.setBackground(new Color(100, 150, 240));
            checklistButton.setForeground(Color.WHITE);
            checklistButton.setFocusPainted(false);
            checklistButton.addActionListener(e -> JOptionPane.showMessageDialog(parentFrame, "체크리스트 기능은 준비 중입니다."));

            buttonPanel.add(planButton);
            buttonPanel.add(checklistButton);
            container.add(buttonPanel);
        }

        return container;
    }

}
