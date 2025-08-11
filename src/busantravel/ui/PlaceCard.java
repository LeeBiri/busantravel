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

    public PlaceCard(JFrame frame, String placeName, String category, String thumbnail) {
        this.placeDAO = new PlaceDAO();
        this.apiHandler = new ApiHandler();
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // 기본 테두리
        setBackground(Color.WHITE); // 배경색

        // 카드 크기 고정 (요청 반영)
        setPreferredSize(new Dimension(180, 180));
        setMinimumSize(new Dimension(180, 180));
        setMaximumSize(new Dimension(180, 180));

        // 이미지 표시 영역 (현재는 단색 배경)
        JLabel imageLabel = new JLabel();
        imageLabel.setBackground(new Color(230, 240, 250)); // 이미지 없을 시 배경색
        imageLabel.setOpaque(true); // 배경색이 보이도록 설정
        imageLabel.setPreferredSize(new Dimension(180, 120)); // 이미지 영역 크기
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);

        ImageIcon cachedImage = ImageCache.getInstance().getImage(placeName);
        if (cachedImage != null) {
            imageLabel.setIcon(cachedImage);
        } else if (thumbnail != null && !thumbnail.isEmpty() && thumbnail.startsWith("http")) {
            loadImage(imageLabel, thumbnail, placeName);
        } else {
            new SwingWorker<ImageIcon, Void>() {
                @Override
                protected ImageIcon doInBackground() throws Exception {
                    List<AutocompletePrediction> predictions = apiHandler.getAutocompletePredictions(placeName, API_KEY);
                    if (!predictions.isEmpty()) {
                        String placeId = predictions.get(0).getPlaceId();
                        PlaceInfo placeInfo = apiHandler.getPlaceDetailsById(placeId, API_KEY);
                        if (placeInfo != null && placeInfo.getPhotoReference() != null) {
                            String photoReference = placeInfo.getPhotoReference();
                            String imageUrl = apiHandler.getPhotoUrl(photoReference, 400, API_KEY);
                            Image image = ImageIO.read(new URL(imageUrl));
                            if (image != null) {
                                ImageIcon imageIcon = new ImageIcon(image);
                                ImageCache.getInstance().putImage(placeName, imageIcon);
                                return imageIcon;
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
                            imageLabel.setIcon(imageIcon);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.execute();
        }

        // 장소 이름 라벨
        JLabel nameLabel = new JLabel(placeName, JLabel.CENTER);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5)); // 패딩 추가
        nameLabel.setBackground(new Color(245, 245, 245)); // 이름 라벨 배경색
        nameLabel.setOpaque(true); // 배경색이 보이도록 설정

        add(imageLabel, BorderLayout.CENTER);
        add(nameLabel, BorderLayout.SOUTH);

        // 클릭 시 상세 정보 팝업 및 호버 효과
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlaceDetailDialog(frame, placeName, category);
            }
            @Override
            public void mouseEntered(MouseEvent e) { // 마우스가 카드 위에 올라갔을 때
                setBorder(BorderFactory.createLineBorder(new Color(222, 146, 124), 2)); // 하이라이트 테두리
            }
            @Override
            public void mouseExited(MouseEvent e) { // 마우스가 카드에서 벗어났을 때
                setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // 원래 테두리로 복원
            }
        });
    }

    private void showPlaceDetailDialog(JFrame frame, String placeName, String category) {
        JDialog detailDialog = new JDialog(frame, placeName + " 상세 정보", true);
        detailDialog.setSize(1200, 800);
        detailDialog.setLocationRelativeTo(frame);
        detailDialog.setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        detailPanel.setBackground(Color.WHITE);

        // PlaceDAO에서 장소 상세 정보 불러오기
        PlaceDetailInfo detailInfo = placeDAO.getPlaceDetail(placeName, category);
        String descriptionText = "해당 장소의 정보를 찾을 수 없습니다.";

        // 상세 정보 이미지 영역
        JLabel detailImageLabel = new JLabel();
        detailImageLabel.setBackground(new Color(230, 240, 250)); // 기본 배경색
        detailImageLabel.setOpaque(true);
        detailImageLabel.setPreferredSize(new Dimension(500, 400));
        detailImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        detailImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        detailImageLabel.setVerticalAlignment(SwingConstants.CENTER);

        if (detailInfo != null) {
            descriptionText = detailInfo.getDescription();
            URL localImageURL = null;
            if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                localImageURL = getClass().getResource("/BusanHow1/" + detailInfo.getImagePath());
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
                loadImage(detailImageLabel, detailInfo.getImagePath(), placeName);
            } else {
                detailImageLabel.setText("이미지 로딩 중...");
                new SwingWorker<ImageIcon, Void>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        List<AutocompletePrediction> predictions = apiHandler.getAutocompletePredictions(placeName, API_KEY);
                        if (!predictions.isEmpty()) {
                            PlaceInfo placeInfo = apiHandler.getPlaceDetailsById(predictions.get(0).getPlaceId(), API_KEY);
                            if (placeInfo != null && placeInfo.getPhotoReference() != null) {
                                String imageUrl = apiHandler.getPhotoUrl(placeInfo.getPhotoReference(), 400, API_KEY);
                                Image image = ImageIO.read(new URL(imageUrl));
                                if (image != null) {
                                    return new ImageIcon(image);
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
                                Image originalDetailImage = imageIcon.getImage();
                                int imgWidth = originalDetailImage.getWidth(null);
                                int imgHeight = originalDetailImage.getHeight(null);
                                int targetWidth = detailImageLabel.getPreferredSize().width;
                                int targetHeight = detailImageLabel.getPreferredSize().height;
                                if (imgWidth > 0 && imgHeight > 0) {
                                    double scaleFactor = (double) 300 / imgHeight;
                                    Image scaledDetailImage = originalDetailImage.getScaledInstance((int) (imgWidth * scaleFactor), 300, Image.SCALE_SMOOTH);
                                    detailImageLabel.setIcon(new ImageIcon(scaledDetailImage));
                                    detailImageLabel.setText(null);
                                }
                            } else {
                                detailImageLabel.setText("이미지를 찾을 수 없습니다.");
                                detailImageLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                                detailImageLabel.setForeground(Color.RED);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            detailImageLabel.setText("이미지 로드 중 오류 발생");
                        }
                    }
                }.execute();
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

        if ("관광지".equals(category) && detailInfo != null) {
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));

            JLabel traLabel = new JLabel("<html><body style='width: 200px'>교통 정보: " + detailInfo.getTransportation() + "</body></html>");
            JLabel restLabel = new JLabel("휴무일: " + detailInfo.getRestDay());
            JLabel addrLabel = new JLabel("<html><body style='width: 200px'>주소: " + detailInfo.getAddress() + "</body></html>");

            infoPanel.add(traLabel);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(restLabel);
            infoPanel.add(Box.createVerticalStrut(10));
            infoPanel.add(addrLabel);

            infoPanel.add(Box.createVerticalStrut(10));

            JLabel mapLabel = new JLabel();
            mapLabel.setPreferredSize(new Dimension(300, 300));
            infoPanel.add(mapLabel);

            // Zoom buttons
            JButton zoomInButton = new JButton("+");
            JButton zoomOutButton = new JButton("-");
            JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            zoomPanel.setBackground(Color.WHITE);
            zoomPanel.add(zoomInButton);
            zoomPanel.add(zoomOutButton);
            infoPanel.add(zoomPanel);

            // Reviews Panel
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


            // SwingWorker to fetch PlaceInfo (which includes map and reviews)
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
                            // Update Map
                            updateMap(mapLabel, placeInfo, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                            
                            // Update Reviews
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

                            // Add ActionListeners for zoom buttons here, after placeInfo is fetched
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

    private void loadImage(JLabel label, String imagePath, String placeName) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                URL url = new URL(imagePath);
                Image image = ImageIO.read(url);
                if (image != null) {
                    ImageIcon imageIcon = new ImageIcon(image);
                    ImageCache.getInstance().putImage(placeName, imageIcon);
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
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }
}
