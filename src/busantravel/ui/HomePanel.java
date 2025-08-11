package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.Tourist;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomePanel extends JPanel {
    private JPanel gridPanel;
    private JFrame frame;
    private JScrollPane scrollPane;
    private Map<String, JButton> categoryButtons = new HashMap<>(); // 카테고리별 버튼 저장
    private String selectedCategory = "관광지"; // 기본 선택 카테고리
    private PlaceDAO placeDAO;

    public HomePanel(JFrame frame) {
        this.placeDAO = new PlaceDAO();
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // --- 상단 카테고리 버튼 바 ---
        JPanel topCategoryPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        topCategoryPanel.setBackground(new Color(250, 250, 250));

        // 순서 변경: 관광지, 맛집, 숙소, 축제
        String[] categories = {"관광지", "맛집", "숙소", "축제"};
        for (String category : categories) {
            JButton btn = new JButton(category);
            btn.setPreferredSize(new Dimension(100, 35));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            btn.addActionListener(e -> {
                selectedCategory = category;
                updateCategoryButtonColors();
                loadPlaces(category);
            });
            categoryButtons.put(category, btn);
            topCategoryPanel.add(btn);
        }

        // --- 장소 카드 패널 ---
        gridPanel = new JPanel(new GridLayout(0, 4, 30, 30));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        gridPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);

        add(topCategoryPanel, BorderLayout.NORTH); // 카테고리 패널 추가
        add(scrollPane, BorderLayout.CENTER);

        // 초기 버튼 색상 및 장소 로드
        updateCategoryButtonColors();
        loadPlaces(selectedCategory);
    }

    // 선택된 카테고리 버튼 색상 업데이트
    private void updateCategoryButtonColors() {
        for (String category : categoryButtons.keySet()) {
            JButton btn = categoryButtons.get(category);
            if (category.equals(selectedCategory)) {
                btn.setBackground(new Color(180, 90, 70)); // 진한 색 (선택됨)
            } else {
                btn.setBackground(new Color(222, 146, 124)); // 기본 색
            }
        }
    }

    // 장소 데이터 로드
    private void loadPlaces(String category) {
        gridPanel.removeAll();
        List<Object> places = placeDAO.getPlacesByCategory(category);

        if (places.isEmpty()) {
            gridPanel.add(new JLabel("데이터가 없습니다."));
        } else {
            for (Object place : places) {
                String placeName = "";
                String thumbnail = "";
                if (place instanceof Tourist) {
                    placeName = ((Tourist) place).getT_place();
                    thumbnail = ((Tourist) place).getT_thum();
                } else if (place instanceof Restaurant) {
                    placeName = ((Restaurant) place).getR_place();
                    thumbnail = ((Restaurant) place).getR_thum();
                } else if (place instanceof Lodging) {
                    placeName = ((Lodging) place).getL_place();
                } else if (place instanceof Festival) {
                    placeName = ((Festival) place).getF_name();
                    thumbnail = ((Festival) place).getF_thum();
                }
                gridPanel.add(new PlaceCard(frame, placeName, category, thumbnail));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    // 검색어로 장소 데이터 로드
    private void loadPlacesBySearch(String query) {
        gridPanel.removeAll();
        List<Object> places = placeDAO.searchPlaces(query);

        if (places.isEmpty()) {
            gridPanel.add(new JLabel("검색 결과가 없습니다."));
        } else {
            for (Object place : places) {
                String placeName = "";
                String category = ""; // 검색 결과는 카테고리가 혼합될 수 있으므로, PlaceCard에 전달할 카테고리 필요
                String thumbnail = "";

                if (place instanceof Tourist) {
                    placeName = ((Tourist) place).getT_place();
                    category = "관광지";
                    thumbnail = ((Tourist) place).getT_thum();
                } else if (place instanceof Restaurant) {
                    placeName = ((Restaurant) place).getR_place();
                    category = "맛집";
                    thumbnail = ((Restaurant) place).getR_thum();
                } else if (place instanceof Lodging) {
                    placeName = ((Lodging) place).getL_place();
                    category = "숙소";
                    // Lodging 모델에 썸네일 필드가 없으므로 빈 문자열
                } else if (place instanceof Festival) {
                    placeName = ((Festival) place).getF_name();
                    category = "축제";
                    thumbnail = ((Festival) place).getF_thum();
                }
                gridPanel.add(new PlaceCard(frame, placeName, category, thumbnail));
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
    }

    // 외부에서 검색을 트리거할 수 있는 메서드
    public void performSearch(String query) {
        loadPlacesBySearch(query);
    }
}
