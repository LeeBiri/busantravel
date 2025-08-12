/* PlanTripPanel.java
 * 여행 계획 패널
 * 사용자가 선택한 장소를 관리할 수 있는, 새 창으로 표시되는 UI
 * 
 * 
 * 
 */




package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class PlanTripPanel extends JPanel {

    private final TravelPlannerApp mainApp;
    private final String tripTitle;
    private final LocalDate startDate;
    private final LocalDate endDate;

    private JComboBox<String> dayComboBox;
    private JPanel placeListPanel;       // 좌측
    private JPanel selectedPlacePanel;   // 가운데
    private JPanel categoryBar;
    private JButton resetButton;

    private final PlaceDAO placeDAO = new PlaceDAO();
    private List<Object> currentCategoryPlaces = new ArrayList<>();
    private final List<ScheduledPlace> scheduledPlaces = new ArrayList<>();

    // 이름/썸네일/주소 등 메타 캐시 (key = type:id)
    private final Map<String, PlaceData> placeMetaIndex = new HashMap<>();

    private String selectedCategory = "저장";

    // 스타일 상수
    private static final Color ACCENT = new Color(222,146,124);
    private static final Color ACCENT_DARK = new Color(200,110,90);
    private static final Color PANEL_BORDER = new Color(228, 180, 160);
    private static final Color DIVIDER = new Color(235, 224, 218);

    private static final int COLUMN_LEFT_WIDTH = 420;
    private static final int COLUMN_CENTER_WIDTH = 440;
    private static final int ROW_HEIGHT = 78;
    private static final int THUMB_W = 68;
    private static final int THUMB_H = 54;
    private static final int ROW_INNER_HPAD = 10;

    public PlanTripPanel(LocalDateTime startDate, LocalDateTime endDate, TravelPlannerApp mainApp, String tripTitle) {
        this.startDate = startDate.toLocalDate();
        this.endDate = endDate.toLocalDate();
        this.mainApp = mainApp;
        this.tripTitle = tripTitle;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(252, 248, 244));

        buildTopBar();
        buildBody();

        loadPlacesFor(selectedCategory);
        refreshSelectedList();
    }

    public String getTripTitle() { return tripTitle; }

    /* ── Top ─────────────────────────────────────────────── */
    private void buildTopBar() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(245, 242, 238));
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

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

        JButton back = new JButton("뒤로 가기");
        styleGhostButton(back);
        back.addActionListener(e -> mainApp.goToScheduleTab());

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);
        right.add(back);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    /* ── Body ────────────────────────────────────────────── */
    private void buildBody() {
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(6, 12, 16, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.weighty = 1;

        // Left
        JPanel left = boxedColumn(null);
        left.setPreferredSize(new Dimension(COLUMN_LEFT_WIDTH, 0));

        // ⟵⟵ 헤더(제목 + 카테고리 바)를 한 패널로 묶어 NORTH에 1번만 추가 (버튼 안 보이던 버그 fix)
        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));
        JLabel titleLeft = new JLabel("추천 장소");
        titleLeft.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleLeft.setForeground(new Color(100, 80, 70));
        titleLeft.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        leftHeader.add(titleLeft);

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

        // Center
        JPanel center = boxedColumn(null);
        center.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH, 0));

        JPanel centerHeader = new JPanel(new BorderLayout());
        centerHeader.setOpaque(false);
        JLabel titleCenter = new JLabel("나의 여행 계획");
        titleCenter.setFont(new Font("맑은 고딕", Font.BOLD, 20));
        titleCenter.setForeground(new Color(100, 80, 70));
        titleCenter.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        centerHeader.add(titleCenter, BorderLayout.WEST);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topBar.setOpaque(false);
        resetButton = new JButton("초기화");
        styleSolidButton(resetButton);
        resetButton.addActionListener(e -> {
            LocalDate sel = getSelectedDate();
            scheduledPlaces.removeIf(sp -> sp.getScheduled_date().toLocalDate().equals(sel));
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

        // Right (map placeholder)
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

    /* ── Load left list ─────────────────────────────────── */
    private void loadPlacesFor(String category) {
        placeListPanel.removeAll();

        if ("저장".equals("관광지")) {
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

    /* ── Row builders (좌/중 동일 스타일) ───────────────────── */
    private JPanel buildListRow(Object obj) {
        PlaceData pd = PlaceData.from(obj);   // id/name/addr/type/thumb
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

        JLabel title = ellipsisLabel("<b>"+safe(pd.name)+"</b> <span style='color:#9a9a9a'>("+pd.type+")</span>", fixedWidth);
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
        plus.setBackground(ACCENT);
        plus.setFocusPainted(false);
        plus.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        plus.addActionListener(e -> {
            LocalDate d = getSelectedDate();
            LocalDateTime visit = LocalDateTime.of(d, LocalTime.NOON);

            // 메타 캐시 (가운데 썸네일/이름 표시용)
            placeMetaIndex.put(pd.type + ":" + pd.id, pd);

            ScheduledPlace sp = new ScheduledPlace(
                    UUID.randomUUID().toString(),
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

    private JPanel buildCenterRow(ScheduledPlace sp) {
        String key = sp.getP_type() + ":" + sp.getP_id();
        PlaceData meta = placeMetaIndex.get(key);
        String name = (meta != null ? meta.name : "[" + sp.getP_id() + "]");
        String thumbUrl = (meta != null ? meta.thumbUrl : null);

        int fixedWidth = COLUMN_CENTER_WIDTH - 2*ROW_INNER_HPAD - 120;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
        row.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));

        // ⟵⟵ 가운데도 썸네일 표시 (이전엔 null로 고정되어 안 보였음)
        JLabel thumb = buildThumb(thumbUrl, THUMB_W, THUMB_H);
        row.add(thumb, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JLabel title = ellipsisLabel("<b>"+safe(name)+"</b> <span style='color:#9a9a9a'>("+sp.getP_type()+")</span>", fixedWidth);
        JLabel sub   = ellipsisLabel("", fixedWidth);
        sub.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        sub.setForeground(new Color(140,140,140));
        text.add(title);
        text.add(sub);
        row.add(text, BorderLayout.CENTER);

        JButton del = new JButton("삭제");
        styleDangerButton(del);
        del.addActionListener(e -> { scheduledPlaces.remove(sp); refreshSelectedList(); });

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
                    placeMetaIndex.put(chosen.type + ":" + chosen.id, chosen);
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

    /* ── Center list refresh ─────────────────────────────── */
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

    /* ── Utils / Style ───────────────────────────────────── */
    private JLabel buildThumb(String url, int w, int h) {
        JLabel lbl;
        if (url != null && !url.isBlank()) {
            try {
                ImageIcon icon = new ImageIcon(new URL(url));
                Image img = icon.getImage();
                if (img != null) {
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    lbl = new JLabel(new ImageIcon(scaled));
                } else lbl = noImg(w, h);
            } catch (Exception ignore) {
                lbl = noImg(w, h);
            }
        } else {
            lbl = noImg(w, h);
        }
        lbl.setPreferredSize(new Dimension(w, h));
        lbl.setBorder(new LineBorder(new Color(235, 224, 218), 1, true));
        return lbl;
    }

    private JLabel noImg(int w, int h) {
        JLabel n = new JLabel("No Img", SwingConstants.CENTER);
        n.setPreferredSize(new Dimension(w, h));
        n.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        n.setForeground(new Color(150, 150, 150));
        n.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        return n;
    }

    private JLabel ellipsisLabel(String innerHtml, int maxCssWidth) {
        String html = String.format(
                "<html><div style='width:%dpx; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;'>%s</div></html>",
                Math.max(50, maxCssWidth), innerHtml == null ? "" : innerHtml);
        JLabel l = new JLabel(html);
        l.setToolTipText(stripTags(innerHtml == null ? "" : innerHtml));
        return l;
    }

    private String stripTags(String s) { return s.replaceAll("\\<.*?\\>", ""); }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(new Color(220, 210, 200), 1, true));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(PANEL_BORDER, 1, true));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
    }

    private void styleCategoryChip(JButton b, boolean selected) {
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(selected ? ACCENT_DARK : PANEL_BORDER, 1, true));
        b.setBackground(selected ? ACCENT : Color.WHITE);
        b.setForeground(selected ? Color.WHITE : new Color(90, 80, 70));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(64, 30));
    }

    private void updateCategoryBarStyles() {
        for (Component c : categoryBar.getComponents()) {
            if (c instanceof JButton b) {
                styleCategoryChip(b, b.getText().equals(selectedCategory));
            }
        }
    }

    private void styleSolidButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(72, 32));
    }
    private void stylePrimaryButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(100, 140, 200));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(80, 120, 180), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(56, 28));
    }
    private void styleDangerButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(new Color(230, 110, 110));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(210, 90, 90), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(56, 28));
    }
    private void styleGhostButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(90, 80, 70));
        b.setBorder(new LineBorder(new Color(210, 200, 190), 1, true));
        b.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        b.setPreferredSize(new Dimension(96, 34));
    }

    private LocalDate getSelectedDate() {
        int idx = Math.max(0, dayComboBox.getSelectedIndex());
        return startDate.plusDays(idx);
    }

    private int nextOrder(LocalDate d) {
        return (int) scheduledPlaces.stream()
                .filter(sp -> sp.getScheduled_date().toLocalDate().equals(d))
                .count() + 1;
    }

    private static String safe(String s) { return s == null ? "" : s; }

    /* 간단한 DTO */
    private static class PlaceData {
        final String id, name, addr, type, thumbUrl;
        private PlaceData(String id, String name, String addr, String type, String thumbUrl) {
            this.id=id; this.name=name; this.addr=addr; this.type=type; this.thumbUrl=thumbUrl;
        }
        static PlaceData from(Object o) {
            if (o instanceof Tourist t)   return new PlaceData(t.getT_id(), t.getT_place(), t.getT_addr(), "관광지", t.getT_thum());
            if (o instanceof Restaurant r) return new PlaceData(r.getR_id(), r.getR_place(), r.getR_addr(), "맛집", r.getR_thum());
            if (o instanceof Lodging l)    return new PlaceData(l.getL_id(), l.getL_place(), l.getL_addr(), "숙소", null);
            if (o instanceof Festival f)    return new PlaceData(f.getF_id(), f.getF_name(), f.getF_addr(), "축제", f.getF_thum());
            return new PlaceData("", "알 수 없는 장소", "", "기타", null);
        }
    }
}
