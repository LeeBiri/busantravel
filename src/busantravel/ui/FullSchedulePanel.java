package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;
import busantravel.model.TravelPlan;
import busantravel.service.ScheduledPlaceService;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class FullSchedulePanel extends JPanel {

    private final TravelPlannerApp mainApp;
    private final TravelPlan plan;
    private final Runnable onBackCallback;
    private Font jalnanFont;


        public FullSchedulePanel(TravelPlannerApp mainApp, TravelPlan plan, Runnable onBackCallback) {
            this.mainApp = mainApp;
            this.plan = plan;
            this.onBackCallback = onBackCallback;

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loadFont();
        // ⬆️ 상단 패널 (제목 + 버튼)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel(plan.getPlan_title());
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
        titleLabel.setForeground(new Color(80, 40, 40));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        // 체크리스트 버튼
        JButton checklistBtn = new JButton("체크리스트");
        checklistBtn.setPreferredSize(new Dimension(140, 40));
        checklistBtn.setMaximumSize(new Dimension(160, 40));
        checklistBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        checklistBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        checklistBtn.setBackground(ColorConstants.ACCENT);
        checklistBtn.setForeground(Color.WHITE);
        checklistBtn.setFocusPainted(false);
    
        // 멤버리스트 버튼
        JButton membersBtn = new JButton("멤버리스트");
        membersBtn.setPreferredSize(new Dimension(140, 40));
        membersBtn.setMaximumSize(new Dimension(160, 40));
        membersBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        membersBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        membersBtn.setBackground(ColorConstants.ACCENT);
        membersBtn.setForeground(Color.WHITE);
        membersBtn.setFocusPainted(false);

        checklistBtn.setFocusPainted(false);
        membersBtn.setFocusPainted(false);
        buttonPanel.add(checklistBtn);
        buttonPanel.add(membersBtn);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        // 좌측 일정 리스트
        JPanel scheduleListPanel = createScheduleListPanel();
        JScrollPane scrollPane = new JScrollPane(scheduleListPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(900, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // 우측 지도 자리 (임시)
        JPanel mapPanel = new JPanel();
        mapPanel.setBackground(Color.GREEN);
        mapPanel.setPreferredSize(new Dimension(550, 0));
        mapPanel.add(new JLabel("지도 자리"));

        // 메인 좌측 패널 구성
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        leftPanel.add(headerPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.CENTER);
        add(mapPanel, BorderLayout.EAST);
    }


    private JPanel createScheduleListPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20, 40, 20, 40));

        List<ScheduledPlace> schedules = ScheduledPlaceService.getSchedulesByPlanId(plan.getPlan_id());

        Map<LocalDate, List<ScheduledPlace>> grouped = schedules.stream()
                .collect(Collectors.groupingBy(s -> s.getScheduled_date().toLocalDate()));

        LocalDate start = plan.getPlan_period_start().toLocalDate();
        LocalDate end = plan.getPlan_period_end().toLocalDate();
        int totalDays = (int) (end.toEpochDay() - start.toEpochDay()) + 1;

        for (int i = 0; i < totalDays; i++) {
            LocalDate date = start.plusDays(i);
            List<ScheduledPlace> daySchedules = grouped.getOrDefault(date, List.of());

            // 일차 패널 추가
            panel.add(createDaySchedulePanel(i + 1, date, daySchedules));
            panel.add(Box.createVerticalStrut(20));
        }

        return panel;
    }

    private JPanel createDaySchedulePanel(int dayIndex, LocalDate date, List<ScheduledPlace> places) {
        loadFont();
        JPanel dayPanel = new JPanel();
        dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
        dayPanel.setBackground(new Color(255, 241, 241));
        dayPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 204, 204)),
                new EmptyBorder(15, 20, 15, 20)
        ));

        String dayTitle = String.format("%d일차 %s", dayIndex,
                date.format(DateTimeFormatter.ofPattern("M/d (E)", java.util.Locale.KOREAN)));
        JLabel titleLabel = new JLabel(dayTitle);
        titleLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
        titleLabel.setForeground(new Color(50, 30, 30));
        dayPanel.add(titleLabel);
        dayPanel.add(Box.createVerticalStrut(15));

        // 장소들 표시
        int order = 1;
        for (ScheduledPlace place : places) {
            dayPanel.add(createPlaceCard(place, order++));
            dayPanel.add(Box.createVerticalStrut(10));
        }

        // 장소 추가 버튼
        JButton addPlaceBtn = new JButton("＋ 장소 추가");
        addPlaceBtn.setPreferredSize(new Dimension(140, 40));
        addPlaceBtn.setMaximumSize(new Dimension(160, 40));
        addPlaceBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addPlaceBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        addPlaceBtn.setBackground(ColorConstants.ACCENT);
        addPlaceBtn.setForeground(Color.WHITE);
        addPlaceBtn.setFocusPainted(false);
        addPlaceBtn.addActionListener(e -> mainApp.switchToPlanTrip(plan, dayIndex));

        dayPanel.add(Box.createVerticalStrut(5)); 
        dayPanel.add(addPlaceBtn); 

        return dayPanel;
    }

