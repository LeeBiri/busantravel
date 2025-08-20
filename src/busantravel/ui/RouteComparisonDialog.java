package busantravel.ui;

import javax.swing.*;
import java.awt.*;

import busantravel.dao.PlaceDAO;
import busantravel.model.ScheduledPlace;
import busantravel.util.Coordinate;
import busantravel.util.StaticMapGenerator;
import busantravel.util.TmapRouteInfo;
import busantravel.util.TmapRouteResult;

import busantravel.dao.PlaceDAO;
import busantravel.model.ScheduledPlace;
import busantravel.util.Coordinate;
import busantravel.util.StaticMapGenerator;
import busantravel.util.TmapRouteInfo;
import busantravel.util.TmapRouteResult;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class RouteComparisonDialog extends JDialog {

    private boolean routeApplied = false;
    private final StaticMapGenerator mapGenerator;
    private Font jalnanFont;
    private static final String TMAP_API_KEY = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg"; // FullSchedulePanel과 동일한 키 사용

    public RouteComparisonDialog(Frame owner, List<ScheduledPlace> originalRoute, List<ScheduledPlace> optimizedRoute, TmapRouteResult originalRouteInfo, TmapRouteResult optimizedRouteInfo) {
        super(owner, "경로 비교", true);
        this.mapGenerator = new StaticMapGenerator(TMAP_API_KEY);
        loadFont();
        setSize(1300, 850);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        initUI(originalRoute, optimizedRoute, originalRouteInfo, optimizedRouteInfo);
    }

    private void initUI(List<ScheduledPlace> originalRoute, List<ScheduledPlace> optimizedRoute, TmapRouteResult originalRouteInfo, TmapRouteResult optimizedRouteInfo) {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        getRootPane().setBorder(new EmptyBorder(15, 15, 15, 15));

        // 메인 분할 패널
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                createRoutePanel("기존 경로", originalRoute, originalRouteInfo),
                createRoutePanel("최적 경로", optimizedRoute, optimizedRouteInfo));
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setOpaque(false);

        add(splitPane, BorderLayout.CENTER);

        // 하단 패널 (비교 정보 + 버튼)
        add(createBottomPanel(originalRouteInfo, optimizedRouteInfo), BorderLayout.SOUTH);
    }

    private JPanel createRoutePanel(String title, List<ScheduledPlace> route, TmapRouteResult routeInfo) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        TitledBorder border = BorderFactory.createTitledBorder(new LineBorder(Color.LIGHT_GRAY, 1, true), title);
        border.setTitleFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        panel.setBorder(border);
        panel.setOpaque(false);

        // 지도 패널
        JLabel mapLabel = new JLabel("지도 생성 중...", SwingConstants.CENTER);
        mapLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        mapLabel.setPreferredSize(new Dimension(580, 400));
        mapLabel.setBorder(new LineBorder(Color.GRAY));
        panel.add(mapLabel, BorderLayout.CENTER);

        // 정보 텍스트 영역
        JTextArea infoArea = new JTextArea(generateRouteInfoText(route, routeInfo));
        infoArea.setEditable(false);
        infoArea.setFont(jalnanFont.deriveFont(Font.PLAIN, 14f));
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(580, 200));
        scrollPane.getVerticalScrollBar().setUI(new MatteScrollBarUI());
        scrollPane.getHorizontalScrollBar().setUI(new MatteScrollBarUI());
        panel.add(scrollPane, BorderLayout.SOUTH);

        // 지도 이미지 비동기 로드
        if (routeInfo != null && routeInfo.getPathCoordinates() != null && !routeInfo.getPathCoordinates().isEmpty()) {
            new Thread(() -> {
                // 컴포넌트 크기가 0일 수 있으므로 고정 크기를 사용
                String mapUrl = mapGenerator.generateRouteMapUrl(routeInfo.getPathCoordinates(), 580, 400, "blue");
                try {
                    ImageIcon icon = new ImageIcon(new URL(mapUrl));
                    SwingUtilities.invokeLater(() -> {
                        mapLabel.setText("");
                        mapLabel.setIcon(icon);
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> mapLabel.setText("지도 로딩 실패"));
                    e.printStackTrace();
                }
            }).start();
        } else {
            mapLabel.setText("경로 정보 없음");
        }

        return panel;
    }
    
    private String generateRouteInfoText(List<ScheduledPlace> route, TmapRouteResult routeInfo) {
        StringBuilder sb = new StringBuilder();
        PlaceDAO placeDAO = new PlaceDAO();

        sb.append("--- 경로 순서 ---\n");
        for (int i = 0; i < route.size(); i++) {
            ScheduledPlace sp = route.get(i);
            Object rawPlace = placeDAO.getPlaceByTypeAndId(sp.getP_type(), sp.getP_id());
            String placeName = getPlaceName(rawPlace);
            sb.append(String.format("%d. %s\n", i + 1, placeName));
        }
        sb.append("\n--- 요약 정보 ---\n");
        if (routeInfo != null && routeInfo.getSummary() != null) {
            TmapRouteInfo summary = routeInfo.getSummary();
            sb.append(String.format("총 이동 시간: %d분\n", summary.getTotalTime() / 60));
            sb.append(String.format("총 이동 거리: %.1fkm\n", summary.getTotalDistance() / 1000.0));
            sb.append(String.format("예상 택시 요금: %,d원\n", summary.getTaxiFare()));
        } else {
            sb.append("정보 없음\n");
        }
        return sb.toString();
    }
    
    private String getPlaceName(Object rawPlace) {
        if (rawPlace instanceof busantravel.model.Tourist) return ((busantravel.model.Tourist) rawPlace).getT_place();
        if (rawPlace instanceof busantravel.model.Restaurant) return ((busantravel.model.Restaurant) rawPlace).getR_place();
        if (rawPlace instanceof busantravel.model.Lodging) return ((busantravel.model.Lodging) rawPlace).getL_place();
        if (rawPlace instanceof busantravel.model.Festival) return ((busantravel.model.Festival) rawPlace).getF_name();
        return "알 수 없는 장소";
    }

    private JPanel createBottomPanel(TmapRouteResult original, TmapRouteResult optimized) {
        JPanel panel = new JPanel(new BorderLayout(20, 10));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 10, 10, 10));

        // 비교 정보
        JPanel comparisonInfoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        comparisonInfoPanel.setOpaque(false);
        
        String timeDiffText = "시간 변화 없음";
        String distDiffText = "거리 변화 없음";

        if (original != null && optimized != null && original.getSummary() != null && optimized.getSummary() != null) {
            int timeDiff = (original.getSummary().getTotalTime() - optimized.getSummary().getTotalTime()) / 60;
            double distDiff = (original.getSummary().getTotalDistance() - optimized.getSummary().getTotalDistance()) / 1000.0;
            
            timeDiffText = String.format("<html><center>시간 절약<br><font color='%s'>%s%d분</font></center></html>", timeDiff > 0 ? "blue" : "red", timeDiff > 0 ? "-" : "+", Math.abs(timeDiff));
            distDiffText = String.format("<html><center>거리 단축<br><font color='%s'>%s%.1fkm</font></center></html>", distDiff > 0 ? "blue" : "red", distDiff > 0 ? "-" : "+", Math.abs(distDiff));
        }

        JLabel timeDiffLabel = new JLabel(timeDiffText, SwingConstants.CENTER);
        timeDiffLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        JLabel distanceDiffLabel = new JLabel(distDiffText, SwingConstants.CENTER);
        distanceDiffLabel.setFont(jalnanFont.deriveFont(Font.PLAIN, 18f));
        
        comparisonInfoPanel.add(timeDiffLabel);
        comparisonInfoPanel.add(distanceDiffLabel);

        // 버튼
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        JButton applyButton = new JButton("새 경로로 적용");
        applyButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        applyButton.setBackground(ColorConstants.ACCENT);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFocusPainted(false);
        
        JButton cancelButton = new JButton("취소");
        cancelButton.setFont(jalnanFont.deriveFont(Font.PLAIN, 16f));
        cancelButton.setFocusPainted(false);
        
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);

        panel.add(comparisonInfoPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);

        cancelButton.addActionListener(e -> dispose());
        applyButton.addActionListener(e -> {
            routeApplied = true;
            dispose();
        });

        return panel;
    }

    public boolean isRouteApplied() {
        return routeApplied;
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
