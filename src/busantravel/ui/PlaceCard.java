package busantravel.ui;

import busantravel.dao.PlaceDAO;
import busantravel.dao.PlaceDAO;
import busantravel.util.Coordinate;
import busantravel.util.ImageCache;
import busantravel.util.PlaceInfo;
import busantravel.util.StaticMapGenerator;
import busantravel.util.TmapApiHandler;

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
    private TmapApiHandler tmapApiHandler;
    private StaticMapGenerator staticMapGenerator;
    private int currentZoom = 14;
    private static final String API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg"; // TMAP API Key

    public enum ViewType {
        GRID,
        LIST
    }

    public PlaceCard(JFrame frame, String placeName, String category, String thumbnail, ViewType viewType) {
        this.placeDAO = new PlaceDAO();
        this.tmapApiHandler = new TmapApiHandler(API_KEY);
        this.staticMapGenerator = new StaticMapGenerator(API_KEY);

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
            // TMAP에서는 장소 이름만으로 이미지를 가져오는 직접적인 API가 없으므로,
            // 이미지가 없는 경우 기본 아이콘이나 문구를 표시합니다.
            imageLabel.setText("이미지 없음");
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
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));

        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(120, 120));
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
            imageLabel.setText("이미지 없음");
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
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(600);
            splitPane.setResizeWeight(0.5);
            splitPane.setEnabled(false);
            splitPane.setBorder(null);

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
                imageLabel.setText("이미지 없음");
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

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rightPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(placeName);
            titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            
            rightPanel.add(titleLabel);
            rightPanel.add(Box.createVerticalStrut(20));

            JTextArea restArea = new JTextArea(detailInfo.getRestDay());
            restArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            restArea.setWrapStyleWord(true);
            restArea.setLineWrap(true);
            restArea.setEditable(false);
            restArea.setFocusable(false);
            restArea.setBorder(BorderFactory.createTitledBorder("운영 시간"));
            rightPanel.add(restArea);
            rightPanel.add(Box.createVerticalStrut(10));

            JTextArea addrArea = new JTextArea(detailInfo.getAddress());
            addrArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            addrArea.setWrapStyleWord(true);
            addrArea.setLineWrap(true);
            addrArea.setEditable(false);
            addrArea.setFocusable(false);
            addrArea.setBorder(BorderFactory.createTitledBorder("주소"));
            rightPanel.add(addrArea);
            rightPanel.add(Box.createVerticalStrut(10));

            JTextArea phoneArea = new JTextArea(detailInfo.getPhone());
            phoneArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            phoneArea.setWrapStyleWord(true);
            phoneArea.setLineWrap(true);
            phoneArea.setEditable(false);
            phoneArea.setFocusable(false);
            phoneArea.setBorder(BorderFactory.createTitledBorder("전화번호"));
            rightPanel.add(phoneArea);
            rightPanel.add(Box.createVerticalStrut(10));

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
            
            rightPanel.add(Box.createVerticalGlue());

            splitPane.setLeftComponent(leftPanel);
            splitPane.setRightComponent(rightPanel);
            
            detailDialog.add(splitPane, BorderLayout.CENTER);

        } else {
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
                    detailImageLabel.setText("이미지 없음");
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

                // 리뷰 패널 제거
                // JPanel reviewsContainer = new JPanel(new BorderLayout());
                // ...

                new SwingWorker<Coordinate, Void>() {
                    @Override
                    protected Coordinate doInBackground() throws Exception {
                        // 데이터베이스에서 가져온 주소로 좌표 검색
                        return tmapApiHandler.getCoordinates(detailInfo.getAddress());
                    }

                    @Override
                    protected void done() {
                        try {
                            Coordinate coordinate = get();
                            if (coordinate != null) {
                                updateMap(mapLabel, coordinate, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                
                                for(ActionListener al : zoomInButton.getActionListeners()) { zoomInButton.removeActionListener(al); }
                                for(ActionListener al : zoomOutButton.getActionListeners()) { zoomOutButton.removeActionListener(al); }

                                zoomInButton.addActionListener(e -> {
                                    if (currentZoom < 20) {
                                        currentZoom++;
                                        updateMap(mapLabel, coordinate, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                    }
                                });

                                zoomOutButton.addActionListener(e -> {
                                    if (currentZoom > 1) {
                                        currentZoom--;
                                        updateMap(mapLabel, coordinate, currentZoom, mapLabel.getPreferredSize().width, mapLabel.getPreferredSize().height);
                                    }
                                });

                            } else {
                                mapLabel.setText("지도 정보를 가져올 수 없습니다.");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            mapLabel.setText("정보 로드 중 오류 발생");
                        }
                    }
                }.execute();

                mainPanel.add(infoPanel, BorderLayout.EAST);
            }
            detailDialog.add(mainPanel, BorderLayout.CENTER);
        }
        detailDialog.setVisible(true);
    }

    private void updateMap(JLabel mapLabel, Coordinate coordinate, int zoom, int width, int height) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                if (coordinate != null) {
                    String staticMapUrl = staticMapGenerator.generateMapWithPinUrl(coordinate, width, height, zoom);
                    if (staticMapUrl != null) {
                        Image image = ImageIO.read(new URL(staticMapUrl));
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
                    if (!isList) {
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
        // TMAP API로는 대체할 수 없으므로, 이 메소드는 비워두거나 기본 이미지를 설정합니다.
        label.setText("이미지 없음");
    }
}