private JPanel createPlaceCard(ScheduledPlace place, int order) {
    loadFont();
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.X_AXIS));
    card.setBackground(Color.WHITE);
    card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(10, 10, 10, 10)
    ));

    PlaceDAO placeDAO = new PlaceDAO();
    Object rawPlace = placeDAO.getPlaceByTypeAndId(place.getP_type(), place.getP_id());
    PlaceData data = PlaceData.from(rawPlace);

    // 순번
    JLabel orderLabel = new JLabel(order + "");
    orderLabel.setPreferredSize(new Dimension(30, 56));
    orderLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 26f));
    orderLabel.setHorizontalAlignment(SwingConstants.CENTER);
    orderLabel.setVerticalAlignment(SwingConstants.CENTER);

    // 썸네일
    JLabel imageLabel = buildThumb(data.thumbUrl, 56, 56);
    imageLabel.setPreferredSize(new Dimension(56, 56));
    imageLabel.setMaximumSize(new Dimension(56, 56));
    imageLabel.setMinimumSize(new Dimension(56, 56));

    // 텍스트 정보
    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    infoPanel.setOpaque(false);
    infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

    JLabel nameLabel = new JLabel(data.name);
    nameLabel.setFont(jalnanFont.deriveFont(Font.BOLD, 18f));

    JLabel timeLabel = new JLabel("체류 시간: " + place.getExpected_start()+ " ~ " + place.getExpected_dutarion());
    timeLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

    JLabel categoryLabel = new JLabel(place.getP_type());
    categoryLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

    infoPanel.add(nameLabel);
    infoPanel.add(Box.createVerticalStrut(3));
    infoPanel.add(timeLabel);
    infoPanel.add(categoryLabel);

    // 조립
    card.add(orderLabel);
    card.add(Box.createHorizontalStrut(6));
    card.add(imageLabel);
    card.add(infoPanel);

    return card;
}

    private JLabel buildThumb(String url, int w, int h) {
        JLabel lbl = new JLabel("No Img", SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(new Color(240, 240, 240));
        lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        lbl.setPreferredSize(new Dimension(w, h));

        if (url == null || url.isBlank()) return lbl;

        new Thread(() -> {
            try {
                ImageIcon icon = new ImageIcon(new URL(url));
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    lbl.setText("");
                    lbl.setIcon(new ImageIcon(scaled));
                });
            } catch (Exception ignored) {}
        }).start();

        return lbl;
    }

    private static class PlaceData {
    final String id, name, addr, type, thumbUrl;

    private PlaceData(String id, String name, String addr, String type, String thumbUrl) {
        this.id = id; this.name = name; this.addr = addr; this.type = type; this.thumbUrl = thumbUrl;
    }

    static PlaceData from(Object o) {
        if (o instanceof Tourist t)
            return new PlaceData(t.getT_id(), t.getT_place(), t.getT_addr(), "관광지", t.getT_thum());
        if (o instanceof Restaurant r)
            return new PlaceData(r.getR_id(), r.getR_place(), r.getR_addr(), "맛집", r.getR_thum());
        if (o instanceof Lodging l)
            return new PlaceData(l.getL_id(), l.getL_place(), l.getL_addr(), "숙소", null);
        if (o instanceof Festival f)
            return new PlaceData(f.getF_id(), f.getF_name(), f.getF_addr(), "축제", f.getF_thum());
        return new PlaceData("", "알 수 없는 장소", "", "기타", null);
    }
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
