package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.util.ApiHandler;
import busantravel.util.AutocompletePrediction;
import busantravel.util.ImageCache;
import busantravel.util.PlaceInfo;
import busantravel.util.PlaceReview;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

public class PlaceCard extends JPanel {
    private PlaceDAO placeDAO;
    private ApiHandler apiHandler;
    private int currentZoom = 14;
    private static final String API_KEY = "AIzaSyCbEjKNhNT9tjRh0KqCil4sUMlF06Dfu5Y";

    public enum ViewType {
        GRID,
        LIST
    }

    public PlaceCard(JFrame frame, String placeName, String category, String thumbnail, ViewType viewType) {
        this.placeDAO = new PlaceDAO();
        this.apiHandler = new ApiHandler();

        if (viewType == ViewType.GRID) {
            setupGridView(frame, placeName, category, thumbnail);
        } else {
            setupListView(frame, placeName, category, thumbnail);
        }
    }
    
    private void setupGridView(JFrame frame, String placeName, String category, String thumbnail) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        setBackground(Color.WHITE);

        setPreferredSize(new Dimension(180, 180));
        setMinimumSize(new Dimension(180, 180));
        setMaximumSize(new Dimension(180, 180));

        JLabel imageLabel = new JLabel();
        imageLabel.setBackground(new Color(230, 240, 250));
        imageLabel.setOpaque(true);
        imageLabel.setPreferredSize(new Dimension(180, 120));
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        String imageUrl = thumbnail;
        ImageIcon cachedImage = null;

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            cachedImage = ImageCache.getInstance().getImage(imageUrl);
        }

        if (cachedImage != null) {
            Image scaledImage = cachedImage.getImage().getScaledInstance(180, 120, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } else if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            loadImage(imageLabel, imageUrl, placeName, 180, 120, false);
        } else {
            loadGoogleImage(imageLabel, placeName, 180, 120, false);
        }

        JLabel nameLabel = new JLabel(placeName, JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        nameLabel.setBackground(new Color(245, 245, 245));
        nameLabel.setOpaque(true);

        add(imageLabel, BorderLayout.CENTER);
        add(nameLabel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlaceDetailDialog(frame, placeName, category);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(new Color(222, 146, 124), 2));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            }
        });
    }

    private void setupListView(JFrame frame, String placeName, String category, String thumbnail) {
        setLayout(new BorderLayout(15, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); // 높이 수정

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(120, 120)); // 이미지 크기 수정
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setBackground(new Color(230, 240, 250));
        imageLabel.setOpaque(true);

        String imageUrl = thumbnail;
        ImageIcon cachedImage = null;

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            cachedImage = ImageCache.getInstance().getImage(imageUrl);
        }

        if (cachedImage != null) {
            imageLabel.setIcon(new ImageIcon(cachedImage.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH)));
        } else if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            loadImage(imageLabel, imageUrl, placeName, 120, 120, true);
        } else {
            loadGoogleImage(imageLabel, placeName, 120, 120, true);
        }
        add(imageLabel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(placeName);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea("설명 로딩 중...");
        descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        descriptionArea.setForeground(Color.DARK_GRAY);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setLineWrap(true);
        descriptionArea.setEditable(false);
        descriptionArea.setFocusable(false);
        descriptionArea.setOpaque(false);
        descriptionArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        PlaceDetailInfo detailInfo = placeDAO.getPlaceDetail(placeName, category);
        String descriptionText = "이 장소에 대한 설명을 찾을 수 없습니다.";
        if (detailInfo != null && detailInfo.getDescription() != null && !detailInfo.getDescription().isEmpty()) {
            descriptionText = detailInfo.getDescription();
        }
        descriptionArea.setText(descriptionText);

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(8));
        infoPanel.add(descriptionArea);
        infoPanel.add(Box.createVerticalGlue());

        add(infoPanel, BorderLayout.CENTER);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlaceDetailDialog(frame, placeName, category);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(245, 245, 245));
                infoPanel.setBackground(new Color(245, 245, 245));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
                infoPanel.setBackground(Color.WHITE);
            }
        });
    }

    private void showPlaceDetailDialog(JFrame frame, String placeName, String category) {
        JDialog detailDialog = new JDialog(frame, placeName + " 상세 정보", true);
        detailDialog.setSize(1200, 800);
        detailDialog.setLocationRelativeTo(frame);
        detailDialog.setLayout(new BorderLayout());

        PlaceDetailInfo detailInfo = placeDAO.getPlaceDetail(placeName, category);

        if ("맛집".equals(category) && detailInfo != null) {
            // 맛집 상세 정보 레이아웃
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(600);
            splitPane.setResizeWeight(0.5);
            splitPane.setEnabled(false); // 사용자가 크기 조절 못하게
            splitPane.setBorder(null);

            // 왼쪽 패널 (사진, 설명)
            JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
            leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            leftPanel.setBackground(Color.WHITE);

            JLabel imageLabel = new JLabel();
            imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            imageLabel.setVerticalAlignment(SwingConstants.CENTER);
            imageLabel.setPreferredSize(new Dimension(560, 420));
            imageLabel.setBackground(new Color(230, 240, 250));
            imageLabel.setOpaque(true);
            
            if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                loadImage(imageLabel, detailInfo.getImagePath(), placeName, 560, 420, false);
            } else {
                loadGoogleImage(imageLabel, placeName, 560, 420, false);
            }

            JTextArea descriptionArea = new JTextArea(detailInfo.getDescription());
            descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBorder(BorderFactory.createTitledBorder("식당 설명"));
            
            JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
            descriptionScrollPane.setPreferredSize(new Dimension(560, 200));

            leftPanel.add(imageLabel, BorderLayout.CENTER);
            leftPanel.add(descriptionScrollPane, BorderLayout.SOUTH);

            // 오른쪽 패널 (운영 시간, 주소, 메뉴)
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rightPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(placeName);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            rightPanel.add(titleLabel);
            rightPanel.add(Box.createVerticalStrut(20));

            // 운영 시간
            JTextArea restArea = new JTextArea(detailInfo.getRestDay());
            restArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            restArea.setWrapStyleWord(true);
            restArea.setLineWrap(true);
            restArea.setEditable(false);
            restArea.setFocusable(false);
            restArea.setBorder(BorderFactory.createTitledBorder("운영 시간"));
            rightPanel.add(restArea);
            rightPanel.add(Box.createVerticalStrut(10));

            // 주소
            JTextArea addrArea = new JTextArea(detailInfo.getAddress());
            addrArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            addrArea.setWrapStyleWord(true);
            addrArea.setLineWrap(true);
            addrArea.setEditable(false);
            addrArea.setFocusable(false);
            addrArea.setBorder(BorderFactory.createTitledBorder("주소"));
            rightPanel.add(addrArea);
            rightPanel.add(Box.createVerticalStrut(10));

            // 전화번호
            JTextArea phoneArea = new JTextArea(detailInfo.getPhone());
            phoneArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            phoneArea.setWrapStyleWord(true);
            phoneArea.setLineWrap(true);
            phoneArea.setEditable(false);
            phoneArea.setFocusable(false);
            phoneArea.setBorder(BorderFactory.createTitledBorder("전화번호"));
            rightPanel.add(phoneArea);
            rightPanel.add(Box.createVerticalStrut(10));

            // 메뉴
            JTextArea menuArea = new JTextArea(detailInfo.getMenu());
            menuArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            menuArea.setWrapStyleWord(true);
            menuArea.setLineWrap(true);
            menuArea.setEditable(false);
            menuArea.setFocusable(false);
            menuArea.setBorder(BorderFactory.createTitledBorder("메뉴"));
            
            JScrollPane menuScrollPane = new JScrollPane(menuArea);
            menuScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            
            rightPanel.add(menuScrollPane);
            
            rightPanel.add(Box.createVerticalGlue()); // 남은 공간 채우기

            splitPane.setLeftComponent(leftPanel);
            splitPane.setRightComponent(rightPanel);
            
            detailDialog.add(splitPane, BorderLayout.CENTER);

        } else {
            // 기존 상세 정보 레이아웃 (관광지 등)
            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);

            JPanel detailPanel = new JPanel();
            detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
            detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            detailPanel.setBackground(Color.WHITE);

            String descriptionText = "해당 장소의 정보를 찾을 수 없습니다.";

            JLabel detailImageLabel = new JLabel();
            detailImageLabel.setBackground(new Color(230, 240, 250));
            detailImageLabel.setOpaque(true);
            detailImageLabel.setPreferredSize(new Dimension(500, 400));
            detailImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            detailImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            detailImageLabel.setVerticalAlignment(SwingConstants.CENTER);

            if (detailInfo != null) {
                descriptionText = detailInfo.getDescription();
                URL localImageURL = null;
                if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                    localImageURL = getClass().getResource("/busantravel/ui/" + detailInfo.getImagePath());
                }

                if (localImageURL != null) {
                    ImageIcon detailImageIcon = new ImageIcon(localImageURL);
                    Image originalDetailImage = detailImageIcon.getImage();
                    int imgWidth = originalDetailImage.getWidth(null);
                    int imgHeight = originalDetailImage.getHeight(null);
                    double scaleFactor = (double) 300 / imgHeight;
                    Image scaledDetailImage = originalDetailImage.getScaledInstance((int) (imgWidth * scaleFactor), 300, Image.SCALE_SMOOTH);
                    detailImageLabel.setIcon(new ImageIcon(scaledDetailImage));
                    detailImageLabel.setText(null);
                } else if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                    loadImage(detailImageLabel, detailInfo.getImagePath(), placeName, 500, 400, false);
                } else {
                    loadGoogleImage(detailImageLabel, placeName, 500, 400, false);
                }
            } else {
                detailImageLabel.setText("장소 정보를 찾을 수 없습니다.");
            }

            JLabel title = new JLabel(placeName);
            title.setFont(new Font("맑은 고딕", Font.BOLD, 22));
            title.setAlignmentX(Component.CENTER_ALIGNMENT);
            title.setForeground(new Color(50, 50, 50));

            JTextArea description = new JTextArea(descriptionText);
            description.setLineWrap(true);
            description.setWrapStyleWord(true);
            description.setEditable(false);
            description.setBackground(detailPanel.getBackground());
            description.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            description.setForeground(new Color(80, 80, 80));
            
            JScrollPane descriptionScrollPane = new JScrollPane(description);
            descriptionScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            descriptionScrollPane.setBorder(null);
            descriptionScrollPane.setAlignmentX(Component.CENTER_ALIGNMENT);

            JButton saveBtn = new JButton("저장하기");
            saveBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
            saveBtn.setBackground(new Color(222, 146, 124));
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);
            saveBtn.setBorderPainted(false);
            saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
            saveBtn.setPreferredSize(new Dimension(120, 35));

            saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(detailDialog, placeName + " 저장 완료! 저장된 장소 탭에서 확인하세요."));

            detailPanel.add(detailImageLabel);
            detailPanel.add(Box.createVerticalStrut(15));
            detailPanel.add(title);
            detailPanel.add(Box.createVerticalStrut(10));
            detailPanel.add(descriptionScrollPane);
            detailPanel.add(Box.createVerticalStrut(25));
            detailPanel.add(saveBtn);

            mainPanel.add(detailPanel, BorderLayout.CENTER);

            if (("관광지".equals(category) || "축제".equals(category)) && detailInfo != null) {
                JPanel infoPanel = new JPanel();
                infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                infoPanel.setBackground(Color.WHITE);
                infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

                if ("축제".equals(category)) {
                    JLabel periodLabel = new JLabel("<html><body style='width: 200px'>축제 기간: " + detailInfo.getRestDay() + "</body></html>");
                    JLabel traLabel = new JLabel("<html><body style='width: 200px'>교통편: " + detailInfo.getTransportation() + "</body></html>");
                    JLabel addrLabel = new JLabel("<html><body style='width: 200px'>주소: " + detailInfo.getAddress() + "</body></html>");
                    infoPanel.add(periodLabel);
                    infoPanel.add(Box.createVerticalStrut(10));
                    infoPanel.add(traLabel);
                    infoPanel.add(Box.createVerticalStrut(10));
                    infoPanel.add(addrLabel);
                } else { // 관광지
                    JLabel traLabel = new JLabel("<html><body style='width: 200px'>교통 정보: " + detailInfo.getTransportation() + "</body></html>");
                    JLabel restLabel = new JLabel("휴무일: " + detailInfo.getRestDay());
                    JLabel addrLabel = new JLabel("<html><body style='width: 200px'>주소: " + detailInfo.getAddress() + "</body></html>");
                    infoPanel.add(traLabel);
                    infoPanel.add(Box.createVerticalStrut(10));
                    infoPanel.add(restLabel);
                    infoPanel.add(Box.createVerticalStrut(10));
                    infoPanel.add(addrLabel);
                }
                infoPanel.add(Box.createVerticalStrut(10));

                JLabel mapLabel = new JLabel();
                mapLabel.setPreferredSize(new Dimension(300, 300));
                infoPanel.add(mapLabel);

                JButton zoomInButton = new JButton("+");
                JButton zoomOutButton = new JButton("-");
                JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                zoomPanel.setBackground(Color.WHITE);
                zoomPanel.add(zoomInButton);
                zoomPanel.add(zoomOutButton);
                infoPanel.add(zoomPanel);

                JPanel reviewsContainer = new JPanel(new BorderLayout());
                reviewsContainer.setBackground(Color.WHITE);
                reviewsContainer.setBorder(BorderFactory.createTitledBorder("리뷰"));
                
                JPanel reviewsPanel = new JPanel();
                reviewsPanel.setLayout(new BoxLayout(reviewsPanel, BoxLayout.Y_AXIS));
                reviewsPanel.setBackground(Color.WHITE);
                
                JScrollPane reviewScrollPane = new JScrollPane(reviewsPanel);
                reviewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
                reviewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                reviewScrollPane.setBorder(null);
                reviewsContainer.add(reviewScrollPane, BorderLayout.CENTER);
                
                infoPanel.add(Box.createVerticalStrut(10));
                infoPanel.add(reviewsContainer);

                new SwingWorker<PlaceInfo, Void>() {
                    @Override
                    protected PlaceInfo doInBackground() throws Exception {
                        List<AutocompletePrediction> predictions = apiHandler.getAutocompletePredictions(placeName, API_KEY);
                        if (!predictions.isEmpty()) {
                            return apiHandler.getPlaceDetailsById(predictions.get(0).getPlaceId(), API_KEY);
                        }
                        return null;
                    }

                    @Override
                    protected void done() {
                        try {
                            PlaceInfo placeInfo = get();
                            if (placeInfo != null) {
                                updateMap(mapLabel, placeInfo, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                
                                reviewsPanel.removeAll();
                                if (placeInfo.getReviews() != null && !placeInfo.getReviews().isEmpty()) {
                                    for (PlaceReview review : placeInfo.getReviews()) {
                                        JPanel reviewPanel = new JPanel();
                                        reviewPanel.setLayout(new BoxLayout(reviewPanel, BoxLayout.Y_AXIS));
                                        reviewPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                                        reviewPanel.setBackground(new Color(248, 248, 248));
                                        
                                        JLabel authorLabel = new JLabel(review.getAuthorName() + " (" + review.getRating() + "/5)");
                                        authorLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
                                        
                                        JTextArea reviewText = new JTextArea(review.getText());
                                        reviewText.setLineWrap(true);
                                        reviewText.setWrapStyleWord(true);
                                        reviewText.setEditable(false);
                                        reviewText.setBackground(reviewPanel.getBackground());
                                        reviewText.setFont(new Font("맑은 고딕", Font.PLAIN, 11));
                                        
                                        reviewPanel.add(authorLabel);
                                        reviewPanel.add(Box.createVerticalStrut(3));
                                        reviewPanel.add(reviewText);
                                        
                                        reviewsPanel.add(reviewPanel);
                                        reviewsPanel.add(Box.createVerticalStrut(5));
                                    }
                                } else {
                                    reviewsPanel.add(new JLabel("리뷰가 없습니다."));
                                }
                                reviewsPanel.revalidate();
                                reviewsPanel.repaint();

                                for(ActionListener al : zoomInButton.getActionListeners()) { zoomInButton.removeActionListener(al); }
                                for(ActionListener al : zoomOutButton.getActionListeners()) { zoomOutButton.removeActionListener(al); }

                                zoomInButton.addActionListener(e -> {
                                    if (currentZoom < 20) {
                                        currentZoom++;
                                        updateMap(mapLabel, placeInfo, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                    }
                                });

                                zoomOutButton.addActionListener(e -> {
                                    if (currentZoom > 1) {
                                        currentZoom--;
                                        updateMap(mapLabel, placeInfo, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                    }
                                });

                            } else {
                                mapLabel.setText("지도 정보를 가져올 수 없습니다.");
                                reviewsPanel.add(new JLabel("리뷰 정보를 가져올 수 없습니다."));
                                reviewsPanel.revalidate();
                                reviewsPanel.repaint();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mapLabel.setText("정보 로드 중 오류 발생");
                            reviewsPanel.add(new JLabel("정보 로드 중 오류 발생"));
                            reviewsPanel.revalidate();
                            reviewsPanel.repaint();
                        }
                    }
                }.execute();

                mainPanel.add(infoPanel, BorderLayout.EAST);
            }
            detailDialog.add(mainPanel, BorderLayout.CENTER);
        }
        detailDialog.setVisible(true);
    }

    private void updateMap(JLabel mapLabel, PlaceInfo placeInfo, int zoom, int width, int height) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                if (placeInfo != null) {
                    String staticMapUrl = apiHandler.getStaticMapUrl(placeInfo.getLatitude(), placeInfo.getLongitude(), zoom, width, height, API_KEY);
                    Image image = ImageIO.read(new URL(staticMapUrl));
                    if (image != null) {
                        return new ImageIcon(image);
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon imageIcon = get();
                    if (imageIcon != null) {
                        mapLabel.setIcon(imageIcon);
                        mapLabel.setText(null);
                    } else {
                        mapLabel.setText("지도를 불러올 수 없습니다.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    mapLabel.setText("지도 로드 중 오류 발생");
                }
            }
        }.execute();
    }

    private void loadImage(JLabel label, String imagePath, String placeName, int width, int height, boolean isList) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(imagePath);
                Image image = ImageIO.read(url);
                if (image != null) {
                    Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                    ImageIcon imageIcon = new ImageIcon(scaledImage);
                    if (!isList) { // 그리드 뷰일 때만 캐시 (리스트 뷰는 썸네일이므로)
                        ImageCache.getInstance().putImage(imagePath, new ImageIcon(image));
                    }
                    return imageIcon;
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon imageIcon = get();
                    if (imageIcon != null) {
                        label.setIcon(imageIcon);
                    } else {
                        label.setText("이미지 없음");
                    }
                } catch (Exception e) {
                    label.setText("이미지 로드 실패");
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadGoogleImage(JLabel label, String placeName, int width, int height, boolean isList) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                List<AutocompletePrediction> predictions = apiHandler.getAutocompletePredictions(placeName, API_KEY);
                if (!predictions.isEmpty()) {
                    String placeId = predictions.get(0).getPlaceId();
                    PlaceInfo placeInfo = apiHandler.getPlaceDetailsById(placeId, API_KEY);
                    if (placeInfo != null && placeInfo.getPhotoReference() != null) {
                        String imageUrl = apiHandler.getPhotoUrl(placeInfo.getPhotoReference(), 400, API_KEY);
                        
                        ImageIcon cachedIcon = ImageCache.getInstance().getImage(imageUrl);
                        if (cachedIcon != null) {
                            return new ImageIcon(cachedIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
                        }

                        Image image = ImageIO.read(new URL(imageUrl));
                        if (image != null) {
                            if (!isList) {
                                ImageCache.getInstance().putImage(imageUrl, new ImageIcon(image));
                            }
                            return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
                        }
                    }
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    ImageIcon imageIcon = get();
                    if (imageIcon != null) {
                        label.setIcon(imageIcon);
                    } else {
                        label.setText("이미지 없음");
                    }
                } catch (Exception e) {
                    label.setText("이미지 로드 실패");
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
