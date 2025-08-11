package busantravel.ui;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.List;

public class PlanTripPanel extends JPanel {
    private JComboBox<String> dayComboBox;
    private JPanel placeListPanel;       // 좌측 장소목록 (스크롤 포함)
    private JPanel selectedPlacePanel;   // 중간 선택장소목록 (스크롤 포함)
    private JPanel mapPanel;             // 우측 지도 자리
    private List<TripDay> tripDays;
    private List<Place> allPlaces;
    private TravelPlannerApp mainApp;
    private String tripTitle;

    private String selectedCategory = "저장";   // 기본 카테고리
    private JPanel categoryButtonsPanel;        // 추천장소 내부에 카테고리 버튼 담음
    private JButton resetPlanButton;            // 여행 계획 초기화 버튼 (중간 영역)

    public PlanTripPanel(LocalDate startDate, LocalDate endDate, TravelPlannerApp mainApp) {
        this(startDate, endDate, mainApp, "새로운 여행");
    }

    public PlanTripPanel(LocalDate startDate, LocalDate endDate, TravelPlannerApp mainApp, String tripTitle) {
        this.mainApp = mainApp;
        this.tripTitle = tripTitle;

        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);

        tripDays = generateTripDays(startDate, endDate);
        allPlaces = PlaceService.getDummyPlaces();

