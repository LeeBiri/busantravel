package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.model.Festival;
import busantravel.model.Lodging;
import busantravel.model.Restaurant;
import busantravel.model.Tourist;
import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;

/**
 * 애플리케이션의 메인 화면을 구성하는 패널 클래스입니다.
 * 관광지, 맛집, 숙소, 축제 등 다양한 장소 정보를 카테고리별로 보여주며,
 * 검색 기능과 페이지네이션을 통해 사용자가 정보를 쉽게 탐색할 수 있도록 합니다.
 */
public class HomePanel extends JPanel {
    private JPanel gridPanel; // 장소 카드를 표시하는 그리드 패널
    private JFrame frame; // 상위 프레임
    private Font jalnanFont;
    private JScrollPane scrollPane; // gridPanel을 담는 스크롤 패널
    private Map<String, JButton> categoryButtons = new HashMap<>(); // 카테고리별 버튼 저장
    private String selectedCategory = "관광지"; // 기본 선택 카테고리
    private PlaceDAO placeDAO; // 데이터 접근 객체
    private JTextField searchField; // 검색어 입력 필드
    private JButton searchButton; // 검색 실행 버튼
    private JPanel paginationPanel; // 페이지네이션 UI를 담는 패널

    private int currentPage = 1; // 현재 페이지 번호
    private int pageSize = 24; // 한 페이지에 보여줄 항목 수
    private int totalPages = 1; // 전체 페이지 수
    private String currentSearchQuery = null; // 현재 검색어 저장

