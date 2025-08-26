package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Interest;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.ScheduledPlace;
import busantravel.model.Tourist;
import busantravel.service.InterestService;
import busantravel.service.ScheduledPlaceService;
import busantravel.util.ApiHandler;
import busantravel.util.Coordinate;
import busantravel.util.PlaceInfo;
import busantravel.util.StaticMapGenerator;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

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
    private final Map<String, String> placeAddrIndex = new HashMap<>();
    private final Map<String, String> scheduledPlaceColors = new HashMap<>();
    private JLabel mapLabel;
    private final ApiHandler apiHandler = new ApiHandler();
    private final StaticMapGenerator mapGenerator;
    private static final String TMAP_API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg";

    private String selectedCategory = "찜"; // 현재 선택된 카테고리
    private int currentPage = 1;
    private static final int ITEMS_PER_PAGE = 20;
    private JPanel paginationControls;

    private static final Color ACCENT_DARK = new Color(200,110,90);
    private static final Color PANEL_BORDER = new Color(228, 180, 160);
    private static final Color DIVIDER = new Color(235, 224, 218);

    private static final int COLUMN_LEFT_WIDTH = 420;
    private static final int COLUMN_CENTER_WIDTH = 440;
    private static final int ROW_HEIGHT = 78;
    private static final int THUMB_W = 68;
    private static final int THUMB_H = 54;
    private static final int ROW_INNER_HPAD = 10;
    private Font jalnanFont;

    private static final List<String> COLOR_PRESETS = Arrays.asList(
        "E6194B", "3CB44B", "FFE119", "4363D8", "F58231",
        "911EB4", "46F0F0", "F032E6", "BCF60C", "FABEBE"
    );

    public PlanTripPanel(LocalDateTime startDate, LocalDateTime endDate,
                         TravelPlannerApp mainApp, String tripTitle, String planId, int dayIndex, Runnable onBack) {
        this.startDate = startDate.toLocalDate();
        this.endDate = endDate.toLocalDate();
        this.mainApp = mainApp;
        this.tripTitle = tripTitle;
        this.planId = planId;
        this.onBack = onBack;
        this.scheduledPlaces = ScheduledPlaceService.getSchedulesByPlanId(planId);

        Set<String> uniquePlaceKeys = new LinkedHashSet<>();
        for (ScheduledPlace sp : this.scheduledPlaces) {
            uniquePlaceKeys.add(sp.getP_type() + ":" + sp.getP_id());
        }

        int colorIndex = 0;
        Random rand = new Random();
        for (String key : uniquePlaceKeys) {
            if (colorIndex < COLOR_PRESETS.size()) {
                scheduledPlaceColors.put(key, COLOR_PRESETS.get(colorIndex));
            } else {
                scheduledPlaceColors.put(key, String.format("%06X", rand.nextInt(0xFFFFFF + 1)));
            }
            colorIndex++;
        }
        
        this.mapGenerator = new StaticMapGenerator(TMAP_API_KEY);

        setLayout(new BorderLayout(10, 10));

        buildTopBar();
        buildBody();

        if (dayComboBox != null && dayIndex >= 0) {
            dayComboBox.setSelectedIndex(dayIndex - 1);
        }
        
        for (ScheduledPlace sp : scheduledPlaces) {
            String type = sp.getP_type();
            String id = sp.getP_id();
            Object place = placeDAO.getPlaceByTypeAndId(type, id);
            if (place != null) {
                PlaceData pd = PlaceData.from(place);
                String key = pd.type + ":" + pd.id;
                placeNameIndex.put(key, pd.name);
                placeThumbIndex.put(key, pd.thumbUrl);
                placeAddrIndex.put(key, pd.addr);
            }
        }

        loadPlacesFor(selectedCategory);
        refreshSelectedList();
    }

    public String getTripTitle() { return tripTitle; }
    public String getPlanId() { return planId; }

    private void buildTopBar() {
        loadFont();
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(245, 242, 238));
        top.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel titleLabel = new JLabel(tripTitle);             
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        left.add(titleLabel);

        JLabel dateLabel = new JLabel("날짜 선택:");
        dateLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
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

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        JButton saveBtn = new JButton("저장");
        styleSolidButton(saveBtn);
        saveBtn.addActionListener(e -> {
        if (mainApp != null) {
            ScheduledPlaceService.saveSchedulesForPlan(planId, scheduledPlaces);
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

    private void buildBody() {
        loadFont();
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(6, 12, 16, 12));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 8, 0, 8);
        gbc.weighty = 1;

        JPanel left = boxedColumn(null);
        left.setPreferredSize(new Dimension(COLUMN_LEFT_WIDTH, 0));

        JPanel leftHeader = new JPanel();
        leftHeader.setOpaque(false);
        leftHeader.setLayout(new BoxLayout(leftHeader, BoxLayout.Y_AXIS));

        JLabel leftTitle = new JLabel("추천 장소");
        leftTitle.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        leftTitle.setForeground(new Color(100, 80, 70));
        leftTitle.setAlignmentX(LEFT_ALIGNMENT);
        leftTitle.setBorder(BorderFactory.createEmptyBorder(0, 2, 6, 0));
        leftHeader.add(leftTitle);

        categoryBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        categoryBar.setOpaque(false);
        for (String c : new String[]{"찜","관광지","맛집","숙소"}) {
            JButton b = new JButton(c);
            styleCategoryChip(b, c.equals(selectedCategory));
            b.addActionListener(e -> {
                currentPage = 1;
                selectedCategory = c;
                updateCategoryBarStyles();
                loadPlacesFor(selectedCategory);
            });
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

        paginationControls = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationControls.setOpaque(false);
        left.add(paginationControls, BorderLayout.SOUTH);

        gbc.gridx = 0; gbc.weightx = 0.30;
        grid.add(left, gbc);

        JPanel center = boxedColumn(null);
        center.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH, 0));

        JPanel centerHeader = new JPanel(new BorderLayout());
        centerHeader.setOpaque(false);
        JLabel centerTitle = new JLabel("나의 여행 계획");
        centerTitle.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
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

        JPanel right = boxedColumn(null);
        right.setPreferredSize(new Dimension(520, 0));
        mapLabel = new JLabel("일정에 장소를 추가하면 지도가 표시됩니다.", SwingConstants.CENTER);
        mapLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        mapLabel.setForeground(new Color(150, 160, 150));
        right.add(mapLabel, BorderLayout.CENTER);

        gbc.gridx = 2; gbc.weightx = 0.36;
        grid.add(right, gbc);

        add(grid, BorderLayout.CENTER);
    }

    private JPanel boxedColumn(String title) {
        loadFont();
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Color.white);
        p.setBorder(
                BorderFactory.createEmptyBorder(10, 12, 12, 12)
        );
        if (title != null) {
            JLabel t = new JLabel(title);
            t.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
            t.setBorder(BorderFactory.createEmptyBorder(0, 2, 8, 0));
            t.setForeground(new Color(100, 80, 70));
            p.add(t, BorderLayout.PAGE_START);
        }
        return p;
    }

    private void loadPlacesFor(String category) {
        placeListPanel.removeAll();
        if ("찜".equals(category)) {
            if (!busantravel.auth.MemberSession.isLoggedIn()) {
                JOptionPane.showMessageDialog(this, "로그인이 필요한 서비스입니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
                selectedCategory = "관광지";
                updateCategoryBarStyles();
                loadPlacesFor(selectedCategory);
                return;
            }
            InterestService interestService = new InterestService();
            List<busantravel.model.Interest> interests = interestService.getInterestById(busantravel.auth.MemberSession.getId());
            List<Object> savedPlaces = new ArrayList<>();
            for (busantravel.model.Interest interest : interests) {
                savedPlaces.add(placeDAO.getPlaceByTypeAndId(interest.getP_type(), interest.getP_id()));
            }
            currentCategoryPlaces = savedPlaces;
        } else {
            currentCategoryPlaces = placeDAO.getPlacesByCategory(category);
        }

        int totalItems = currentCategoryPlaces.size();
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int start = (currentPage - 1) * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, totalItems);

        if (start < totalItems) {
            List<Object> placesToShow = currentCategoryPlaces.subList(start, end);
            for (Object obj : placesToShow) {
                placeListPanel.add(buildListRow(obj));
            }
        }

        updatePaginationControls(totalPages);

        placeListPanel.revalidate();
        placeListPanel.repaint();
    }

    private JPanel buildListRow(Object obj) {
        loadFont();
        PlaceData pd = PlaceData.from(obj);
        placeNameIndex.put(pd.type + ":" + pd.id, pd.name);
        placeThumbIndex.put(pd.type + ":" + pd.id, pd.thumbUrl);
        placeAddrIndex.put(pd.type + ":" + pd.id, pd.addr);

        int fixedWidth = COLUMN_LEFT_WIDTH - 2*ROW_INNER_HPAD - 6;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
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
        title.setFont(jalnanFont.deriveFont(Font.PLAIN, 13f));
        addr.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        addr.setForeground(new Color(140,140,140));
        text.add(title);
        text.add(addr);
        row.add(text, BorderLayout.CENTER);


        JButton plus = new JButton("+");
        plus.setPreferredSize(new Dimension(36, 36));
        plus.setFont(jalnanFont.deriveFont(Font.BOLD, 20f));
        plus.setForeground(Color.WHITE);
        plus.setBackground(ColorConstants.ACCENT);
        plus.setFocusPainted(false);
        plus.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        plus.addActionListener(e -> {
            LocalDate d = getSelectedDate();
            LocalDateTime visit = LocalDateTime.of(d, LocalTime.MIDNIGHT);
            ScheduledPlace sp = new ScheduledPlace(
                planId,
                pd.id, pd.type,
                visit,
                nextOrder(d),
                null
            );
            
            String key = sp.getP_type() + ":" + sp.getP_id();
            if (!scheduledPlaceColors.containsKey(key)) {
                int colorIndex = scheduledPlaceColors.size();
                String color;
                if (colorIndex < COLOR_PRESETS.size()) {
                    color = COLOR_PRESETS.get(colorIndex);
                } else {
                    Random rand = new Random();
                    color = String.format("%06X", rand.nextInt(0xFFFFFF + 1));
                }
                scheduledPlaceColors.put(key, color);
            }

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
        loadFont();
        String key = sp.getP_type() + ":" + sp.getP_id();
        String name = placeNameIndex.getOrDefault(key, "[" + sp.getP_id() + "]");
        String thumbUrl = placeThumbIndex.get(key);

        int fixedWidth = COLUMN_CENTER_WIDTH - 2*ROW_INNER_HPAD - 120;

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        row.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
        row.setPreferredSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));
        row.setMaximumSize(new Dimension(COLUMN_CENTER_WIDTH - 32, ROW_HEIGHT));

        JPanel westPanel = new JPanel(new BorderLayout(4, 0));
        westPanel.setOpaque(false);

        String colorHex = scheduledPlaceColors.get(key);

        JPanel colorChip = new JPanel();
        colorChip.setPreferredSize(new Dimension(12, 12));
        if (colorHex != null) {
            colorChip.setBackground(Color.decode("#" + colorHex));
        } else {
            colorChip.setBackground(Color.GRAY);
        }
        colorChip.setBorder(new LineBorder(Color.DARK_GRAY));

        JPanel chipContainer = new JPanel(new GridBagLayout());
        chipContainer.setOpaque(false);
        chipContainer.add(colorChip);

        westPanel.add(chipContainer, BorderLayout.WEST);
        JLabel thumb = buildThumb(thumbUrl, THUMB_W, THUMB_H);
        westPanel.add(thumb, BorderLayout.CENTER);

        row.add(westPanel, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setBorder(BorderFactory.createEmptyBorder(10,0,10,0));

        JLabel title = ellipsisLabel("<html><b>"+safe(name)+"</b> <span style='color:#9a9a9a'>("+sp.getP_type()+")</span></html>", fixedWidth);
        JLabel sub   = ellipsisLabel("", fixedWidth);
        title.setFont(jalnanFont.deriveFont(Font.PLAIN, 13f));
        sub.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        sub.setForeground(new Color(140,140,140));
        text.add(title);
        text.add(sub);
        row.add(text, BorderLayout.CENTER);



        JButton del = new JButton("삭제");
        styleDangerButton(del);
        del.addActionListener(e -> {
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
        updateMap();
    }

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

    private JLabel noImg(int w, int h) {
        loadFont();
        JLabel n = new JLabel("No Img", SwingConstants.CENTER);
        n.setPreferredSize(new Dimension(w, h));
        n.setFont(jalnanFont.deriveFont(Font.PLAIN, 11f));
        n.setForeground(new Color(150, 150, 150));
        n.setBorder(new LineBorder(new Color(220, 220, 220), 1, true));
        return n;
    }

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

    private String stripTags(String s) { return s.replaceAll("\\<.*?\\>", ""); }

    private void styleCombo(JComboBox<?> cb) {
        loadFont();
        cb.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        cb.setBackground(Color.WHITE);
        cb.setBorder(new LineBorder(new Color(220, 210, 200), 1, true));
    }

    private void styleScroll(JScrollPane sp) {
        sp.setBorder(new LineBorder(PANEL_BORDER, 1, true));
        sp.getVerticalScrollBar().setUnitIncrement(18);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
    }

    private void styleCategoryChip(JButton b, boolean selected) {
        loadFont();
        b.setFocusPainted(false);
        b.setBorder(new LineBorder(selected ? ACCENT_DARK : PANEL_BORDER, 1, true));
        b.setBackground(selected ? ColorConstants.ACCENT : Color.WHITE);
        b.setForeground(selected ? Color.WHITE : new Color(90, 80, 70));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 13f));
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
        loadFont();
        b.setFocusPainted(false);
        b.setBackground(ColorConstants.ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 13f));
        b.setPreferredSize(new Dimension(72, 32));
    }

    private void stylePrimaryButton(JButton b) {
        loadFont();
        b.setFocusPainted(false);
        b.setBackground(new Color(100, 140, 200));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(80, 120, 180), 1, true));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 12f));
        b.setPreferredSize(new Dimension(56, 28));
    }

    private void styleDangerButton(JButton b) {
        loadFont();
        b.setFocusPainted(false);
        b.setBackground(new Color(230, 110, 110));
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(new Color(210, 90, 90), 1, true));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 12f));
        b.setPreferredSize(new Dimension(56, 28));
    }

    private void styleGhostButton(JButton b) {
        loadFont();
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(90, 80, 70));
        b.setBorder(new LineBorder(new Color(210, 200, 190), 1, true));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 13f));
        b.setPreferredSize(new Dimension(96, 34));
    }

    private void stylePaginationButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(ColorConstants.ACCENT);
        b.setForeground(Color.WHITE);
        b.setBorder(new LineBorder(ACCENT_DARK, 1, true));
        b.setFont(jalnanFont.deriveFont(Font.BOLD, 13f));
        b.setPreferredSize(new Dimension(40, 30));
    }

    private void updatePaginationControls(int totalPages) {
        paginationControls.removeAll();

        if (totalPages <= 1) {
            paginationControls.revalidate();
            paginationControls.repaint();
            return;
        }

        JButton prev = new JButton("<");
        stylePaginationButton(prev);
        prev.setEnabled(currentPage > 1);
        prev.addActionListener(e -> {
            if (currentPage > 1) {
                currentPage--;
                loadPlacesFor(selectedCategory);
            }
        });

        JLabel pageLabel = new JLabel(currentPage + " / " + totalPages);
        pageLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        pageLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

        JButton next = new JButton(">");
        stylePaginationButton(next);
        next.setEnabled(currentPage < totalPages);
        next.addActionListener(e -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadPlacesFor(selectedCategory);
            }
        });

        paginationControls.add(prev);
        paginationControls.add(pageLabel);
        paginationControls.add(next);

        paginationControls.revalidate();
        paginationControls.repaint();
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

    private void updateMap() {
        LocalDate selectedDate = getSelectedDate();
        List<ScheduledPlace> todaysPlaces = scheduledPlaces.stream()
            .filter(sp -> sp.getScheduled_date().toLocalDate().equals(selectedDate))
            .collect(Collectors.toList());

        if (todaysPlaces.isEmpty()) {
            mapLabel.setIcon(null);
            mapLabel.setText("선택한 날짜에 예정된 장소가 없습니다.");
            return;
        }

        new Thread(() -> {
            List<Coordinate> coordinates = new ArrayList<>();
            List<String> markerList = new ArrayList<>();
            Random rand = new Random();

            for (ScheduledPlace sp : todaysPlaces) {
                String placeAddr = placeAddrIndex.get(sp.getP_type() + ":" + sp.getP_id());
                String placeName = placeNameIndex.get(sp.getP_type() + ":" + sp.getP_id());
                
                String searchQuery = (placeAddr != null && !placeAddr.isBlank()) ? placeAddr : placeName;

                if (searchQuery != null) {
                    PlaceInfo placeInfo = apiHandler.getPlaceDetailsByName(searchQuery, TMAP_API_KEY);
                    if (placeInfo != null) {
                        Coordinate coord = new Coordinate(placeInfo.getLatitude(), placeInfo.getLongitude());
                        coordinates.add(coord);

                        String key = sp.getP_type() + ":" + sp.getP_id();
                        String color = scheduledPlaceColors.get(key);
                        if (color == null) {
                            color = String.format("%06X", rand.nextInt(0xFFFFFF + 1));
                            scheduledPlaceColors.put(key, color);
                        }
                        markerList.add(String.format("pin-m@%s(%f,%f)viewSize:0.3", color, coord.getLongitude(), coord.getLatitude()));
                    }
                }
            }

            if (!coordinates.isEmpty()) {
                Coordinate center = calculateCenter(coordinates);
                
                int zoom = 11;
                if (coordinates.size() > 1) {
                    double maxDistance = calculateMaxDistance(coordinates);
                    if (maxDistance >= 24) {
                        zoom = 9;
                    } else if (maxDistance >= 14) {
                        zoom = 10;
                    }
                }

                String markersValue = String.join(",", markerList);
                String mapUrl = mapGenerator.generateMapWithMarkers(center, markersValue, 520, mapLabel.getHeight(), zoom);
                if (mapUrl != null) {
                    try {
                        ImageIcon icon = new ImageIcon(new URL(mapUrl));
                        SwingUtilities.invokeLater(() -> {
                            mapLabel.setText("");
                            mapLabel.setIcon(icon);
                        });
                    } catch (Exception e) {
                        SwingUtilities.invokeLater(() -> mapLabel.setText("지도 이미지를 불러올 수 없습니다."));
                    }
                }
            } else {
                SwingUtilities.invokeLater(() -> mapLabel.setText("장소의 좌표를 찾을 수 없습니다."));
            }
        }).start();
    }

    private double calculateDistance(Coordinate c1, Coordinate c2) {
        final int R = 6371;
        double lat1 = c1.getLatitude();
        double lon1 = c1.getLongitude();
        double lat2 = c2.getLatitude();
        double lon2 = c2.getLongitude();

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    private double calculateMaxDistance(List<Coordinate> coordinates) {
        double maxDistance = 0;
        for (int i = 0; i < coordinates.size(); i++) {
            for (int j = i + 1; j < coordinates.size(); j++) {
                double distance = calculateDistance(coordinates.get(i), coordinates.get(j));
                if (distance > maxDistance) {
                    maxDistance = distance;
                }
            }
        }
        return maxDistance;
    }

    private Coordinate calculateCenter(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.isEmpty()) {
            return new Coordinate(35.1581, 129.0596);
        }
        if (coordinates.size() == 1) {
            return coordinates.get(0);
        }
        double sumLat = 0, sumLon = 0;
        for (Coordinate c : coordinates) {
            sumLat += c.getLatitude();
            sumLon += c.getLongitude();
        }
        return new Coordinate(sumLat / coordinates.size(), sumLon / coordinates.size());
    }

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
            ImageIcon icon = memoryCache.get(key);
            if (icon != null) return icon;

            File imageFile = cacheDir.resolve(getFileName(key)).toFile();
            if (imageFile.exists()) {
                try {
                    BufferedImage img = ImageIO.read(imageFile);
                    if (img != null) {
                        icon = new ImageIcon(img);
                        memoryCache.put(key, icon);
                        return icon;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }

        public void putImage(String key, Image image) {
            if (image == null) return;
            memoryCache.put(key, new ImageIcon(image));

            File imageFile = cacheDir.resolve(getFileName(key)).toFile();
            new Thread(() -> {
                try {
                    BufferedImage bufferedImg = toBufferedImage(image);
                    if (bufferedImg != null) {
                        ImageIO.write(bufferedImg, "png", imageFile);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        private String getFileName(String key) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder(digest.length * 2);
                for (byte b : digest) sb.append(String.format("%02x", b));
                return sb.toString() + ".png";
            } catch (Exception e) {
                return Integer.toHexString(key.hashCode()) + ".png";
            }
        }

        private static BufferedImage toBufferedImage(Image img) {
            if (img instanceof BufferedImage) return (BufferedImage) img;

            ImageIcon icon = new ImageIcon(img);
            int width = icon.getIconWidth();
            int height = icon.getIconHeight();

            if (width <= 0 || height <= 0) {
                return null;
            }

            BufferedImage bimage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bimage.createGraphics();
            g.drawImage(icon.getImage(), 0, 0, null);
            g.dispose();
            return bimage;
        }
    }

    private static class PlaceData {
        final String id, name, addr, type, thumbUrl;
        private PlaceData(String id, String name, String addr, String type, String thumbUrl) {
            this.id=id; this.name=name; this.addr=addr; this.type=type; this.thumbUrl=thumbUrl;
        }

        static PlaceData from(Object o) {
            if (o instanceof Tourist t)   return new PlaceData(t.getT_id(), t.getT_place(), t.getT_addr(), "관광지", t.getT_thum());
            if (o instanceof Restaurant r) return new PlaceData(r.getR_id(), r.getR_place(), r.getR_addr(), "맛집", r.getR_thum());
            if (o instanceof Lodging l)    return new PlaceData(l.getL_id(), l.getL_place(), l.getL_addr(), "숙소", l.getL_thum());
            if (o instanceof Festival f)    return new PlaceData(f.getF_id(), f.getF_name(), f.getF_addr(), "축제", f.getF_thum());
            return new PlaceData("", "알 수 없는 장소", "", "기타", null);
        }
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