        setupTopPanel();
        setupMainPanel();
        updateSelectedPlacesPanel();
    }

    public String getTripTitle() {
        return tripTitle;
    }

    // 상단 날짜 선택 및 뒤로 가기 버튼 포함
    private void setupTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(240,240,240));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // 날짜 선택 패널
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        datePanel.setOpaque(false);

        JLabel dateLabel = new JLabel("날짜 선택:");
        dateLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        datePanel.add(dateLabel);

        DefaultComboBoxModel<String> daysModel = new DefaultComboBoxModel<>();
        for (TripDay day : tripDays) {
            daysModel.addElement(day.getDisplayName());
        }
        dayComboBox = new JComboBox<>(daysModel);
        dayComboBox.setFont(new Font("맑은 고딕", Font.PLAIN, 16));
        dayComboBox.setSelectedIndex(0);
        dayComboBox.addActionListener(e -> updateSelectedPlacesPanel());
        datePanel.add(dayComboBox);

        topPanel.add(datePanel, BorderLayout.WEST);

        // 뒤로 가기 버튼 패널
        JButton backBtn = new JButton("뒤로 가기");
        backBtn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        backBtn.setBackground(new Color(150,150,150));
        backBtn.setForeground(Color.WHITE);
        backBtn.setFocusPainted(false);
        backBtn.addActionListener(e -> mainApp.goToScheduleTab());

        JPanel backBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        backBtnPanel.setOpaque(false);
        backBtnPanel.add(backBtn);
        topPanel.add(backBtnPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
    }

    // 중간 공간: 추천장소 + 카테고리 버튼, 여행계획 + 초기화 버튼, 지도
    private void setupMainPanel() {
        JPanel mainContentPanel = new JPanel(new GridBagLayout());
        mainContentPanel.setBackground(Color.WHITE);
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0,5,0,5);
        gbc.weighty = 1.0;

        // 좌측: 추천 장소 패널 + 카테고리 버튼
        JPanel leftWrapper = new JPanel(new BorderLayout());
        leftWrapper.setBackground(Color.WHITE);
        leftWrapper.setBorder(new LineBorder(new Color(222,146,124), 2, true));

        // 카테고리 버튼 패널
        categoryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 5));
        categoryButtonsPanel.setOpaque(false);

        String[] categories = {"저장", "관광지", "맛집", "축제", "숙소"};
        for(String category : categories) {
            JButton cbtn = new JButton(category);
            styleCategoryButton(cbtn, category.equals(selectedCategory));
            cbtn.addActionListener(e -> {
                selectedCategory = category;
                updateCategoryButtonColors();
                populatePlaceList();
            });
            categoryButtonsPanel.add(cbtn);
        }
        // 카테고리 패널을 위에 붙임
        leftWrapper.add(categoryButtonsPanel, BorderLayout.NORTH);

        // 추천 장소 리스트 (스크롤 가능)
        placeListPanel = new JPanel();
        placeListPanel.setLayout(new BoxLayout(placeListPanel, BoxLayout.Y_AXIS));
        placeListPanel.setBackground(Color.WHITE);
        populatePlaceList();

        JScrollPane leftScroll = new JScrollPane(placeListPanel);
        leftScroll.setBorder(null);
        leftScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        leftScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        leftWrapper.add(leftScroll, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.weightx = 0.2;
        mainContentPanel.add(leftWrapper, gbc);

        // 중간: 나의 여행 계획 + 초기화 버튼
        JPanel middleWrapper = new JPanel(new BorderLayout());
        middleWrapper.setBackground(Color.WHITE);
        middleWrapper.setBorder(new LineBorder(new Color(222,146,124), 2, true));

        // 초기화 버튼
        resetPlanButton = new JButton("초기화");
        resetPlanButton.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        resetPlanButton.setBackground(new Color(240,100,100));
        resetPlanButton.setForeground(Color.WHITE);
        resetPlanButton.setFocusPainted(false);
        resetPlanButton.setPreferredSize(new Dimension(100, 36));
        resetPlanButton.addActionListener(e -> {
            TripDay currentDay = tripDays.get(dayComboBox.getSelectedIndex());
            List<Place> toRemove = new ArrayList<>(currentDay.getPlaces());
            toRemove.forEach(currentDay::removePlace);
            updateSelectedPlacesPanel();
        });

        // 배치 버튼 위쪽에
        JPanel resetBtnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        resetBtnPanel.setOpaque(false);
        resetBtnPanel.add(resetPlanButton);
        middleWrapper.add(resetBtnPanel, BorderLayout.NORTH);

        // 선택 장소 리스트 (스크롤 가능)
        selectedPlacePanel = new JPanel();
        selectedPlacePanel.setLayout(new BoxLayout(selectedPlacePanel, BoxLayout.Y_AXIS));
        selectedPlacePanel.setBackground(Color.WHITE);

        JScrollPane centerScroll = new JScrollPane(selectedPlacePanel);
        centerScroll.setBorder(null);
        centerScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        centerScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        middleWrapper.add(centerScroll, BorderLayout.CENTER);

        gbc.gridx = 1;
        gbc.weightx = 0.2;
        mainContentPanel.add(middleWrapper, gbc);

        // 우측: 지도 자리
        mapPanel = new JPanel();
        mapPanel.setBackground(new Color(250, 240, 240));
        mapPanel.setBorder(new LineBorder(new Color(222,146,124), 2, true));

        gbc.gridx = 2;
        gbc.weightx = 0.6;
        mainContentPanel.add(mapPanel, gbc);

        add(mainContentPanel, BorderLayout.CENTER);
    }

    private void styleCategoryButton(JButton btn, boolean selected) {
        if(selected) {
            btn.setBackground(new Color(222,146,124));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(240,240,240));
            btn.setForeground(Color.BLACK);
        }
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
    }

    private void updateCategoryButtonColors() {
        for(Component c : categoryButtonsPanel.getComponents()) {
            if(c instanceof JButton) {
                JButton btn = (JButton) c;
                styleCategoryButton(btn, btn.getText().equals(selectedCategory));
            }
        }
    }

    private void populatePlaceList() {
        placeListPanel.removeAll();

        List<Place> filteredPlaces = new ArrayList<>();
        for(Place p : allPlaces) {
            if(selectedCategory.equals("저장")) {
                filteredPlaces.add(p);
            } else if(p.getCategory().equals(selectedCategory)) {
                filteredPlaces.add(p);
            }
        }

        for(Place place : filteredPlaces) {
            JPanel placePanel = createPlaceDisplayPanel(place);
            placeListPanel.add(placePanel);
            placeListPanel.add(Box.createVerticalStrut(5));
        }

        placeListPanel.revalidate();
        placeListPanel.repaint();
    }

    private JPanel createPlaceDisplayPanel(Place place) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230), 1, true));
        panel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameAndCategory = new JLabel("<html><b>" + place.getName() + "</b> (" + place.getCategory() + ")</html>");
        nameAndCategory.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        infoPanel.add(nameAndCategory);

        JLabel locationLabel = new JLabel(place.getLocation());
        locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        locationLabel.setForeground(Color.GRAY);
        infoPanel.add(locationLabel);

        JLabel photoLabel;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(place.getPhotoUrl()));
            Image image = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            photoLabel = new JLabel(new ImageIcon(image));
        } catch (Exception e) {
            photoLabel = new JLabel("No Img");
            photoLabel.setPreferredSize(new Dimension(60, 60));
            photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        panel.add(photoLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        JButton addButton = new JButton("+");
        addButton.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        addButton.setPreferredSize(new Dimension(40, 40));
        addButton.setBackground(new Color(222, 146, 124));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> {
            TripDay currentDay = tripDays.get(dayComboBox.getSelectedIndex());
            currentDay.addPlace(place);
            updateSelectedPlacesPanel();
        });
        panel.add(addButton, BorderLayout.EAST);

        return panel;
    }

    private void updateSelectedPlacesPanel() {
        selectedPlacePanel.removeAll();
        TripDay currentDay = tripDays.get(dayComboBox.getSelectedIndex());
        List<Place> selectedPlaces = new ArrayList<>(currentDay.getPlaces());

        for (int i = 0; i < selectedPlaces.size(); i++) {
            Place place = selectedPlaces.get(i);
            JPanel panel = createSelectedPlaceManagementPanel(place, i);
            selectedPlacePanel.add(panel);
            selectedPlacePanel.add(Box.createVerticalStrut(5));
        }

        selectedPlacePanel.revalidate();
        selectedPlacePanel.repaint();
    }

    private JPanel createSelectedPlaceManagementPanel(Place place, int index) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        panel.setBorder(BorderFactory.createLineBorder(new Color(222, 146, 124), 1, true));
        panel.setBackground(Color.WHITE);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameAndCategory = new JLabel("<html><b>" + place.getName() + "</b> (" + place.getCategory() + ")</html>");
        nameAndCategory.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        infoPanel.add(nameAndCategory);

        JLabel locationLabel = new JLabel(place.getLocation());
        locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
        locationLabel.setForeground(Color.GRAY);
        infoPanel.add(locationLabel);

        JLabel photoLabel;
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(place.getPhotoUrl()));
            Image image = icon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            photoLabel = new JLabel(new ImageIcon(image));
        } catch (Exception e) {
            photoLabel = new JLabel("No Img");
            photoLabel.setPreferredSize(new Dimension(60, 60));
            photoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            photoLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        }
        panel.add(photoLabel, BorderLayout.WEST);
        panel.add(infoPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);

        JButton deleteButton = new JButton("삭제");
        deleteButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        deleteButton.setBackground(new Color(240, 100, 100));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        deleteButton.addActionListener(e -> {
            TripDay currentDay = tripDays.get(dayComboBox.getSelectedIndex());
            currentDay.removePlace(place);
            updateSelectedPlacesPanel();
        });
        buttonPanel.add(deleteButton);

        JButton replaceButton = new JButton("교체");
        replaceButton.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        replaceButton.setBackground(new Color(100, 150, 240));
        replaceButton.setForeground(Color.WHITE);
        replaceButton.setFocusPainted(false);
        replaceButton.addActionListener(e -> {
            Place newPlace = selectReplacementPlace(place);
            if (newPlace != null) {
                TripDay currentDay = tripDays.get(dayComboBox.getSelectedIndex());
                currentDay.replacePlace(index, newPlace);
                updateSelectedPlacesPanel();
            }
        });
        buttonPanel.add(replaceButton);

        panel.add(buttonPanel, BorderLayout.EAST);

        return panel;
    }

    private Place selectReplacementPlace(Place oldPlace) {
        List<Place> candidates = new ArrayList<>(allPlaces);
        candidates.remove(oldPlace);

        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this, "교체할 다른 장소가 없습니다.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }

        String[] options = candidates.stream()
                .map(Place::getName)
                .toArray(String[]::new);

        String selectedName = (String) JOptionPane.showInputDialog(this,
                "교체할 장소를 선택하세요:",
                "장소 교체",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (selectedName == null) return null;

        return candidates.stream()
                .filter(p -> p.getName().equals(selectedName))
                .findFirst()
                .orElse(null);
    }

    private List<TripDay> generateTripDays(LocalDate startDate, LocalDate endDate) {
        List<TripDay> days = new ArrayList<>();
        LocalDate date = startDate;
        int dayNum = 1;

        while (!date.isAfter(endDate)) {
            String dayLabel = String.format("%d일차 (%d월 %d일 %s)",
                    dayNum,
                    date.getMonthValue(),
                    date.getDayOfMonth(),
                    date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN));
            days.add(new TripDay(dayLabel, date));
            date = date.plusDays(1);
            dayNum++;
        }

        return days;
    }
}
