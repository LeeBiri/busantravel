package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;
import busantravel.service.ScheduledPlaceService;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 사용자가 여행 일정을 계획하는 주 UI 패널입니다.
 * 이 패널은 추천 장소 목록, 선택된 장소 목록, 그리고 지도를 표시하는 세 개의 열로 구성됩니다.
 * 사용자는 날짜를 선택하고, 추천 장소를 자신의 일정에 추가, 삭제, 교체할 수 있습니다.
 */
public class PlanTripPanel extends JPanel {

    private final TravelPlannerApp mainApp;
    private final String tripTitle;
    private final String planId;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final Runnable onBack;

    private JComboBox<String> dayComboBox;
    private JPanel placeListPanel;
    private JPanel selectedPlacePanel;
    private JPanel categoryBar;
    private JButton resetButton;

    private final PlaceDAO placeDAO = new PlaceDAO();
    private List<Object> currentCategoryPlaces = new ArrayList<>();
    private final List<ScheduledPlace> scheduledPlaces;
    private final Map<String, String> placeNameIndex = new HashMap<>();
    private final Map<String, String> placeThumbIndex = new HashMap<>();

    private String selectedCategory = "저장"; // 현재 선택된 카테고리

    private static final Color ACCENT_DARK = new Color(200,110,90);
    private static final Color PANEL_BORDER = new Color(228, 180, 160);
    private static final Color DIVIDER = new Color(235, 224, 218);

    private static final int COLUMN_LEFT_WIDTH = 420;
    private static final int COLUMN_CENTER_WIDTH = 440;
    private static final int ROW_HEIGHT = 78;
    private static final int THUMB_W = 68;
    private static final int THUMB_H = 54;
    private static final int ROW_INNER_HPAD = 10;

    /**
     * PlanTripPanel의 새 인스턴스를 생성합니다.
     *
     * @param startDate 여행 시작 날짜와 시간
     * @param endDate   여행 종료 날짜와 시간
     * @param mainApp   메인 애플리케이션(TravelPlannerApp)의 참조
     * @param tripTitle 여행 계획의 제목
     * @param planId    여행 계획 ID
     * @param onBack    '뒤로가기' 버튼 클릭 시 실행될 콜백
     */
    public PlanTripPanel(LocalDateTime startDate, LocalDateTime endDate,
                         TravelPlannerApp mainApp, String tripTitle, String planId, Runnable onBack) {
        this.startDate = startDate.toLocalDate();
        this.endDate = endDate.toLocalDate();
        this.mainApp = mainApp;
        this.tripTitle = tripTitle;
        this.planId = planId;
        this.onBack = onBack;
        this.scheduledPlaces = ScheduledPlaceService.getSchedulesByPlanId(planId);
        System.out.println("[DEBUG] planId: " + planId);

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(252, 248, 244));

        buildTopBar();
        buildBody();

