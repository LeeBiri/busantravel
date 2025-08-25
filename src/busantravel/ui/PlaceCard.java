package busantravel.ui;

import busantravel.auth.MemberSession;
import busantravel.dao.PlaceDAO;
import busantravel.model.Interest;
import busantravel.service.InterestService;
import busantravel.util.Coordinate;
import busantravel.util.ImageCache;
import busantravel.util.StaticMapGenerator;
import busantravel.util.TmapApiHandler;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlaceCard extends JPanel {
    private String placeId;
    private PlaceDAO placeDAO;
    private Font jalnanFont;
    private TmapApiHandler tmapApiHandler;
    private StaticMapGenerator staticMapGenerator;
    private int currentZoom = 14;
    private TravelPlannerApp mainApp;
    private static final String API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg"; // TMAP API Key

    public enum ViewType {
        GRID,
        LIST
    }

    public PlaceCard(JFrame frame, String placeId, String placeName, String category, String thumbnail, ViewType viewType, Font customFont, TravelPlannerApp mainApp) {
        this.placeId = placeId;
        this.placeDAO = new PlaceDAO();
        this.tmapApiHandler = new TmapApiHandler(API_KEY);
        this.staticMapGenerator = new StaticMapGenerator(API_KEY);
        this.jalnanFont = customFont;
        this.mainApp = mainApp;
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
        loadImageAsync(imagePanel, thumbnail, placeName, 180, 120, false);

        JLabel nameLabel = new JLabel(placeName, JLabel.CENTER);
        nameLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        nameLabel.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        nameLabel.setBackground(new Color(245, 245, 245));
        nameLabel.setOpaque(true);

        add(imagePanel, BorderLayout.CENTER);
        add(nameLabel, BorderLayout.SOUTH);
        addMouseListener(createMouseAdapter(frame, placeName, category));
    }

    private void setupListView(JFrame frame, String placeName, String category, String thumbnail) {
        setLayout(new BorderLayout(15, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(Color.WHITE);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));

        ScaledImagePanel imagePanel = new ScaledImagePanel();
        imagePanel.setPreferredSize(new Dimension(120, 120));
        loadImageAsync(imagePanel, thumbnail, placeName, 120, 120, true);
        add(imagePanel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new BorderLayout(0, 8));
        textPanel.setBackground(Color.WHITE);

        JLabel nameLabel = new JLabel(placeName);
        nameLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));

        JTextArea descriptionArea = new JTextArea("설명 로딩 중...");
        setupDescriptionArea(descriptionArea);
        
        PlaceDetailInfo detailInfo = placeDAO.getPlaceDetail(placeName, category);
        String descriptionText = (detailInfo != null && detailInfo.getDescription() != null && !detailInfo.getDescription().isEmpty())
            ? detailInfo.getDescription() : "이 장소에 대한 설명을 찾을 수 없습니다.";
        descriptionArea.setText(descriptionText);

        textPanel.add(nameLabel, BorderLayout.NORTH);
        textPanel.add(descriptionArea, BorderLayout.CENTER);

        add(textPanel, BorderLayout.CENTER);
        MouseAdapter adapter = createMouseAdapter(frame, placeName, category);
        addMouseListener(adapter);
        descriptionArea.addMouseListener(adapter);
    }

    public void showPlaceDetailDialog(JFrame frame, String placeName, String category) {
        JDialog detailDialog = new JDialog(frame, placeName + " 상세 정보", true);
        detailDialog.setSize(1180, 850);
        detailDialog.setLocationRelativeTo(frame);
        detailDialog.setLayout(new BorderLayout());

        PlaceDetailInfo detailInfo = placeDAO.getPlaceDetail(placeName, category);

        if (detailInfo == null) {
            JPanel panel = new JPanel(new GridBagLayout());
            panel.add(new JLabel("해당 장소의 정보를 찾을 수 없습니다."));
            detailDialog.add(panel);
            detailDialog.setVisible(true);
            return;
        }

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);
        splitPane.setBorder(null);

        splitPane.setLeftComponent(createLeftPanel(detailInfo, placeName));
        splitPane.setRightComponent(createRightPanel(detailInfo, placeName, category));

        detailDialog.add(splitPane, BorderLayout.CENTER);
        detailDialog.setVisible(true);
    }

    private JPanel createLeftPanel(PlaceDetailInfo detailInfo, String placeName) {
        JPanel leftPanel = new JPanel(new BorderLayout(10, 10));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        leftPanel.setBackground(Color.WHITE);

        ScaledImagePanel imagePanel = new ScaledImagePanel();
        imagePanel.setPreferredSize(new Dimension(560, 420));
        loadImageAsync(imagePanel, detailInfo.getImagePath(), placeName, 560, 420, false);

        JTextArea descriptionArea = new JTextArea(detailInfo.getDescription());
        setupDescriptionArea(descriptionArea);
        descriptionArea.setBorder(BorderFactory.createTitledBorder("장소 설명"));
        
        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setPreferredSize(new Dimension(560, 200));
        descriptionScrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        descriptionScrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());

        leftPanel.add(imagePanel, BorderLayout.CENTER);
        leftPanel.add(descriptionScrollPane, BorderLayout.SOUTH);
        return leftPanel;
    }

    private JPanel createRightPanel(PlaceDetailInfo detailInfo, String placeName, String category) {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        rightPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(placeName);
        titleLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 26f));
        rightPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel infoAndMapPanel = new JPanel();
        infoAndMapPanel.setLayout(new BoxLayout(infoAndMapPanel, BoxLayout.Y_AXIS));
        infoAndMapPanel.setBackground(Color.WHITE);

        infoAndMapPanel.add(createDetailsPanel(detailInfo, category));
        infoAndMapPanel.add(Box.createVerticalStrut(10));
        infoAndMapPanel.add(createMapPanel(detailInfo));
        
        rightPanel.add(infoAndMapPanel, BorderLayout.CENTER);

        JButton saveBtn = new JButton("찜하기");
        setupSaveButton(saveBtn, placeName, category);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(saveBtn);
        rightPanel.add(buttonPanel, BorderLayout.SOUTH);

        return rightPanel;
    }

    private Component createDetailsPanel(PlaceDetailInfo detailInfo, String category) {
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        switch (category) {
            case "축제":
                infoPanel.add(createBorderedInfoRowPanel("축제 기간", detailInfo.getRestDay()));
                infoPanel.add(createBorderedInfoRowPanel("교통편", detailInfo.getTransportation()));
                infoPanel.add(createBorderedInfoRowPanel("주소", detailInfo.getAddress()));
                break;
            case "숙소":
                infoPanel.add(createBorderedInfoRowPanel("교통 정보", detailInfo.getTransportation()));
                infoPanel.add(createBorderedInfoRowPanel("호텔 정보", detailInfo.getRestDay()));
                infoPanel.add(createBorderedInfoRowPanel("주소", detailInfo.getAddress()));
                infoPanel.add(createBorderedInfoRowPanel("전화번호", detailInfo.getPhone()));
                break;
            case "맛집":
                infoPanel.add(createBorderedInfoRowPanel("운영 시간", detailInfo.getRestDay()));
                infoPanel.add(createBorderedInfoRowPanel("주소", detailInfo.getAddress()));
                infoPanel.add(createBorderedInfoRowPanel("전화번호", detailInfo.getPhone()));
                infoPanel.add(createBorderedInfoRowPanel("대표 메뉴", detailInfo.getMenu()));
                break;
            default: // 관광지
                infoPanel.add(createBorderedInfoRowPanel("교통 정보", detailInfo.getTransportation()));
                infoPanel.add(createBorderedInfoRowPanel("휴무일", detailInfo.getRestDay()));
                infoPanel.add(createBorderedInfoRowPanel("주소", detailInfo.getAddress()));
                break;
        }

        JScrollPane infoScrollPane = new JScrollPane(infoPanel);
        infoScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        infoScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        infoScrollPane.setBorder(BorderFactory.createTitledBorder("세부 정보"));
        infoScrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        infoScrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        infoScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        return infoScrollPane;
    }

    private JPanel createMapPanel(PlaceDetailInfo detailInfo) {
        JPanel mapPanel = new JPanel(new BorderLayout());
        mapPanel.setBackground(Color.WHITE);
        mapPanel.setBorder(BorderFactory.createTitledBorder("지도"));
        mapPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel mapLabel = new JLabel();
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
        
        updateMapAsync(mapLabel, detailInfo.getAddress(), zoomInButton, zoomOutButton);
        
        return mapPanel;
    }

    private void updateMapAsync(JLabel mapLabel, String address, JButton zoomIn, JButton zoomOut) {
        new SwingWorker<Coordinate, Void>() {
            @Override
            protected Coordinate doInBackground() throws Exception {
                return tmapApiHandler.getCoordinates(address);
            }

            @Override
            protected void done() {
                try {
                    Coordinate coordinate = get();
                    if (coordinate != null) {
                        updateMapImage(mapLabel, coordinate, currentZoom);
                        for(ActionListener al : zoomIn.getActionListeners()) { zoomIn.removeActionListener(al); }
                        for(ActionListener al : zoomOut.getActionListeners()) { zoomOut.removeActionListener(al); }
                        
                        zoomIn.addActionListener(e -> {
                            if (currentZoom < 20) {
                                currentZoom++;
                                updateMapImage(mapLabel, coordinate, currentZoom);
                            }
                        });
                        zoomOut.addActionListener(e -> {
                            if (currentZoom > 1) {
                                currentZoom--;
                                updateMapImage(mapLabel, coordinate, currentZoom);
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
    }

    private void updateMapImage(JLabel mapLabel, Coordinate coordinate, int zoom) {
        new SwingWorker<ImageIcon, Void>() {
            @Override
            protected ImageIcon doInBackground() throws Exception {
                if (coordinate != null) {
                    int width = mapLabel.getWidth() > 0 ? mapLabel.getWidth() : 500;
                    int height = mapLabel.getHeight() > 0 ? mapLabel.getHeight() : 300;
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

    private void loadImageAsync(ScaledImagePanel imagePanel, String imagePath, String placeName, int width, int height, boolean isList) {
        if (imagePath == null || imagePath.isEmpty() || !imagePath.startsWith("http")) {
            return;
        }
        ImageIcon cachedImage = ImageCache.getInstance().getImage(imagePath);
        if (cachedImage != null) {
            imagePanel.setImage(cachedImage.getImage());
            return;
        }

        new SwingWorker<Image, Void>() {
            @Override
            protected Image doInBackground() throws Exception {
                URL url = new URL(imagePath);
                Image image = ImageIO.read(url);
                if (image != null && !isList) {
                    ImageCache.getInstance().putImage(imagePath, new ImageIcon(image));
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

    private JPanel createBorderedInfoRowPanel(String title, String content) {
        JPanel panel = createInfoRowPanel(title, content);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(5, 2, 5, 2)
        ));
        return panel;
    }

    private JPanel createInfoRowPanel(String title, String content) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        titleLabel.setPreferredSize(new Dimension(80, 30));
        panel.add(titleLabel, BorderLayout.WEST);

        JTextArea contentArea = new JTextArea(content);
        setupDescriptionArea(contentArea);
        contentArea.setOpaque(false);
        panel.add(contentArea, BorderLayout.CENTER);

        return panel;
    }

    private void setupDescriptionArea(JTextArea area) {
        area.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        area.setForeground(Color.DARK_GRAY);
        area.setWrapStyleWord(true);
        area.setLineWrap(true);
        area.setEditable(false);
        area.setFocusable(false);
    }

    private void setupZoomButton(JButton button) {
        button.setPreferredSize(new Dimension(40, 40));
        button.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(ColorConstants.ACCENT);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
    }

    private void setupSaveButton(JButton button, String placeName, String category) {
        button.setBackground(ColorConstants.ACCENT);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));

        String memberId = MemberSession.getId();
        AtomicBoolean alreadySaved = new AtomicBoolean(false);

        for (Interest interest : InterestService.getInterestById(memberId)) {
            if (interest.getP_id().equals(placeId) && interest.getP_type().equals(category)) {
                alreadySaved.set(true);
                break;
            }
        }
        button.setText(alreadySaved.get() ? "찜 해제" : "찜하기");

        button.addActionListener(e -> {
            if (alreadySaved.get()) {
                // 해제
                InterestService.deleteInterest(new Interest(memberId, placeId, category));
                JOptionPane.showMessageDialog(this, placeName + "을(를) 찜 목록에서 제거했습니다.");
                if (mainApp != null && mainApp.getMyPagePanel() != null) {
                    mainApp.getMyPagePanel().reloadFavoritePlacePanel();
                }
                button.setText("찜하기");
                alreadySaved.set(false);
            } else {
                // 등록
                InterestService.insertInterest(memberId, placeId, category);
                JOptionPane.showMessageDialog(this, placeName + "을(를) 찜했습니다!");
                button.setText("찜 해제");
                alreadySaved.set(true);
            }
        });
    }

    private MouseAdapter createMouseAdapter(JFrame frame, String placeName, String category) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPlaceDetailDialog(frame, placeName, category);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(new Color(245, 245, 245));
                for(Component c : getComponents()){
                    if(c instanceof JPanel) c.setBackground(new Color(245, 245, 245));
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(Color.WHITE);
                for(Component c : getComponents()){
                    if(c instanceof JPanel) c.setBackground(Color.WHITE);
                }
            }
        };
    }

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
