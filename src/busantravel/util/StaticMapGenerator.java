package busantravel.util;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TMAP Static Map API를 사용하여 핀이 표시된 지도 이미지 URL을 생성하는 클래스입니다.
 */
public class StaticMapGenerator {

    private static final String STATIC_MAP_URL = "https://apis.openapi.sk.com/tmap/staticMap?version=1";
    private final String apiKey;

    /**
     * StaticMapGenerator의 생성자입니다.
     * @param apiKey TMAP API 키
     */
    public StaticMapGenerator(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 지정된 좌표에 핀을 표시하는 TMAP Static Map 이미지 URL을 생성합니다.
     *
     * @param coordinate   핀을 표시할 좌표
     * @param width        생성할 지도의 너비 (픽셀)
     * @param height       생성할 지도의 높이 (픽셀)
     * @param zoom         지도의 확대 수준
     * @return 지도 이미지에 접근할 수 있는 완전한 URL 문자열
     */
    public String generateMapWithPinUrl(Coordinate coordinate, int width, int height, int zoom) {
        if (coordinate == null || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder(STATIC_MAP_URL);
        urlBuilder.append("&appKey=").append(apiKey);
        urlBuilder.append("&width=").append(width);
        urlBuilder.append("&height=").append(height);
        urlBuilder.append("&zoom=").append(zoom);
        urlBuilder.append("&coordType=WGS84GEO");
        urlBuilder.append("&format=PNG");
        urlBuilder.append("&longitude=").append(coordinate.getLongitude());
        urlBuilder.append("&latitude=").append(coordinate.getLatitude());
        
        // markers 파라미터 형식 변경
        String markersValue = String.format("%f,%f", coordinate.getLongitude(), coordinate.getLatitude());
        urlBuilder.append("&markers=").append(markersValue);

        return urlBuilder.toString();
    }
    
    /**
     * 지정된 좌표 목록에 여러 개의 핀을 표시하는 TMAP Static Map 이미지 URL을 생성합니다.
     *
     * @param coordinates  핀을 표시할 좌표 목록
     * @param width        생성할 지도의 너비 (픽셀)
     * @param height       생성할 지도의 높이 (픽셀)
     * @param zoom         지도의 확대 수준
     * @return 지도 이미지에 접근할 수 있는 완전한 URL 문자열
     */
    public String generateMapWithMultiplePinsUrl(List<Coordinate> coordinates, int width, int height, int zoom) {
        if (coordinates == null || coordinates.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            return null;
        }

        StringBuilder urlBuilder = new StringBuilder(STATIC_MAP_URL);
        urlBuilder.append("&appKey=").append(apiKey);
        urlBuilder.append("&width=").append(width);
        urlBuilder.append("&height=").append(height);
        urlBuilder.append("&zoom=").append(zoom);
        urlBuilder.append("&coordType=WGS84GEO");
        urlBuilder.append("&format=PNG");

        // 여러 마커를 문자열로 조합
        String markersValue = coordinates.stream()
                                         .map(c -> String.format("pin_s_b:%f,%f", c.getLongitude(), c.getLatitude()))
                                         .collect(Collectors.joining("_"));
        urlBuilder.append("&markers=").append(markersValue);

        // 지도의 중심을 첫 번째 마커로 설정
        Coordinate firstCoordinate = coordinates.get(0);
        urlBuilder.append("&longitude=").append(firstCoordinate.getLongitude());
        urlBuilder.append("&latitude=").append(firstCoordinate.getLatitude());

        return urlBuilder.toString();
    }
}
