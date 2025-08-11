package busantravel;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class TripSchedulePanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private TravelPlannerApp mainApp; // TravelPlannerApp 참조 추가

    // 생성자에 TravelPlannerApp mainApp 인스턴스를 받도록 수정
    public TripSchedulePanel(TravelPlannerApp mainApp) {
        this.mainApp = mainApp; // 참조 저장
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        cardLayout = new CardLayout(); // TripSchedulePanel 자신의 CardLayout
        mainPanel = new JPanel(cardLayout); // TripSchedulePanel 자신의 mainPanel
        mainPanel.setOpaque(false);

        // 초기 화면 (아무것도 선택되지 않았을 때)
        JPanel initialPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // 좌측 정렬 FlowLayout
        initialPanel.setBackground(Color.WHITE);
        initialPanel.setOpaque(false);

        JLabel selectPrompt = new JLabel("<html><div style='color: #888;'>여행 일정을 선택해주세요.</div></html>");
        selectPrompt.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        selectPrompt.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // 패딩 완전 제거 또는 미세 조절

        initialPanel.add(selectPrompt);
        mainPanel.add(initialPanel, "Initial");

        add(mainPanel, BorderLayout.CENTER);
    }

    // TripRoom이 로드될 때 호출되어 새 UI를 보여줍니다.
    public void loadTripRoom(TripRoom room) {
        mainPanel.removeAll(); // 이전 내용을 모두 제거

        JPanel newUiPanel = new JPanel();
        newUiPanel.setLayout(new GridBagLayout()); // GridBagLayout으로 중앙 정렬
        newUiPanel.setOpaque(false); // 배경 투명 처리 (상위 TripSchedulePanel의 배경이 보임)

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 10, 0); // 각 컴포넌트 간 여백
        gbc.fill = GridBagConstraints.NONE; // 컴포넌트 크기 조정 안함
        gbc.weightx = 0; // 추가 공간 분배 안함
        gbc.anchor = GridBagConstraints.CENTER; // 중앙 정렬

        // 여행 제목 라벨
        JLabel titleLabel = new JLabel("<html><div style='text-align: center; font-size: 20pt; font-weight: bold; color: #333;'>" + room.getTitle() + "</div></html>");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy++;
        gbc.insets = new Insets(20, 0, 20, 0); // 제목 상하 여백
        newUiPanel.add(titleLabel, gbc);

        LocalDate today = LocalDate.now();
        // 여행 종료일이 오늘과 같거나 이전이면 지난 여행으로 간주
        if (room.getEndDate().isBefore(today) || room.getEndDate().isEqual(today)) {
            // 지난 여행일 때 후기 버튼들
            JButton writeReviewButton = new JButton("후기 쓰기");
            writeReviewButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            writeReviewButton.setBackground(new Color(222, 146, 124));
            writeReviewButton.setForeground(Color.WHITE);
            writeReviewButton.setFocusPainted(false);
            writeReviewButton.setPreferredSize(new Dimension(160, 50));
            writeReviewButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, room.getTitle() + " 후기 쓰기 기능 (UI만 제공)", "안내", JOptionPane.INFORMATION_MESSAGE);
            });
            gbc.gridy++;
            gbc.insets = new Insets(10, 0, 5, 0); // 버튼 간격
            newUiPanel.add(writeReviewButton, gbc);

            JButton viewReviewButton = new JButton("후기 보기");
            viewReviewButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            viewReviewButton.setBackground(new Color(222, 146, 124));
            viewReviewButton.setForeground(Color.WHITE);
            viewReviewButton.setFocusPainted(false);
            viewReviewButton.setPreferredSize(new Dimension(160, 50));
            viewReviewButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, room.getTitle() + " 후기 보기 기능 (UI만 제공)", "안내", JOptionPane.INFORMATION_MESSAGE);
            });
            gbc.gridy++;
            gbc.insets = new Insets(5, 0, 10, 0); // 버튼 간격
            newUiPanel.add(viewReviewButton, gbc);
        }

        // --- 여행 계획 세우기 버튼 (지난 여행과 다가오는 여행 모두 포함) ---
        JButton planTripButton = new JButton("여행 계획 세우기");
        planTripButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        planTripButton.setBackground(new Color(222, 146, 124));
        planTripButton.setForeground(Color.WHITE);
        planTripButton.setFocusPainted(false);
        planTripButton.setPreferredSize(new Dimension(160, 50));
        planTripButton.addActionListener(e -> {
            // JDialog를 띄우는 대신 mainApp을 통해 PlanTripPanel로 화면 전환
            mainApp.switchToPlanTrip(room); // TravelPlannerApp의 switchToPlanTrip 메서드 호출
        });
        gbc.gridy++; // 버튼 추가 위치 조정
        gbc.insets = new Insets(10, 0, 5, 0);
        newUiPanel.add(planTripButton, gbc);

        // --- 체크리스트 버튼 (다가오는 여행일 때만 추가) ---
        // (room.getEndDate().isBefore(today) || room.getEndDate().isEqual(today))
        // 이 조건은 "이미 지난 여행"이라는 뜻입니다.
        // !로 감싸면 "아직 지나지 않은 여행" 즉, 다가오는 여행에 해당합니다.
        if (!room.getEndDate().isBefore(today) && !room.getEndDate().isEqual(today)) {
            JButton checklistButton = new JButton("체크리스트");
            checklistButton.setFont(new Font("맑은 고딕", Font.BOLD, 13));
            checklistButton.setBackground(new Color(222, 146, 124));
            checklistButton.setForeground(Color.WHITE);
            checklistButton.setFocusPainted(false);
            checklistButton.setPreferredSize(new Dimension(160, 50));
            checklistButton.addActionListener(e -> {
                JOptionPane.showMessageDialog(this, room.getTitle() + " 체크리스트 기능 (UI만 제공)", "안내", JOptionPane.INFORMATION_MESSAGE);
            });
            gbc.gridy++;
            gbc.insets = new Insets(5, 0, 10, 0); // 버튼 간격
            newUiPanel.add(checklistButton, gbc);
        }

        String cardName = "TripDetail_" + room.getRoomCode();
        mainPanel.add(newUiPanel, cardName);
        cardLayout.show(mainPanel, cardName); // TripSchedulePanel 내부의 CardLayout 전환

        mainPanel.revalidate();
        mainPanel.repaint();
    }
}