package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.dao.TravelPlanMemberDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Member;
import busantravel.model.Restaurant;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;
import busantravel.model.TravelPlan;
import busantravel.service.ScheduledPlaceService;
import busantravel.util.ApiHandler;
import busantravel.util.Coordinate;
import busantravel.util.PlaceInfo;
import busantravel.util.RoutePoint;
import busantravel.util.StaticMapGenerator;
import busantravel.util.TmapApiHandler;
import busantravel.util.TmapRouteInfo;
import busantravel.util.TmapRouteResult;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    private JLabel mapLabel;
    private JPanel routeInfoPanel;
    private final ApiHandler apiHandler = new ApiHandler();
    private final TmapApiHandler tmapApiHandler = new TmapApiHandler(TMAP_API_KEY);
    private final StaticMapGenerator mapGenerator;
    private static final String TMAP_API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg";


    public FullSchedulePanel(TravelPlannerApp mainApp, TravelPlan plan, Runnable onBackCallback) {
        this.mainApp = mainApp;
        this.plan = plan;
        this.onBackCallback = onBackCallback;
        this.mapGenerator = new StaticMapGenerator(TMAP_API_KEY);

        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loadFont();
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(new EmptyBorder(20, 30, 10, 30));
        headerPanel.setOpaque(false);

        JPanel titleAndBackPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleAndBackPanel.setOpaque(false);

        JButton backButton = new JButton("←");
        backButton.setFont(jalnanFont.deriveFont(Font.BOLD, 24f));
        backButton.setFocusPainted(false);
        backButton.setBorderPainted(false);
        backButton.setContentAreaFilled(false);
        backButton.addActionListener(e -> {
            if (this.onBackCallback != null) {
                this.onBackCallback.run();
            } else {
                mainApp.goToScheduleTab();
            }
        });

        JLabel titleLabel = new JLabel(plan.getPlan_title());
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
        titleLabel.setForeground(new Color(80, 40, 40));
        
        titleAndBackPanel.add(backButton);
        titleAndBackPanel.add(titleLabel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton checklistBtn = new JButton("체크리스트");
        checklistBtn.setPreferredSize(new Dimension(140, 40));
        checklistBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        checklistBtn.setBackground(ColorConstants.ACCENT);
        checklistBtn.setForeground(Color.WHITE);
        checklistBtn.setFocusPainted(false);
        checklistBtn.addActionListener(e -> mainApp.switchToChecklist(plan));
    
        JButton membersBtn = new JButton("멤버리스트");
        membersBtn.setPreferredSize(new Dimension(140, 40));
        membersBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        membersBtn.setBackground(ColorConstants.ACCENT);
        membersBtn.setForeground(Color.WHITE);
        membersBtn.setFocusPainted(false);
        membersBtn.addActionListener(e -> {
            List<Member> members = TravelPlanMemberDAO.findMembersByPlanId(plan.getPlan_id());
            MemberListDialog dialog = new MemberListDialog((JFrame) SwingUtilities.getWindowAncestor(this), members);
            dialog.setVisible(true);
        });

        buttonPanel.add(checklistBtn);
        buttonPanel.add(membersBtn);

        headerPanel.add(titleAndBackPanel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel scheduleListPanel = createScheduleListPanel();
        JScrollPane scrollPane = new JScrollPane(scheduleListPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(800, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());

        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setPreferredSize(new Dimension(550, 0));
        mapLabel = new JLabel("좌측 일정에서 '지도 보기'를 클릭하세요.", SwingConstants.CENTER);
        mapLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        mapPanel.add(mapLabel, BorderLayout.CENTER);

        routeInfoPanel = new JPanel();
        routeInfoPanel.setLayout(new BoxLayout(routeInfoPanel, BoxLayout.Y_AXIS));
        routeInfoPanel.setBackground(Color.WHITE);

        JScrollPane routeInfoScrollPane = new JScrollPane(routeInfoPanel);
        routeInfoScrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        routeInfoScrollPane.setBackground(Color.WHITE);
        routeInfoScrollPane.setPreferredSize(new Dimension(550, 300));
        routeInfoScrollPane.getViewport().setBackground(Color.WHITE);
        routeInfoScrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        routeInfoScrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        mapPanel.add(routeInfoScrollPane, BorderLayout.SOUTH);

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

            panel.add(createDaySchedulePanel(i + 1, date, daySchedules));
            panel.add(Box.createVerticalStrut(20));
        }

        return panel;
    }

    private JPanel createDaySchedulePanel(int dayIndex, LocalDate date, List<ScheduledPlace> places) {
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
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
        titleLabel.setForeground(new Color(50, 30, 30));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dayPanel.add(titleLabel);
        dayPanel.add(Box.createVerticalStrut(15));

        int order = 1;
        for (ScheduledPlace place : places) {
            JPanel placeCard = createPlaceCard(place, order++);
            placeCard.setAlignmentX(Component.LEFT_ALIGNMENT);
            dayPanel.add(placeCard);
            dayPanel.add(Box.createVerticalStrut(10));
        }

        JPanel buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.X_AXIS));
        buttonContainer.setOpaque(false);
        buttonContainer.setAlignmentX(Component.LEFT_ALIGNMENT);


        if (places.isEmpty()) {
            JButton addPlaceBtn = new JButton("＋ 장소 추가");
            addPlaceBtn.setPreferredSize(new Dimension(140, 40));
            addPlaceBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            addPlaceBtn.setBackground(ColorConstants.ACCENT);
            addPlaceBtn.setForeground(Color.WHITE);
            addPlaceBtn.setFocusPainted(false);
            addPlaceBtn.addActionListener(e -> mainApp.switchToPlanTrip(plan, dayIndex));
            buttonContainer.add(addPlaceBtn);
        } else {
            JButton editPlaceBtn = new JButton("장소 편집");
            editPlaceBtn.setPreferredSize(new Dimension(140, 40));
            editPlaceBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            editPlaceBtn.setBackground(ColorConstants.ACCENT);
            editPlaceBtn.setForeground(Color.WHITE);
            editPlaceBtn.setFocusPainted(false);
            editPlaceBtn.addActionListener(e -> mainApp.switchToPlanTrip(plan, dayIndex));

            JButton viewMapBtn = new JButton("지도 보기");
            viewMapBtn.setPreferredSize(new Dimension(140, 40));
            viewMapBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            viewMapBtn.setBackground(new Color(67, 119, 181));
            viewMapBtn.setForeground(Color.WHITE);
            viewMapBtn.setFocusPainted(false);
            viewMapBtn.addActionListener(e -> updateMap(places));

            JButton optimizeRouteBtn = new JButton("경로 최적화");
            optimizeRouteBtn.setPreferredSize(new Dimension(180, 40));
            optimizeRouteBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            Color originalColor = new Color(0, 153, 153);
            optimizeRouteBtn.setBackground(originalColor);
            optimizeRouteBtn.setForeground(Color.WHITE);
            optimizeRouteBtn.setFocusPainted(false);
            optimizeRouteBtn.addActionListener(e -> {
                if (places.size() < 3) {
                    JOptionPane.showMessageDialog(this, "경유지가 1개 이상 있어야 최적화할 수 있습니다.\n(총 장소 3개 이상)", "알림", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                optimizeRouteBtn.setEnabled(false);
                optimizeRouteBtn.setText("최적 경로 계산중...");
                optimizeRouteBtn.setBackground(Color.GRAY);

                new Thread(() -> {
                    try {
                        List<ScheduledPlace> optimizedSchedule = optimizeRoute(places, date);
                        if (optimizedSchedule == null) return;

                        TmapRouteResult originalRouteInfo = getRouteDetails(places);
                        TmapRouteResult optimizedRouteInfo = getRouteDetails(optimizedSchedule);

                        SwingUtilities.invokeLater(() -> {
                            if (originalRouteInfo != null && optimizedRouteInfo != null &&
                                originalRouteInfo.getSummary() != null && optimizedRouteInfo.getSummary() != null) {
                                int originalDistance = originalRouteInfo.getSummary().getTotalDistance();
                                int optimizedDistance = optimizedRouteInfo.getSummary().getTotalDistance();

                                if (optimizedDistance >= originalDistance) {
                                    JOptionPane.showMessageDialog(this, "현재 경로가 이미 최적의 경로입니다.", "경로 최적화", JOptionPane.INFORMATION_MESSAGE);
                                    return;
                                }
                            }

                            RouteComparisonDialog dialog = new RouteComparisonDialog(
                                    (Frame) SwingUtilities.getWindowAncestor(this),
                                    places,
                                    optimizedSchedule,
                                    originalRouteInfo,
                                    optimizedRouteInfo
                            );
                            dialog.setVisible(true);
                            
                            if (dialog.isRouteApplied()) {
                                 for (int i = 0; i < optimizedSchedule.size(); i++) {
                                    optimizedSchedule.get(i).setVisit_order(i + 1);
                                }
                                ScheduledPlaceService.updateDayScheduleOrder(plan.getPlan_id(), date, optimizedSchedule);
                                JOptionPane.showMessageDialog(this, "새로운 경로가 적용되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
                                mainApp.refreshFullSchedule(plan);
                            }
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "경로 최적화 중 오류가 발생했습니다: " + ex.getMessage(), "오류", JOptionPane.ERROR_MESSAGE));
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            optimizeRouteBtn.setEnabled(true);
                            optimizeRouteBtn.setText("경로 최적화");
                            optimizeRouteBtn.setBackground(originalColor);
                        });
                    }
                }).start();
            });

            buttonContainer.add(editPlaceBtn);
            buttonContainer.add(Box.createHorizontalStrut(10));
            buttonContainer.add(viewMapBtn);
            buttonContainer.add(Box.createHorizontalStrut(10));
            buttonContainer.add(optimizeRouteBtn);
        }

        if (plan.getPlan_period_end().toLocalDate().isBefore(LocalDate.now())) {
            JButton writeReviewBtn = new JButton("후기 작성하기");
            writeReviewBtn.setPreferredSize(new Dimension(180, 40));
            writeReviewBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            writeReviewBtn.setBackground(ColorConstants.ACCENT);
            writeReviewBtn.setForeground(Color.WHITE);
            writeReviewBtn.setFocusPainted(false);
            writeReviewBtn.addActionListener(e -> {
                List<ScheduledPlace> scheduledPlaces = ScheduledPlaceService.getSchedulesByPlanId(plan.getPlan_id());
                ReviewPopup popup = new ReviewPopup((JFrame) SwingUtilities.getWindowAncestor(this), null, () -> mainApp.refreshFullSchedule(plan), scheduledPlaces, plan.getPlan_id());
                popup.setVisible(true);
            });
            buttonContainer.add(Box.createHorizontalStrut(10));
            buttonContainer.add(writeReviewBtn);
        }

        dayPanel.add(Box.createVerticalStrut(5));
        dayPanel.add(buttonContainer);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.add(dayPanel, BorderLayout.NORTH);
        return wrapper;
    }

    private JPanel createPlaceCard(ScheduledPlace place, int order) {
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

        JLabel orderLabel = new JLabel(order + "");
        orderLabel.setPreferredSize(new Dimension(30, 56));
        orderLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
        orderLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel imageLabel = buildThumb(data.thumbUrl, 56, 56);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel nameLabel = new JLabel(data.name);
        nameLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));

        JLabel categoryLabel = new JLabel(place.getP_type());
        categoryLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(3));
        infoPanel.add(categoryLabel);

        card.add(orderLabel);
        card.add(Box.createHorizontalStrut(6));
        card.add(imageLabel);
        card.add(infoPanel);

        // rightPanel: 메모와 버튼 감싸는 전체 박스
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 14)); // 좌우15, 상하14 padding
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(300, 56));

        // 메모 라벨
        String memo = place.getSchedule_memo();
        if (memo == null || memo.isBlank()) memo = "메모 없음";
        String displayMemo = memo.length() > 20 ? memo.substring(0, 20) + "..." : memo;

        JLabel memoLabel = new JLabel(displayMemo);
        memoLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 13f));
        memoLabel.setForeground(Color.DARK_GRAY);
        memoLabel.setToolTipText(memo);
        memoLabel.setPreferredSize(new Dimension(160, 28)); // 고정폭
        memoLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // 메모 수정 버튼
        JButton editMemoBtn = new JButton("메모 수정");
        editMemoBtn.setFont(jalnanFont.deriveFont(Font.PLAIN, 13f));
        editMemoBtn.setBackground(new Color(255, 204, 204));
        editMemoBtn.setFocusPainted(false);
        editMemoBtn.setForeground(Color.BLACK);
        editMemoBtn.setPreferredSize(new Dimension(90, 28));

        editMemoBtn.addActionListener(e -> {
            String newMemo = JOptionPane.showInputDialog(this, "메모를 입력하세요", place.getSchedule_memo());
            if (newMemo != null) {
                place.setSchedule_memo(newMemo);
                ScheduledPlaceService.updateSchedule(place);
                mainApp.refreshFullSchedule(plan);
            }
        });

        // 오른쪽 패널 조립
        rightPanel.add(memoLabel);
        rightPanel.add(editMemoBtn);

        card.add(Box.createHorizontalGlue());
        card.add(rightPanel);


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

    private List<ScheduledPlace> optimizeRoute(List<ScheduledPlace> places, LocalDate date) throws Exception {
        PlaceDAO placeDAO = new PlaceDAO();
        List<PlaceDataWithCoords> placeDataList = new ArrayList<>();
        for (ScheduledPlace sp : places) {
            Object rawPlace = placeDAO.getPlaceByTypeAndId(sp.getP_type(), sp.getP_id());
            PlaceData data = PlaceData.from(rawPlace);
            if (data.name != null && !data.name.equals("알 수 없는 장소")) {
                String searchQuery = (data.addr != null && !data.addr.isBlank()) ? data.addr : data.name;
                PlaceInfo placeInfo = apiHandler.getPlaceDetailsByName(searchQuery, TMAP_API_KEY);
                if (placeInfo != null) {
                    placeDataList.add(new PlaceDataWithCoords(sp, data, new Coordinate(placeInfo.getLatitude(), placeInfo.getLongitude())));
                }
            }
        }

        if (placeDataList.size() < 3) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "장소의 좌표를 찾을 수 없어 최적화에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE));
            return null;
        }

        RoutePoint startPoint = new RoutePoint(
            placeDataList.get(0).scheduledPlace.getP_id(),
            placeDataList.get(0).placeData.name,
            placeDataList.get(0).coordinate
        );
        RoutePoint endPoint = new RoutePoint(
            placeDataList.get(placeDataList.size() - 1).scheduledPlace.getP_id(),
            placeDataList.get(placeDataList.size() - 1).placeData.name,
            placeDataList.get(placeDataList.size() - 1).coordinate
        );
        List<RoutePoint> viaPoints = placeDataList.subList(1, placeDataList.size() - 1).stream()
                    .map(p -> new RoutePoint(p.scheduledPlace.getP_id(), p.placeData.name, p.coordinate))
                    .collect(Collectors.toList());

        List<String> optimizedViaPointIds = tmapApiHandler.optimizeRoute(startPoint, endPoint, viaPoints);

        if (optimizedViaPointIds == null) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, "API 호출 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE));
            return null;
        }

        List<ScheduledPlace> newSchedule = new ArrayList<>();
        newSchedule.add(placeDataList.get(0).scheduledPlace);

        Map<String, ScheduledPlace> viaMap = placeDataList.stream()
            .skip(1)
            .limit(placeDataList.size() - 2)
            .collect(Collectors.toMap(p -> p.scheduledPlace.getP_id(), p -> p.scheduledPlace, (existing, replacement) -> existing));

        for (String viaId : optimizedViaPointIds) {
            ScheduledPlace place = viaMap.get(viaId);
            if (place != null && !newSchedule.contains(place)) {
                newSchedule.add(place);
            }
        }
        newSchedule.add(placeDataList.get(placeDataList.size() - 1).scheduledPlace);
        
        return newSchedule;
    }
    
    private TmapRouteResult getRouteDetails(List<ScheduledPlace> places) {
        List<Coordinate> coordinates = new ArrayList<>();
        List<String> placeNames = new ArrayList<>();
        PlaceDAO placeDAO = new PlaceDAO();

        for (ScheduledPlace sp : places) {
            Object rawPlace = placeDAO.getPlaceByTypeAndId(sp.getP_type(), sp.getP_id());
            PlaceData data = PlaceData.from(rawPlace);
            if (data.name != null && !data.name.equals("알 수 없는 장소")) {
                String searchQuery = (data.addr != null && !data.addr.isBlank()) ? data.addr : data.name;
                PlaceInfo placeInfo = apiHandler.getPlaceDetailsByName(searchQuery, TMAP_API_KEY);
                if (placeInfo != null) {
                    coordinates.add(new Coordinate(placeInfo.getLatitude(), placeInfo.getLongitude()));
                    placeNames.add(data.name);
                }
            }
        }

        if (coordinates.size() < 2) return null;

        int totalTime = 0;
        int totalDistance = 0;
        int totalTaxiFare = 0;

        for (int i = 0; i < coordinates.size() - 1; i++) {
            TmapRouteResult segmentResult = tmapApiHandler.getRouteInfo(TMAP_API_KEY,
                    coordinates.get(i).getLongitude(), coordinates.get(i).getLatitude(),
                    coordinates.get(i + 1).getLongitude(), coordinates.get(i + 1).getLatitude(),
                    placeNames.get(i), placeNames.get(i + 1));
            if (segmentResult != null && segmentResult.getSummary() != null) {
                TmapRouteInfo info = segmentResult.getSummary();
                totalTime += info.getTotalTime();
                totalDistance += info.getTotalDistance();
                totalTaxiFare += info.getTaxiFare();
            }
        }
        return new TmapRouteResult(new TmapRouteInfo(totalDistance, totalTime, 0, totalTaxiFare), coordinates);
    }

    private static class PlaceDataWithCoords {
        final ScheduledPlace scheduledPlace;
        final PlaceData placeData;
        final Coordinate coordinate;

        PlaceDataWithCoords(ScheduledPlace sp, PlaceData pd, Coordinate c) {
            this.scheduledPlace = sp;
            this.placeData = pd;
            this.coordinate = c;
        }
    }

    private void updateMap(List<ScheduledPlace> places) {
        if (places == null || places.isEmpty()) {
            mapLabel.setIcon(null);
            mapLabel.setText("선택한 날짜에 예정된 장소가 없습니다.");
            routeInfoPanel.removeAll();
            routeInfoPanel.revalidate();
            routeInfoPanel.repaint();
            return;
        }

        mapLabel.setText("지도 생성 중...");
        mapLabel.setIcon(null);
        routeInfoPanel.removeAll();

        new Thread(() -> {
            List<Coordinate> coordinates = new ArrayList<>();
            List<String> placeNames = new ArrayList<>();
            PlaceDAO placeDAO = new PlaceDAO();

            for (ScheduledPlace sp : places) {
                Object rawPlace = placeDAO.getPlaceByTypeAndId(sp.getP_type(), sp.getP_id());
                PlaceData data = PlaceData.from(rawPlace);
                if (data.name != null && !data.name.equals("알 수 없는 장소")) {
                    String searchQuery = (data.addr != null && !data.addr.isBlank()) ? data.addr : data.name;
                    PlaceInfo placeInfo = apiHandler.getPlaceDetailsByName(searchQuery, TMAP_API_KEY);
                    if (placeInfo != null) {
                        coordinates.add(new Coordinate(placeInfo.getLatitude(), placeInfo.getLongitude()));
                        placeNames.add(data.name);
                    }
                }
            }

            if (coordinates.isEmpty()) {
                SwingUtilities.invokeLater(() -> mapLabel.setText("장소의 좌표를 찾을 수 없습니다."));
                return;
            }

            String mapUrl = null;
            if (coordinates.size() >= 2) {
                mapUrl = mapGenerator.generateRouteMapUrl(coordinates, mapLabel.getWidth(), mapLabel.getHeight(), "red");
                
                List<TmapRouteResult> routeResults = new ArrayList<>();
                for (int i = 0; i < coordinates.size() - 1; i++) {
                    TmapRouteResult segmentResult = tmapApiHandler.getRouteInfo(TMAP_API_KEY,
                            coordinates.get(i).getLongitude(), coordinates.get(i).getLatitude(),
                            coordinates.get(i + 1).getLongitude(), coordinates.get(i + 1).getLatitude(),
                            placeNames.get(i), placeNames.get(i + 1));
                    routeResults.add(segmentResult);
                }
                SwingUtilities.invokeLater(() -> updateRouteInfoPanel(routeResults, placeNames));

            } else if (coordinates.size() == 1) {
                mapUrl = mapGenerator.generateMapWithPinUrl(coordinates.get(0), mapLabel.getWidth(), mapLabel.getHeight(), 15);
                 SwingUtilities.invokeLater(() -> {
                    routeInfoPanel.removeAll();
                    routeInfoPanel.revalidate();
                    routeInfoPanel.repaint();
                });
            }

            if (mapUrl != null) {
                try {
                    ImageIcon icon = new ImageIcon(new URL(mapUrl));
                    SwingUtilities.invokeLater(() -> {
                        mapLabel.setText("");
                        mapLabel.setIcon(icon);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> mapLabel.setText("지도 이미지를 불러올 수 없습니다."));
                    e.printStackTrace();
                }
            } else {
                SwingUtilities.invokeLater(() -> mapLabel.setText("지도 URL을 생성할 수 없습니다."));
            }
        }).start();
    }

    private void updateRouteInfoPanel(List<TmapRouteResult> results, List<String> placeNames) {
        routeInfoPanel.removeAll();
        int totalTime = 0;
        int totalDistance = 0;

        for (int i = 0; i < results.size(); i++) {
            TmapRouteResult result = results.get(i);
            
            JLabel segmentTitle = new JLabel(String.format("<html><b>%s &rarr; %s</b></html>", placeNames.get(i), placeNames.get(i+1)));
            segmentTitle.setFont(jalnanFont.deriveFont(Font.PLAIN, 15f));
            routeInfoPanel.add(segmentTitle);

            if (result != null && result.getSummary() != null) {
                TmapRouteInfo info = result.getSummary();
                int minutes = info.getTotalTime() / 60;
                double distance = info.getTotalDistance() / 1000.0;
                totalTime += info.getTotalTime();
                totalDistance += info.getTotalDistance();

                JLabel timeLabel = new JLabel(String.format("  소요 시간: %d분", minutes));
                JLabel distanceLabel = new JLabel(String.format("  거리: %.1fkm", distance));
                JLabel taxiFareLabel = new JLabel(String.format("  예상 택시 요금: %,d원", info.getTaxiFare()));
                
                timeLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
                distanceLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
                taxiFareLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));

                routeInfoPanel.add(timeLabel);
                routeInfoPanel.add(distanceLabel);
                routeInfoPanel.add(taxiFareLabel);

            } else {
                JLabel errorLabel = new JLabel("  경로 정보를 가져올 수 없습니다.");
                errorLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
                routeInfoPanel.add(errorLabel);
            }
            routeInfoPanel.add(Box.createVerticalStrut(10));
        }

        if (results.size() > 0) {
            JSeparator separator = new JSeparator();
            routeInfoPanel.add(separator);
            routeInfoPanel.add(Box.createVerticalStrut(10));

            int totalMinutes = totalTime / 60;
            double totalKm = totalDistance / 1000.0;

            JLabel totalTimeLabel = new JLabel(String.format("총 이동 시간: %d분", totalMinutes));
            totalTimeLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            routeInfoPanel.add(totalTimeLabel);

            JLabel totalDistanceLabel = new JLabel(String.format("총 이동 거리: %.1fkm", totalKm));
            totalDistanceLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
            routeInfoPanel.add(totalDistanceLabel);
        }

        routeInfoPanel.revalidate();
        routeInfoPanel.repaint();
    }
    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/Jalnan2TTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(14f);
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
