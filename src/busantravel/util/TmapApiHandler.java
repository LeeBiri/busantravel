package busantravel.util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SK Tmap 모빌리티 API와의 통신을 처리하는 핸들러 클래스입니다.
 * <p>
 * 이 클래스는 자동차 경로안내 API를 호출하여 두 지점 간의 경로 정보를 요청하고,
 * 그 결과를 파싱하여 요약 정보와 상세 경로 좌표를 제공하는 기능을 담당합니다.
 * 네트워크 통신에는 {@link HttpURLConnection}을 사용합니다.
 *
 * @see TmapRouteResult
 * @see TmapRouteInfo
 */
public class TmapApiHandler {

    private static final String TMAP_ROUTE_URL = "https://apis.openapi.sk.com/tmap/routes?version=1";
    private static final String TMAP_GEOCODING_URL = "https://apis.openapi.sk.com/tmap/pois?version=1&count=1&searchKeyword=";
    private final String apiKey;

    public TmapApiHandler(String apiKey) {
        this.apiKey = apiKey;
    }

    public Coordinate getCoordinates(String placeName) throws IOException, ParseException {
        String encodedPlaceName = URLEncoder.encode(placeName, StandardCharsets.UTF_8.name());
        String requestUrl = TMAP_GEOCODING_URL + encodedPlaceName + "&appKey=" + apiKey +
                            "&searchtypCd=R&radius=30&centerLon=129.0756&centerLat=35.1796";
        URL url = new URL(requestUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                JSONParser parser = new JSONParser();
                JSONObject jsonObj = (JSONObject) parser.parse(response.toString());
                JSONObject searchPoiInfo = (JSONObject) jsonObj.get("searchPoiInfo");
                if (searchPoiInfo != null) {
                    JSONObject pois = (JSONObject) searchPoiInfo.get("pois");
                    if (pois != null) {
                        JSONArray poi = (JSONArray) pois.get("poi");
                        if (poi != null && !poi.isEmpty()) {
                            JSONObject firstPoi = (JSONObject) poi.get(0);
                            double lat = Double.parseDouble(firstPoi.get("noorLat").toString());
                            double lon = Double.parseDouble(firstPoi.get("noorLon").toString());
                            return new Coordinate(lat, lon);
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Tmap 경로안내 API를 호출하여 경로 요약 및 상세 좌표를 포함한 전체 결과를 반환합니다.
     *
     * @return TmapRouteResult 객체, 실패 시 null
     */
    public TmapRouteResult getRouteInfo(String appKey, double startX, double startY, double endX, double endY, String startName, String endName) {
        HttpURLConnection conn = null;
        try {
            String jsonBody = String.format(
                "{\"startX\":%f,\"startY\":%f,\"endX\":%f,\"endY\":%f,\"startName\":\"%s\",\"endName\":\"%s\"," +
                "\"reqCoordType\":\"WGS84GEO\",\"resCoordType\":\"WGS84GEO\",\"tollgateFareOption\":16}",
                startX, startY, endX, endY, startName, endName
            );
            URL url = new URL(TMAP_ROUTE_URL);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("appKey", appKey);
            
            try(OutputStream os = conn.getOutputStream(); BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))){
                writer.write(jsonBody);
                writer.flush();
            }

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) { sb.append(line); }
                }
                return parseRouteResult(sb.toString()); // 파싱 메소드 호출
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /**
     * Tmap 경로안내 API의 JSON 응답을 파싱하여 TmapRouteResult 객체로 변환합니다.
     */
    private TmapRouteResult parseRouteResult(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObj = (JSONObject) parser.parse(jsonResponse);
            JSONArray features = (JSONArray) jsonObj.get("features");

            if (features == null || features.isEmpty()) return null;

            // 1. 경로 요약 정보 파싱 (첫 번째 feature)
            JSONObject summaryFeature = (JSONObject) features.get(0);
            JSONObject summaryProps = (JSONObject) summaryFeature.get("properties");
            int totalDistance = ((Number) summaryProps.get("totalDistance")).intValue();
            int totalTime = ((Number) summaryProps.get("totalTime")).intValue();
            int totalFare = ((Number) summaryProps.get("totalFare")).intValue();
            int taxiFare = ((Number) summaryProps.get("taxiFare")).intValue();
            TmapRouteInfo summaryInfo = new TmapRouteInfo(totalDistance, totalTime, totalFare, taxiFare);
            
            // 2. 상세 경로 좌표 파싱 (LineString 타입의 feature들)
            List<Coordinate> pathCoordinates = new ArrayList<>();
            for (Object featureObj : features) {
                JSONObject feature = (JSONObject) featureObj;
                JSONObject geometry = (JSONObject) feature.get("geometry");
                if (geometry.get("type").equals("LineString")) {
                    JSONArray coordsArray = (JSONArray) geometry.get("coordinates");
                    for (Object coordObj : coordsArray) {
                        JSONArray coord = (JSONArray) coordObj;
                        double longitude = (Double) coord.get(0);
                        double latitude = (Double) coord.get(1);
                        pathCoordinates.add(new Coordinate(latitude, longitude));
                    }
                }
            }
            
            return new TmapRouteResult(summaryInfo, pathCoordinates);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