    /**
     * 애플리케이션에서 사용할 커스텀 폰트를 로드합니다.
     * 폰트 파일을 찾지 못할 경우, 기본 시스템 폰트를 사용합니다.
     */
    private void loadFont() {
        try {
            InputStream is = getClass().getResourceAsStream("/busantravel/resources/JalnanGothicTTF.ttf");
            if (is != null) {
                jalnanFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(16f);
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


    /**
     * HomePanel의 새 인스턴스를 생성합니다.
     * UI 컴포넌트를 초기화하고 레이아웃을 설정하며, 초기 데이터를 로드합니다.
     *
     * @param frame 상위 JFrame 객체.
     */
    public HomePanel(JFrame frame) {
        this.placeDAO = new PlaceDAO();
        this.frame = frame;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        loadFont();
        setFont(jalnanFont);
        // --- 상단 카테고리 및 검색 바 ---
        JPanel topBarPanel = new JPanel();
        topBarPanel.setLayout(new BoxLayout(topBarPanel, BoxLayout.Y_AXIS));
        topBarPanel.setBackground(new Color(250, 250, 250));
        topBarPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, ColorConstants.ACCENT), // 하단 구분선
            BorderFactory.createEmptyBorder(10, 20, 10, 20)  // 기존 패딩
        ));

        // 카테고리 버튼 패널
        JPanel categoryButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        categoryButtonsPanel.setOpaque(false);

        // 순서 변경: 관광지, 맛집, 숙소, 축제
        String[] categories = {"관광지", "맛집", "숙소", "축제"};
        for (String category : categories) {
            JButton btn = new JButton(category);
            btn.setPreferredSize(new Dimension(100, 35));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setFont(new Font(jalnanFont.getName(), Font.PLAIN, 14));
            btn.addActionListener(e -> {
                loadPlaces(category, 1);
            });
            categoryButtons.put(category, btn);
            categoryButtonsPanel.add(btn);
        }
        
        topBarPanel.add(categoryButtonsPanel);
        topBarPanel.add(Box.createVerticalStrut(10)); // 간격 추가

        // 검색 UI 패널
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        searchPanel.setOpaque(false);
        searchPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30)); // 높이 고정 (마진 축소)

        searchField = new JTextField(20); // 너비 조정
        searchField.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        
        // 엔터키 리스너
        searchField.addActionListener(e -> searchButton.doClick());

        searchButton = new RoundedButton("검색"); // 둥근 버튼으로 변경
        searchButton.setPreferredSize(new Dimension(70, 25));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBackground(ColorConstants.ACCENT); // 악센트 컬러로 변경
        searchButton.setFocusPainted(false);
        searchButton.setBorderPainted(false);
        searchButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));

        searchButton.addActionListener(e -> {
            String query = searchField.getText();
            if (query != null && !query.trim().isEmpty()) {
                performSearch(query.trim());
            } else {
                // 검색어가 비어있으면 현재 카테고리의 모든 장소를 로드
                loadPlaces(selectedCategory);
            }
        });

        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(5));
        searchPanel.add(searchButton);
        
        topBarPanel.add(searchPanel);


        // --- 장소 카드 패널 ---
        gridPanel = new JPanel(); // 레이아웃은 loadPlaces에서 동적으로 설정
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        gridPanel.setBackground(Color.WHITE);

        scrollPane = new JScrollPane(gridPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.setBorder(null);

        // --- 페이지네이션 패널 ---
        paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        paginationPanel.setBackground(Color.WHITE);

        add(topBarPanel, BorderLayout.NORTH); // 상단 바 패널 추가
        add(scrollPane, BorderLayout.CENTER);
        add(paginationPanel, BorderLayout.SOUTH); // 페이지네이션 패널 추가

        // 초기 버튼 색상 및 장소 로드
        updateCategoryButtonColors();
        loadPlaces(selectedCategory);
    }

    /**
     * 현재 선택된 카테고리에 따라 버튼의 배경색을 업데이트하여
     * 시각적으로 선택 상태를 표시합니다.
     */
    private void updateCategoryButtonColors() {
        for (String category : categoryButtons.keySet()) {
            JButton btn = categoryButtons.get(category);
            if (category.equals(selectedCategory)) {
                btn.setBackground(ColorConstants.SELECTED); // 선택 시 컬러
            } else {
                btn.setBackground(ColorConstants.ACCENT); // 메인 컬러
            }
        }
    }

    /**
     * 지정된 카테고리의 장소 목록을 첫 페이지부터 로드합니다.
     *
     * @param category 로드할 장소의 카테고리 ("관광지", "맛집" 등).
     */
    private void loadPlaces(String category) {
        loadPlaces(category, 1);
    }

    /**
     * 지정된 카테고리와 페이지 번호에 해당하는 장소 목록을 데이터베이스에서 가져와 화면에 표시합니다.
     *
     * @param category 로드할 장소의 카테고리.
     * @param page     로드할 페이지 번호.
     */
    private void loadPlaces(String category, int page) {
        this.selectedCategory = category;
        updateCategoryButtonColors();
        this.currentPage = page;
        this.currentSearchQuery = null; // 카테고리 로드 시 검색어 초기화

        gridPanel.removeAll();
        
        // 카테고리에 따라 페이지 크기 및 레이아웃 변경
        if (category.equals("맛집") || category.equals("숙소")) {
            this.pageSize = 10;
            gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
        } else {
            this.pageSize = 24;
            gridPanel.setLayout(new GridLayout(0, 5, 30, 30));
        }

        int totalItems = placeDAO.getTotalPlaceCount(category);
        totalPages = (int) Math.ceil((double) totalItems / pageSize);

        List<Object> places = placeDAO.getPlacesByCategory(category, currentPage, pageSize);

        if (places.isEmpty()) {
            gridPanel.add(new JLabel("데이터가 없습니다."));
        } else {
            PlaceCard.ViewType viewType = (category.equals("맛집") || category.equals("숙소")) ? PlaceCard.ViewType.LIST : PlaceCard.ViewType.GRID;
            
            JPanel contentPanel = new JPanel();
            if(viewType == PlaceCard.ViewType.LIST) {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBackground(Color.WHITE);
            } else {
                contentPanel.setLayout(new GridLayout(0, 5, 30, 30));
                contentPanel.setBackground(Color.WHITE);
            }

            for (Object place : places) {
                String placeId = "";
                String placeName = "";
                String thumbnail = "";
                if (place instanceof Tourist) {
                    placeId = ((Tourist) place).getT_id();
                    placeName = ((Tourist) place).getT_place();
                    thumbnail = ((Tourist) place).getT_thum();
                } else if (place instanceof Restaurant) {
                    placeId = ((Restaurant) place).getR_id();
                    placeName = ((Restaurant) place).getR_place();
                    thumbnail = ((Restaurant) place).getR_thum();
                } else if (place instanceof Lodging) {
                    placeId = ((Lodging) place).getL_id();
                    placeName = ((Lodging) place).getL_place();
                    thumbnail = ((Lodging) place).getL_thum();
                } else if (place instanceof Festival) {
                    placeId = ((Festival) place).getF_id();
                    placeName = ((Festival) place).getF_name();
                    thumbnail = ((Festival) place).getF_thum();
                }
                contentPanel.add(new PlaceCard(frame, placeId, placeName, category, thumbnail, viewType, jalnanFont));
            }
            
            if (viewType == PlaceCard.ViewType.LIST) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(Color.WHITE);
                wrapper.add(contentPanel, BorderLayout.NORTH);
                gridPanel.setLayout(new BorderLayout());
                gridPanel.add(wrapper, BorderLayout.CENTER);
            } else {
                gridPanel.setLayout(new BorderLayout());
                gridPanel.add(contentPanel, BorderLayout.NORTH);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        updatePagination();
    }

    /**
     * 검색어를 기반으로 장소 데이터를 찾아 지정된 페이지의 결과를 화면에 표시합니다.
     *
     * @param query 검색할 문자열.
     * @param page  로드할 페이지 번호.
     */
    private void loadPlacesBySearch(String query, int page) {
        this.currentSearchQuery = query;
        this.currentPage = page;
        this.pageSize = 24; // 검색 결과는 항상 24개씩 표시

        gridPanel.removeAll();

        // 선택된 카테고리에 따라 레이아웃 및 페이지 크기 결정
        PlaceCard.ViewType viewType;
        if (selectedCategory.equals("맛집") || selectedCategory.equals("숙소")) {
            this.pageSize = 10;
            gridPanel.setLayout(new BoxLayout(gridPanel, BoxLayout.Y_AXIS));
            viewType = PlaceCard.ViewType.LIST;
        } else {
            this.pageSize = 24;
            gridPanel.setLayout(new GridLayout(0, 5, 30, 30));
            viewType = PlaceCard.ViewType.GRID;
        }

        int totalItems = placeDAO.getTotalSearchCount(query, selectedCategory);
        totalPages = (int) Math.ceil((double) totalItems / pageSize);

        List<Object> places = placeDAO.searchPlaces(query, selectedCategory, currentPage, pageSize);

        if (places.isEmpty()) {
            gridPanel.add(new JLabel("검색 결과가 없습니다."));
        } else {
            JPanel contentPanel = new JPanel();
            if(viewType == PlaceCard.ViewType.LIST) {
                contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
                contentPanel.setBackground(Color.WHITE);
            } else {
                contentPanel.setLayout(new GridLayout(0, 5, 30, 30));
                contentPanel.setBackground(Color.WHITE);
            }

            for (Object place : places) {
                String placeId = "";
                String placeName = "";
                String thumbnail = "";
                if (place instanceof Tourist) {
                    placeId = ((Tourist) place).getT_id();
                    placeName = ((Tourist) place).getT_place();
                    thumbnail = ((Tourist) place).getT_thum();
                } else if (place instanceof Restaurant) {
                    placeId = ((Restaurant) place).getR_id();
                    placeName = ((Restaurant) place).getR_place();
                    thumbnail = ((Restaurant) place).getR_thum();
                } else if (place instanceof Lodging) {
                    placeId = ((Lodging) place).getL_id();
                    placeName = ((Lodging) place).getL_place();
                    thumbnail = ((Lodging) place).getL_thum();
                } else if (place instanceof Festival) {
                    placeId = ((Festival) place).getF_id();
                    placeName = ((Festival) place).getF_name();
                    thumbnail = ((Festival) place).getF_thum();
                }
                contentPanel.add(new PlaceCard(frame, placeId, placeName, selectedCategory, thumbnail, viewType, jalnanFont));
            }

            if (viewType == PlaceCard.ViewType.LIST) {
                JPanel wrapper = new JPanel(new BorderLayout());
                wrapper.setBackground(Color.WHITE);
                wrapper.add(contentPanel, BorderLayout.NORTH);
                gridPanel.setLayout(new BorderLayout());
                gridPanel.add(wrapper, BorderLayout.CENTER);
            } else {
                gridPanel.setLayout(new BorderLayout());
                gridPanel.add(contentPanel, BorderLayout.NORTH);
            }
        }

        gridPanel.revalidate();
        gridPanel.repaint();
        SwingUtilities.invokeLater(() -> scrollPane.getVerticalScrollBar().setValue(0));
        updatePagination();
    }

    /**
     * 검색 필드에 입력된 검색어로 검색을 실행합니다.
     *
     * @param query 사용자가 입력한 검색어.
     */
    public void performSearch(String query) {
        loadPlacesBySearch(query, 1);
    }

    /**
     * 현재 페이지 상태(총 페이지 수, 현재 페이지 번호)에 맞춰 페이지네이션 UI를 동적으로 생성하고 업데이트합니다.
     * 페이지 버튼, 이전/다음 버튼 등을 포함합니다.
     */
    private void updatePagination() {
        paginationPanel.removeAll();
        paginationPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));


        if (totalPages <= 1) {
            paginationPanel.revalidate();
            paginationPanel.repaint();
            return;
        }

        // 이전 버튼
        JButton prevButton = createStyledButton("<");
        if (currentPage > 1) {
            prevButton.addActionListener(e -> {
                if (currentSearchQuery != null) {
                    loadPlacesBySearch(currentSearchQuery, currentPage - 1);
                } else {
                    loadPlaces(selectedCategory, currentPage - 1);
                }
            });
        } else {
            prevButton.setEnabled(false);
            prevButton.setBackground(Color.LIGHT_GRAY);
        }
        paginationPanel.add(prevButton);
        paginationPanel.add(Box.createHorizontalStrut(5));


        // 페이지 번호 버튼
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, currentPage + 2);

        if(totalPages > 5 && currentPage > 3) {
            JButton firstButton = createStyledButton("1");
            firstButton.addActionListener(e -> {
                if (currentSearchQuery != null) loadPlacesBySearch(currentSearchQuery, 1);
                else loadPlaces(selectedCategory, 1);
            });
            paginationPanel.add(firstButton);
            paginationPanel.add(Box.createHorizontalStrut(5));
            if(currentPage > 4) {
                 paginationPanel.add(new JLabel("..."));
                 paginationPanel.add(Box.createHorizontalStrut(5));
            }
        }


        for (int i = startPage; i <= endPage; i++) {
            JButton pageButton = createStyledButton(String.valueOf(i));
            final int pageNum = i;
            if (i == currentPage) {
                pageButton.setBackground(ColorConstants.SELECTED); // 현재 페이지 강조
            }
            pageButton.addActionListener(e -> {
                if (currentSearchQuery != null) {
                    loadPlacesBySearch(currentSearchQuery, pageNum);
                } else {
                    loadPlaces(selectedCategory, pageNum);
                }
            });
            paginationPanel.add(pageButton);
            paginationPanel.add(Box.createHorizontalStrut(5));
        }

        if(totalPages > 5 && currentPage < totalPages - 2) {
            if(currentPage < totalPages - 3) {
                paginationPanel.add(new JLabel("..."));
                paginationPanel.add(Box.createHorizontalStrut(5));
            }
            JButton lastButton = createStyledButton(String.valueOf(totalPages));
            lastButton.addActionListener(e -> {
                if (currentSearchQuery != null) loadPlacesBySearch(currentSearchQuery, totalPages);
                else loadPlaces(selectedCategory, totalPages);
            });
            paginationPanel.add(lastButton);
            paginationPanel.add(Box.createHorizontalStrut(5));
        }


        // 다음 버튼
        JButton nextButton = createStyledButton(">");
        if (currentPage < totalPages) {
            nextButton.addActionListener(e -> {
                if (currentSearchQuery != null) {
                    loadPlacesBySearch(currentSearchQuery, currentPage + 1);
                } else {
                    loadPlaces(selectedCategory, currentPage + 1);
                }
            });
        } else {
            nextButton.setEnabled(false);
            nextButton.setBackground(Color.LIGHT_GRAY);
        }
        paginationPanel.add(nextButton);


        paginationPanel.revalidate();
        paginationPanel.repaint();
    }

    /**
     * 페이지네이션 바에 사용될 스타일이 적용된 JButton을 생성합니다.
     *
     * @param text 버튼에 표시될 텍스트.
     * @return 스타일이 적용된 JButton 객체.
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(jalnanFont.deriveFont(Font.PLAIN, 12f));
        button.setBackground(ColorConstants.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        //button.setBorderPainted(false);
        return button;
    }

    /**
     * 모서리가 둥근 버튼을 생성하기 위한 커스텀 JButton 클래스입니다.
     */
    class RoundedButton extends JButton {
        /**
         * RoundedButton의 새 인스턴스를 생성합니다.
         *
         * @param text 버튼에 표시될 텍스트.
         */
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false); // 기본 배경 채우기 비활성화
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 마우스가 올라가 있거나 눌렸을 때 색상 변경 (선택적)
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            
            // 둥근 사각형 그리기
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15); // 15는 둥근 정도
            
            // 텍스트 그리기
            super.paintComponent(g2);
            g2.dispose();
        }
    }
}