        loadPlacesFor(selectedCategory);
        refreshSelectedList();
    }

    /**
     * 현재 여행 계획의 제목을 반환합니다.
     * @return 여행 제목 문자열
     */
    public String getTripTitle() { return tripTitle; }
    public String getPlanId() { return planId; }

    /* ── Top Bar ─────────────────────────────────────────────── */
    /**
     * 패널의 상단 바(날짜 선택 콤보박스, 뒤로가기 버튼)를 구성합니다.
     */
    private void buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(245, 242, 238));
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        // 왼쪽: 제목 + 날짜 선택
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel(tripTitle);             // ✅ 여행 제목 표시
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        left.add(titleLabel);

        JLabel dateLabel = new JLabel("날짜 선택:");
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        left.add(dateLabel);

        DefaultComboBoxModel<String> daysModel = new DefaultComboBoxModel<>();
        LocalDate d = startDate; int idx = 1;
        while (!d.isAfter(endDate)) {
            daysModel.addElement(String.format("%d일차 (%d월 %d일 %s)",
                    idx, d.getMonthValue(), d.getDayOfMonth(),
                    d.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN)));
            d = d.plusDays(1); idx++;
        }
        dayComboBox = new JComboBox<>(daysModel);
        styleCombo(dayComboBox);
        dayComboBox.addActionListener(e -> refreshSelectedList());
        left.add(dayComboBox);
        top.add(left, BorderLayout.WEST);

        // 오른쪽: 뒤로 가기 + 저장
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton saveBtn = new JButton("저장");
        styleSolidButton(saveBtn);
        saveBtn.addActionListener(e -> {
        if (mainApp != null) {
            ScheduledPlaceService.insertSchedule(scheduledPlaces);
            if (onBack != null) onBack.run();
        }});

        JButton back = new JButton("뒤로 가기");
        styleGhostButton(back);
        back.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
        right.add(saveBtn);
        right.add(back);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    /**
     * 패널의 본문 영역을 구성합니다.
     * 추천 장소, 나의 여행 계획, 지도 영역으로 나누어진 3단 레이아웃을 생성합니다.
     */
    private void buildBody() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(6, 12, 16, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.weighty = 1;

        // ─ Left: 추천 장소 ─
        JPanel left = boxedColumn(null);
        left.setPreferredSize(new Dimension(COLUMN_LEFT_WIDTH, 0));

        // 제목 + 카테고리 버튼을 하나의 헤더에 넣어서 NORTH에 단 한 번만 추가 ✅
        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JLabel leftTitle = new JLabel("추천 장소");
        leftTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        leftTitle.setForeground(new Color(100, 80, 70));
        leftTitle.setAlignmentX(LEFT_ALIGNMENT);
        leftTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        leftHeader.add(leftTitle);

        categoryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        categoryBar.setOpaque(false);
        for (String c : new String[]{"저장","관광지","맛집","숙소"}) {
            JButton b = new JButton(c);
            styleCategoryChip(b, c.equals(selectedCategory));
            b.addActionListener(e -> { selectedCategory = c; updateCategoryBarStyles(); loadPlacesFor(selectedCategory);});
            categoryBar.add(b);
        }
        leftHeader.add(categoryBar);
        left.add(leftHeader, BorderLayout.NORTH);

        placeListPanel = new JPanel();
        placeListPanel.setLayout(new BoxLayout(placeListPanel, BoxLayout.Y_AXIS));
        placeListPanel.setOpaque(false);

        JScrollPane leftScroll = new JScrollPane(placeListPanel);
        styleScroll(leftScroll);
        leftScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        left.add(leftScroll, BorderLayout.CENTER);

        gbc.gridx = 0; gbc.weightx = 0.30;
        grid.add(left, gbc);

        // ─ Center: 나의 여행 계획 ─
        JPanel center = boxedColumn(null);
        center.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH, 0));

        JPanel centerHeader = new JPanel(new BorderLayout());
        centerHeader.setOpaque(false);
        JLabel centerTitle = new JLabel("나의 여행 계획");
        centerTitle.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        centerTitle.setForeground(new Color(100, 80, 70));
        centerTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        centerHeader.add(centerTitle, BorderLayout.WEST);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        resetButton = new JButton("초기화");
        styleSolidButton(resetButton);
        resetButton.addActionListener(e -> {
            LocalDate sel = getSelectedDate();
            List<ScheduledPlace> toRemove = scheduledPlaces.stream()
                .filter(sp -> sp.getScheduled_date().toLocalDate().equals(sel))
                .collect(Collectors.toList());

            for (ScheduledPlace sp : toRemove) {
                ScheduledPlaceService.deleteSchedule(sp);
            }

            scheduledPlaces.removeAll(toRemove);
            refreshSelectedList();
        });

        topBar.add(resetButton);
        centerHeader.add(topBar, BorderLayout.EAST);

        center.add(centerHeader, BorderLayout.NORTH);

        selectedPlacePanel = new JPanel();
        selectedPlacePanel.setLayout(new BoxLayout(selectedPlacePanel, BoxLayout.Y_AXIS));
        selectedPlacePanel.setOpaque(false);

        JScrollPane centerScroll = new JScrollPane(selectedPlacePanel);
        styleScroll(centerScroll);
        centerScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        center.add(centerScroll, BorderLayout.CENTER);

        gbc.gridx = 1; gbc.weightx = 0.34;
        grid.add(center, gbc);

        // ─ Right: 지도 placeholder ─
        JPanel right = boxedColumn(null);
        right.setPreferredSize(new Dimension(520, 0));
        JLabel mapPh = new JLabel("지도", SwingConstants.CENTER);
        mapPh.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        mapPh.setForeground(new Color(150, 160, 150));
        right.add(mapPh, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.weightx = 0.36;
        grid.add(right, gbc);

        add(grid, BorderLayout.CENTER);
    }

    /**
     * 제목과 테두리가 있는 박스 형태의 컬럼 패널을 생성합니다.
     *
     * @param title 컬럼 상단에 표시될 제목 (null일 경우 생략)
     * @return 스타일이 적용된 JPanel 객체
     */
    private JPanel boxedColumn(String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(255, 251, 248));
        p.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(PANEL_BORDER, 2, true),
                BorderFactory.createEmptyBorder(10, 12, 12, 12)
        ));
        if (title != null) {
            JLabel t = new JLabel(title);
            t.setFont(new Font("맑은 고딕", Font.BOLD, 20));
            t.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
            t.setForeground(new Color(100, 80, 70));
            p.add(t, BorderLayout.PAGE_START);
        }
        return p;
    }

    /* ── Load places ─────────────────────────────────── */
    /**
     * 지정된 카테고리에 해당하는 장소 목록을 로드하여 좌측 추천 장소 패널에 표시합니다.
     *
     * @param category 로드할 장소의 카테고리
     */
    private void loadPlacesFor(String category) {
        placeListPanel.removeAll();

        if ("저장".equals(category)) {
            List<Object> merged = new ArrayList<>();
            merged.addAll(placeDAO.getPlacesByCategory("관광지"));
            merged.addAll(placeDAO.getPlacesByCategory("맛집"));
            merged.addAll(placeDAO.getPlacesByCategory("숙소"));
            merged.addAll(placeDAO.getPlacesByCategory("축제"));
            currentCategoryPlaces = merged;
        } else {
            currentCategoryPlaces = placeDAO.getPlacesByCategory(category);
        }

        for (Object obj : currentCategoryPlaces) {
            placeListPanel.add(buildListRow(obj));
        }
        placeListPanel.revalidate();
        placeListPanel.repaint();
    }

    /**
     * 좌측 추천 장소 목록에 표시될 개별 행(Row) UI를 생성합니다.
     * 각 행은 썸네일, 장소 이름, 주소, 추가 버튼으로 구성됩니다.
     *
     * @param obj 장소 정보를 담고 있는 데이터 객체 (Tourist, Restaurant 등)
     * @return 생성된 행 JPanel 객체
     */
    private JPanel buildListRow(Object obj) {
        PlaceData pd = PlaceData.from(obj);
        placeNameIndex.put(pd.type + ":" + pd.id, pd.name);
        placeThumbIndex.put(pd.type + ":" + pd.id, pd.thumbUrl);

        int fixedWidth = COLUMN_LEFT_WIDTH - 2*ROW_INNER_HPAD - 6;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
        row.setPreferredSize(new Dimension(COLUMN_LEFT_WIDTH - 32, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(COLUMN_LEFT_WIDTH - 32, ROW_HEIGHT));

        JLabel thumb = buildThumb(pd.thumbUrl, THUMB_W, THUMB_H);
        row.add(thumb, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JLabel title = ellipsisLabel("<html><b>"+safe(pd.name)+"</b> <span style='color:#9a9a9a'>("+pd.type+")</span></html>", fixedWidth);
        JLabel addr  = ellipsisLabel(safe(pd.addr), fixedWidth);
        addr.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        addr.setForeground(new Color(140,140,140));
        text.add(title);
        text.add(addr);
        row.add(text, BorderLayout.CENTER);

        JButton plus = new JButton("+");
        plus.setPreferredSize(new Dimension(36, 36));
        plus.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        plus.setForeground(Color.WHITE);
        plus.setBackground(ColorConstants.ACCENT);
        plus.setFocusPainted(false);
        plus.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        plus.addActionListener(e -> {
            LocalDate d = getSelectedDate();
            LocalDateTime visit = LocalDateTime.of(d, LocalTime.NOON);
            ScheduledPlace sp = new ScheduledPlace(
                    planId,
                    pd.id, pd.type,
                    visit, visit.plusHours(2),
                    nextOrder(d)
            );
            scheduledPlaces.add(sp);
            refreshSelectedList();
        });
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, (ROW_HEIGHT-36)/2));
        btnWrap.setOpaque(false);
        btnWrap.add(plus);
        row.add(btnWrap, BorderLayout.EAST);
        return row;
    }

    /**
     * 가운데 '나의 여행 계획' 목록에 표시될 개별 행(Row) UI를 생성합니다.
     * 각 행은 썸네일, 장소 이름, 삭제/교체 버튼으로 구성됩니다.
     *
     * @param sp 계획된 장소 정보를 담고 있는 ScheduledPlace 객체
     * @return 생성된 행 JPanel 객체
     */
    private JPanel buildCenterRow(ScheduledPlace sp) {
        String key = sp.getP_type() + ":" + sp.getP_id();
        String name = placeNameIndex.getOrDefault(key, "[" + sp.getP_id() + "]");
        String thumbUrl = placeThumbIndex.get(key);

        int fixedWidth = COLUMN_CENTER_WIDTH - 2*ROW_INNER_HPAD - 120;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
        row.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));

        JLabel thumb = buildThumb(thumbUrl, THUMB_W, THUMB_H);
        row.add(thumb, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JLabel title = ellipsisLabel("<html><b>"+safe(name)+"</b> <span style='color:#9a9a9a'>("+sp.getP_type()+")</span></html>", fixedWidth);
        JLabel sub   = ellipsisLabel("", fixedWidth);
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sub.setForeground(new Color(140,140,140));
        text.add(title);
        text.add(sub);
        row.add(text, BorderLayout.CENTER);

        JButton del = new JButton("삭제");
        styleDangerButton(del);
        del.addActionListener(e -> {
            ScheduledPlaceService.deleteSchedule(sp);
            scheduledPlaces.remove(sp); refreshSelectedList();
            });

        JButton swap = new JButton("교체");
        stylePrimaryButton(swap);
        swap.addActionListener(e -> {
            if (currentCategoryPlaces.isEmpty()) {
                JOptionPane.showMessageDialog(this, "교체할 후보가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String[] opts = currentCategoryPlaces.stream().map(o -> PlaceData.from(o).name).toArray(String[]::new);
            String sel = (String) JOptionPane.showInputDialog(this, "교체할 장소를 선택하세요.", "장소 교체",
                    JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]);
            if (sel != null) {
                PlaceData chosen = currentCategoryPlaces.stream()
                        .map(PlaceData::from)
                        .filter(p -> p.name.equals(sel)).findFirst().orElse(null);
                if (chosen != null) {
                    sp.setP_id(chosen.id);
                    sp.setP_type(chosen.type);
                    placeNameIndex.put(chosen.type + ":" + chosen.id, chosen.name);
                    placeThumbIndex.put(chosen.type + ":" + chosen.id, chosen.thumbUrl);
                    refreshSelectedList();
                }
            }
        });

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, (ROW_HEIGHT-28)/2));
        btns.setOpaque(false);
        btns.add(del);
        btns.add(swap);
        row.add(btns, BorderLayout.EAST);
        return row;
    }

    /**
     * '나의 여행 계획' 패널(가운데 열)을 새로고침합니다.
     * 현재 선택된 날짜에 해당하는 계획된 장소들만 필터링하여 표시합니다.
     */
    private void refreshSelectedList() {
        selectedPlacePanel.removeAll();
        LocalDate sel = getSelectedDate();
        List<ScheduledPlace> todays = scheduledPlaces.stream()
                .filter(sp -> sp.getScheduled_date().toLocalDate().equals(sel))
                .sorted(Comparator.comparingInt(ScheduledPlace::getVisit_order))
                .collect(Collectors.toList());
        for (ScheduledPlace sp : todays) {
            selectedPlacePanel.add(buildCenterRow(sp));
        }
        selectedPlacePanel.revalidate();
        selectedPlacePanel.repaint();
    }

    /* ── Async + Cache image loader ───────────────────────── */
    /**
     * 지정된 URL의 이미지를 로드하여 썸네일 JLabel을 생성합니다.
     * 이미지 로딩에 실패하거나 URL이 유효하지 않을 경우, "No Img" 플레이스홀더를 표시합니다.
     *
     * @param url 썸네일 이미지의 URL
     * @param w   썸네일 너비
     * @param h   썸네일 높이
     * @return 생성된 썸네일 JLabel 객체
     */
    private JLabel buildThumb(String url, int w, int h) {
        JLabel lbl = noImg(w, h);
        if (url == null || url.isBlank()) return lbl;

        ImageCache cache = ImageCache.getInstance();
        ImageIcon cached = cache.getImage(url);
        if (cached != null) {
            lbl.setText("");
            lbl.setIcon(scaleIcon(cached, w, h));
            return lbl;
        }

        new Thread(() -> {
            try {
                ImageIcon icon = new ImageIcon(new URL(url));
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                cache.putImage(url, scaled);
                SwingUtilities.invokeLater(() -> {
                    lbl.setText("");
                    lbl.setIcon(new ImageIcon(scaled));
                });
            } catch (Exception ignored) {}
        }).start();

        return lbl;
    }

    private ImageIcon scaleIcon(ImageIcon icon, int w, int h) {
        return new ImageIcon(icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    /**
     * 이미지가 없을 때 표시할 플레이스홀더 JLabel을 생성합니다.
     *
     * @param w 플레이스홀더 너비
     * @param h 플레이스홀더 높이
     * @return "No Img" 텍스트가 포함된 JLabel 객체
     */
    private JLabel noImg(int w, int h) {
        JLabel n = new JLabel("No Img", SwingConstants.CENTER);
        n.setPreferredSize(new Dimension(w, h));
        n.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        n.setForeground(new Color(150, 150, 150));
        n.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        return n;
    }

    /* ── UI Style helpers ───────────────────────────────── */
    /**
     * 내용이 너무 길 경우 말줄임표(...)로 처리되는 JLabel을 생성합니다.
     *
     * @param htmlOrText  JLabel 내부에 표시될 HTML 또는 일반 텍스트
     * @param maxCssWidth 최대 너비 (CSS 픽셀 단위)
     * @return 생성된 JLabel 객체
     */
    private JLabel ellipsisLabel(String htmlOrText, int maxCssWidth) {
        String body;
        if (htmlOrText != null && htmlOrText.toLowerCase().startsWith("<html>")) {
            body = htmlOrText.substring(6, htmlOrText.length() - 7);
        } else {
            body = safe(htmlOrText);
        }
        String html = String.format(
                "<html><div style='width:%dpx; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;'>%s</div></html>",
                Math.max(50, maxCssWidth), body);
        JLabel l = new JLabel(html);
        l.setToolTipText(stripTags(body));
        return l;
    }

    /**
     * HTML 태그를 제거하는 유틸리티 메서드입니다.
     * @param s 원본 HTML 문자열
     * @return 태그가 제거된 문자열
     */
    private String stripTags(String s) { return s.replaceAll("\\<.*?\\>", ""); }

    /**
     * JComboBox에 일관된 스타일을 적용합니다.
     * @param cb 스타일을 적용할 JComboBox 객체
     */
    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(new Color(220, 210, 200), 1, true));
    }

    /**
     * JScrollPane에 일관된 스타일을 적용합니다.
     * @param sp 스타일을 적용할 JScrollPane 객체
     */
    private void styleScroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(PANEL_BORDER, 1, true));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
    }

    /**
     * 카테고리 선택 버튼(칩)에 스타일을 적용합니다.
     * @param b        스타일을 적용할 JButton 객체
     * @param selected 해당 버튼이 현재 선택되었는지 여부
     */
    private void styleCategoryChip(JButton b, boolean selected) {
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(selected ? ACCENT_DARK : PANEL_BORDER, 1, true));
        b.setBackground(selected ? ColorConstants.ACCENT : Color.WHITE);
        b.setForeground(selected ? Color.WHITE : new Color(90, 80, 70));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(64, 30));
    }

    /**
     * 카테고리 바의 모든 버튼 스타일을 현재 선택된 카테고리에 맞춰 업데이트합니다.
     */
    private void updateCategoryBarStyles() {
        for (Component c : categoryBar.getComponents()) {
            if (c instanceof JButton b) {
                styleCategoryChip(b, b.getText().equals(selectedCategory));
            }
        }
    }

    /**
     * 단색 배경의 기본 버튼 스타일을 적용합니다.
     * @param b 스타일을 적용할 JButton 객체
     */
    private void styleSolidButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(ColorConstants.ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(72, 32));
    }
    /**
     * 파란색 계열의 주요 액션 버튼 스타일을 적용합니다.
     * @param b 스타일을 적용할 JButton 객체
     */
    private void stylePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(100, 140, 200));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(80, 120, 180), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(56, 28));
    }
    /**
     * 붉은색 계열의 위험/삭제 버튼 스타일을 적용합니다.
     * @param b 스타일을 적용할 JButton 객체
     */
    private void styleDangerButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(230, 110, 110));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(210, 90, 90), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(56, 28));
    }
    /**
     * 배경이 거의 없고 테두리만 있는 '고스트' 버튼 스타일을 적용합니다.
     * @param b 스타일을 적용할 JButton 객체
     */
    private void styleGhostButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(90, 80, 70));
        b.setBorder(new LineBorder(new Color(210, 200, 190), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(96, 34));
    }

    /**
     * 날짜 선택 콤보박스에서 현재 선택된 날짜를 가져옵니다.
     * @return 선택된 날짜의 LocalDate 객체
     */
    private LocalDate getSelectedDate() {
        int idx = Math.max(0, dayComboBox.getSelectedIndex());
        return startDate.plusDays(idx);
    }

    /**
     * 특정 날짜에 새로 추가될 장소의 순서 번호를 계산합니다.
     * @param d 순서를 계산할 날짜
     * @return 해당 날짜의 다음 순서 번호
     */
    private int nextOrder(LocalDate d) {
        return (int) scheduledPlaces.stream()
                .filter(sp -> sp.getScheduled_date().toLocalDate().equals(d))
                .count() + 1;
    }

    /**
     * null 문자열을 빈 문자열로 안전하게 변환합니다.
     * @param s 변환할 문자열
     * @return 원본 문자열 또는 빈 문자열
     */
    private static String safe(String s) { return s == null ? "" : s; }

    /* ImageCache (내부) */
    private static class ImageCache {
        private static ImageCache instance;
        private final Map<String, ImageIcon> memoryCache = new HashMap<>();
        private final Path cacheDir = Paths.get("cache");

        private ImageCache() {
            try { if (!Files.exists(cacheDir)) Files.createDirectories(cacheDir); }
            catch (IOException e) { e.printStackTrace(); }
        }
        public static synchronized ImageCache getInstance() {
            if (instance == null) instance = new ImageCache();
            return instance;
        }
        public ImageIcon getImage(String key) {
            if (memoryCache.containsKey(key)) return memoryCache.get(key);
            File imageFile = cacheDir.resolve(getFileName(key)).toFile();
            if (imageFile.exists()) {
                try {
                    Image image = ImageIO.read(imageFile);
                    if (image != null) {
                        ImageIcon icon = new ImageIcon(image);
                        memoryCache.put(key, icon);
                        return icon;
                    }
                } catch (IOException ignored) {}
            }
            return null;
        }
        public void putImage(String key, Image image) {
            memoryCache.put(key, new ImageIcon(image));
            File imageFile = cacheDir.resolve(getFileName(key)).toFile();
            try { ImageIO.write(toBufferedImage(image), "png", imageFile); }
            catch (IOException e) { e.printStackTrace(); }
        }
        private String getFileName(String key) {
            try {
                java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
                byte[] hash = digest.digest(key.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                StringBuilder hexString = new StringBuilder();
                for (byte b : hash) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
                }
                return hexString.toString() + ".png";
            } catch (java.security.NoSuchAlgorithmException e) {
                // SHA-256을 사용할 수 없는 경우 이전 방식으로 대체
                return key.replaceAll("[^a-zA-Z0-9.-]", "_") + ".png";
            }
        }
        private static BufferedImage toBufferedImage(Image img) {
            if (img instanceof BufferedImage) return (BufferedImage) img;
            BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bimage.createGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
            return bimage;
        }
    }

    /* PlaceData */
    /**
     * 다양한 장소 모델 객체(Tourist, Restaurant 등)의 공통 데이터를 추출하여 담는
     * 내부 정적 DTO 클래스입니다.
     */
    private static class PlaceData {
        final String id, name, addr, type, thumbUrl;
        private PlaceData(String id, String name, String addr, String type, String thumbUrl) {
            this.id=id; this.name=name; this.addr=addr; this.type=type; this.thumbUrl=thumbUrl;
        }
        /**
         * 다양한 타입의 장소 객체로부터 PlaceData 인스턴스를 생성하는 정적 팩토리 메서드입니다.
         * @param o 원본 장소 객체 (Tourist, Restaurant 등)
         * @return 생성된 PlaceData 객체
         */
        static PlaceData from(Object o) {
            if (o instanceof Tourist t)   return new PlaceData(t.getT_id(), t.getT_place(), t.getT_addr(), "관광지", t.getT_thum());
            if (o instanceof Restaurant r) return new PlaceData(r.getR_id(), r.getR_place(), r.getR_addr(), "맛집", r.getR_thum());
            if (o instanceof Lodging l)    return new PlaceData(l.getL_id(), l.getL_place(), l.getL_addr(), "숙소", null);
            if (o instanceof Festival f)    return new PlaceData(f.getF_id(), f.getF_name(), f.getF_addr(), "축제", f.getF_thum());
            return new PlaceData("", "알 수 없는 장소", "", "기타", null);
        }
    }
}
