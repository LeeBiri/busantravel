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
import busantravel.ui.ColorConstants;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.List;

public class PlaceCard extends JPanel {
    private PlaceDAO placeDAO;
    private Font jalnanFont;
    private TmapApiHandler tmapApiHandler;
    private StaticMapGenerator staticMapGenerator;
    private int currentZoom = 14;
    private static final String API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg"; // TMAP API Key

    public enum ViewType {
        GRID,
        LIST
    }

    public PlaceCard(JFrame frame, String placeName, String category, String thumbnail, ViewType viewType, Font customFont) {
        this.placeDAO = new PlaceDAO();
        this.tmapApiHandler = new TmapApiHandler(API_KEY);
        this.staticMapGenerator = new StaticMapGenerator(API_KEY);
        this.jalnanFont = customFont; // 외부에서 폰트 받아오기
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

        ScaledImagePanel imagePanel = new ScaledImagePanel();
        imagePanel.setPreferredSize(new Dimension(180, 120));

        String imageUrl = thumbnail;
        ImageIcon cachedImage = null;

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            cachedImage = ImageCache.getInstance().getImage(imageUrl);
        }

        if (cachedImage != null) {
            imagePanel.setImage(cachedImage.getImage());
        } else if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            loadImage(imagePanel, imageUrl, placeName, 180, 120, false);
        } else {
            // 이미지가 없는 경우
        }

        JLabel nameLabel = new JLabel(placeName, JLabel.CENTER);
        nameLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        nameLabel.setBackground(new Color(245, 245, 245));
        nameLabel.setOpaque(true);

        add(imagePanel, BorderLayout.CENTER);
        add(nameLabel, BorderLayout.SOUTH);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlaceDetailDialog(frame, placeName, category);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorder(BorderFactory.createLineBorder(ColorConstants.ACCENT, 2));
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

        ScaledImagePanel imagePanel = new ScaledImagePanel();
        imagePanel.setPreferredSize(new Dimension(120, 120));

        String imageUrl = thumbnail;
        ImageIcon cachedImage = null;

        if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            cachedImage = ImageCache.getInstance().getImage(imageUrl);
        }

        if (cachedImage != null) {
            imagePanel.setImage(cachedImage.getImage());
        } else if (imageUrl != null && !imageUrl.isEmpty() && imageUrl.startsWith("http")) {
            loadImage(imagePanel, imageUrl, placeName, 120, 120, true);
        } else {
            // 이미지가 없는 경우
        }
        add(imagePanel, BorderLayout.WEST);

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(placeName);
        nameLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
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
        detailDialog.setSize(1180, 850);
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

            ScaledImagePanel imagePanel = new ScaledImagePanel();
            imagePanel.setPreferredSize(new Dimension(560, 420));
            
            if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                loadImage(imagePanel, detailInfo.getImagePath(), placeName, 560, 420, false);
            } else {
                // 이미지가 없는 경우
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

            leftPanel.add(imagePanel, BorderLayout.CENTER);
            leftPanel.add(descriptionScrollPane, BorderLayout.SOUTH);

            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rightPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(placeName);
            titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
            titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            
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

        } else if (detailInfo != null) {
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            splitPane.setDividerLocation(600);
            splitPane.setResizeWeight(0.5);
            splitPane.setEnabled(false);
            splitPane.setBorder(null);

            // Left Panel for Image and Description
            JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
            leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            leftPanel.setBackground(Color.WHITE);

            ScaledImagePanel imagePanel = new ScaledImagePanel();
            imagePanel.setPreferredSize(new Dimension(560, 420));

            if (detailInfo.getImagePath() != null && !detailInfo.getImagePath().isEmpty()) {
                loadImage(imagePanel, detailInfo.getImagePath(), placeName, 560, 420, false);
            } else {
                // 이미지가 없는 경우
            }

            JTextArea descriptionArea = new JTextArea(detailInfo.getDescription());
            descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setFocusable(false);
            descriptionArea.setBorder(BorderFactory.createTitledBorder("장소 설명"));
            
            JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
            descriptionScrollPane.setPreferredSize(new Dimension(560, 200));

            leftPanel.add(imagePanel, BorderLayout.CENTER);
            leftPanel.add(descriptionScrollPane, BorderLayout.SOUTH);

            // Right Panel for Details, Map, and Actions
            JPanel rightPanel = new JPanel(new BorderLayout());
            rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            rightPanel.setBackground(Color.WHITE);

            JLabel titleLabel = new JLabel(placeName);
            titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
            rightPanel.add(titleLabel, BorderLayout.NORTH);

            JPanel infoAndMapPanel = new JPanel();
            infoAndMapPanel.setLayout(new BoxLayout(infoAndMapPanel, BoxLayout.Y_AXIS));
            infoAndMapPanel.setBackground(Color.WHITE);

            // Info Panel
            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setBackground(Color.WHITE);
            infoPanel.setBorder(BorderFactory.createTitledBorder("세부 정보"));
            infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            if ("축제".equals(category)) {
                infoPanel.add(createLabel("축제 기간: " + detailInfo.getRestDay()));
                infoPanel.add(Box.createVerticalStrut(10));
                infoPanel.add(createLabel("교통편: " + detailInfo.getTransportation()));
                infoPanel.add(Box.createVerticalStrut(10));
                infoPanel.add(createLabel("주소: " + detailInfo.getAddress()));
            } else { // 관광지
                infoPanel.add(createLabel("교통 정보: " + detailInfo.getTransportation()));
                infoPanel.add(Box.createVerticalStrut(10));
                infoPanel.add(createLabel("휴무일: " + detailInfo.getRestDay()));
                infoPanel.add(Box.createVerticalStrut(10));
                infoPanel.add(createLabel("주소: " + detailInfo.getAddress()));
            }
            
            infoAndMapPanel.add(infoPanel);
            infoAndMapPanel.add(Box.createVerticalStrut(10));

            // Map Panel
            JPanel mapPanel = new JPanel(new BorderLayout());
            mapPanel.setBackground(Color.WHITE);
            mapPanel.setBorder(BorderFactory.createTitledBorder("지도"));
            mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel mapLabel = new JLabel();
            mapLabel.setPreferredSize(new Dimension(800, 400));
            mapPanel.add(mapLabel, BorderLayout.CENTER);

            JButton zoomInButton = new RoundedButton("+");
            setupZoomButton(zoomInButton);
            
            JButton zoomOutButton = new RoundedButton("-");
            setupZoomButton(zoomOutButton);

            JPanel zoomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            zoomPanel.setBackground(Color.WHITE);
            zoomPanel.add(zoomInButton);
            zoomPanel.add(zoomOutButton);
            mapPanel.add(zoomPanel, BorderLayout.SOUTH);
            
            infoAndMapPanel.add(mapPanel);

            new SwingWorker<Coordinate, Void>() {
                @Override
                protected Coordinate doInBackground() throws Exception {
                    return tmapApiHandler.getCoordinates(detailInfo.getAddress());
                }

                @Override
                protected void done() {
                    try {
                        Coordinate coordinate = get();
                        if (coordinate != null) {
                            updateMap(mapLabel, coordinate, currentZoom, 800, 400);
                            for(ActionListener al : zoomInButton.getActionListeners()) { zoomInButton.removeActionListener(al); }
                            for(ActionListener al : zoomOutButton.getActionListeners()) { zoomOutButton.removeActionListener(al); }
                            zoomInButton.addActionListener(e -> {
                                if (currentZoom < 20) {
                                    currentZoom++;
                                    updateMap(mapLabel, coordinate, currentZoom, 800, 400);
                                }
                            });
                            zoomOutButton.addActionListener(e -> {
                                if (currentZoom > 1) {
                                    currentZoom--;
                                    updateMap(mapLabel, coordinate, currentZoom, 800, 400);
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
            
            rightPanel.add(infoAndMapPanel, BorderLayout.CENTER);

            JButton saveBtn = new JButton("저장하기");
            saveBtn.setBackground(ColorConstants.ACCENT);
            saveBtn.setForeground(Color.WHITE);
            saveBtn.setFocusPainted(false);
            saveBtn.setBorderPainted(false);
            saveBtn.setFont(new Font("맑은 고딕", Font.BOLD, 15));
            saveBtn.addActionListener(e -> JOptionPane.showMessageDialog(detailDialog, placeName + " 저장 완료! 저장된 장소 탭에서 확인하세요."));
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(saveBtn);
            rightPanel.add(buttonPanel, BorderLayout.SOUTH);

            splitPane.setLeftComponent(leftPanel);
            splitPane.setRightComponent(rightPanel);
            
            detailDialog.add(splitPane, BorderLayout.CENTER);
        } else {
            // Fallback for when detailInfo is null
            JPanel panel = new JPanel(new GridBagLayout());
            panel.add(new JLabel("해당 장소의 정보를 찾을 수 없습니다."));
            detailDialog.add(panel);
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

    private void loadImage(ScaledImagePanel imagePanel, String imagePath, String placeName, int width, int height, boolean isList) {
        new SwingWorker<Image, Void>() {
            @Override
            protected Image doInBackground() throws Exception {
                URL url = new URL(imagePath);
                Image image = ImageIO.read(url);
                if (image != null) {
                    if (!isList) {
                        ImageCache.getInstance().putImage(imagePath, new ImageIcon(image));
                    }
                }
                return image;
            }

            @Override
            protected void done() {
                try {
                    Image image = get();
                    if (image != null) {
                        imagePanel.setImage(image);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    private void loadGoogleImage(ScaledImagePanel imagePanel, String placeName, int width, int height, boolean isList) {
        // TMAP API로는 대체할 수 없으므로, 이 메소드는 비워두거나 기본 이미지를 설정합니다.
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel("<html><body style='width: 300px'>" + text + "</body></html>");
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }
    
    private void setupZoomButton(JButton button) {
        button.setPreferredSize(new Dimension(40, 40));
        button.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(ColorConstants.ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    // 둥근 버튼을 위한 커스텀 클래스
    class RoundedButton extends JButton {
        public RoundedButton(String text) {
            super(text);
            setContentAreaFilled(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (getModel().isArmed()) {
                g2.setColor(getBackground().darker());
            } else if (getModel().isRollover()) {
                g2.setColor(getBackground().brighter());
            } else {
                g2.setColor(getBackground());
            }
            
            g2.fillOval(0, 0, getWidth(), getHeight());

            // 텍스트를 직접 중앙에 그리기
            g2.setColor(getForeground());
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int stringAscent = fm.getAscent();
            int stringDescent = fm.getDescent();
            int x = (getWidth() - stringWidth) / 2;
            int y = (getHeight() - stringAscent - stringDescent) / 2 + stringAscent;
            g2.drawString(getText(), x, y);
            
            g2.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension size = super.getPreferredSize();
            size.width = size.height = Math.max(size.width, size.height);
            return size;
        }
    }
}
