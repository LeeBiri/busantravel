package busantravel.util;

import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * TMAP API와의 통신을 처리하는 핸들러 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * <ul>
 *   <li>장소 이름 또는 Place ID를 이용한 상세 정보 조회 (통합 검색 API)</li>
 *   <li>사용자 입력에 대한 장소 자동 완성 목록 제공 (통합 검색 API)</li>
 *   <li>정적 지도 이미지 URL 생성 (Static Map API)</li>
 * </ul>
 * 모든 네트워크 통신은 자바 표준 라이브러리인 {@link HttpURLConnection}을 사용합니다.
 *
 */
public class ApiHandler {

    //================================================================
    // Public API 호출 메소드들
    //================================================================

    /**
     * 장소 이름으로 장소의 상세 정보를 검색합니다.
     * 부산의 장소가 우선적으로 검색됩니다.
     * @param placeName 검색할 장소 이름
     * @param key TMAP API 키
     * @return PlaceInfo 객체 (장소 정보)
     */
    public PlaceInfo getPlaceDetailsByName(String placeName, String key) {
        try {
            List<PlaceInfo> places = searchPlace(placeName, key);
            if (places.isEmpty()) {
                System.err.println("장소를 찾을 수 없습니다: " + placeName);
                return null;
            }
            // 가장 첫 번째 결과를 반환
            return places.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 입력된 검색어를 기반으로 장소 자동 완성 예상 목록을 반환합니다.
     * 검색 지역은 부산으로 제한되어 있습니다.
     * @param input 사용자가 입력한 검색어 (예: "롯데월드")
     * @param key TMAP API 키
     * @return AutocompletePrediction 객체의 리스트. 실패 시 비어있는 리스트 반환.
     */
    public List<AutocompletePrediction> getAutocompletePredictions(String input, String key) {
        try {
            List<PlaceInfo> places = searchPlace(input, key);
            List<AutocompletePrediction> predictions = new ArrayList<>();
            for (PlaceInfo place : places) {
                predictions.add(new AutocompletePrediction(place.getName(), place.getAddress()));
            }
            return predictions;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    public String getStaticMapUrl(double latitude, double longitude, int zoom, int width, int height, String key) {
        // 공식 문서에 따라 appKey를 URL 파라미터로 전달합니다.
        return String.format(
            "https://apis.openapi.sk.com/tmap/staticmap?version=1&appKey=%s&lon=%f&lat=%f&zoom=%d&width=%d&height=%d&markers=%f,%f",
            key, longitude, latitude, zoom, width, height, longitude, latitude
        );
    }
    
    

    //================================================================
    // Private 헬퍼 메소드 (API 호출)
    //================================================================

    private List<PlaceInfo> searchPlace(String query, String key) throws Exception {
        String encodedQuery = URLEncoder.encode(query, "UTF-8");
        String apiUrl = "https://apis.openapi.sk.com/tmap/pois?version=1&format=json&count=10" +
                        "&searchKeyword=" + encodedQuery +
                        "&resCoordType=WGS84GEO" +
                        "&reqCoordType=WGS84GEO" +
                        "&appKey=" + key;

        // 부산 중심 좌표 (서면역)
        apiUrl += "&centerLon=129.0596&centerLat=35.1581";

        String jsonResponse = sendGetRequest(apiUrl);
        if (jsonResponse == null) {
            return Collections.emptyList();
        }

        return parsePlaces(jsonResponse);
    }

    //================================================================
    // Private 헬퍼 메소드들 (JSON 파싱)
    //================================================================

    private List<PlaceInfo> parsePlaces(String jsonResponse) throws ParseException {
        List<PlaceInfo> places = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONObject responseObj = (JSONObject) parser.parse(jsonResponse);
        JSONObject searchPoiInfo = (JSONObject) responseObj.get("searchPoiInfo");
        if (searchPoiInfo == null) return places;

        JSONObject pois = (JSONObject) searchPoiInfo.get("pois");
        if (pois == null) return places;

        JSONArray poiArr = (JSONArray) pois.get("poi");
        if (poiArr == null) return places;

        for (Object obj : poiArr) {
            JSONObject poi = (JSONObject) obj;
            String name = (String) poi.get("name");
            
            String upperAddrName = (String) poi.get("upperAddrName");
            String middleAddrName = (String) poi.get("middleAddrName");
            String lowerAddrName = (String) poi.get("lowerAddrName");
            String detailAddrName = (String) poi.get("detailAddrName");
            String firstNo = (String) poi.get("firstNo");
            String secondNo = (String) poi.get("secondNo");

            StringBuilder addressBuilder = new StringBuilder();
            if (upperAddrName != null) addressBuilder.append(upperAddrName).append(" ");
            if (middleAddrName != null) addressBuilder.append(middleAddrName).append(" ");
            if (lowerAddrName != null) addressBuilder.append(lowerAddrName).append(" ");
            if (detailAddrName != null) addressBuilder.append(detailAddrName).append(" ");
            if (firstNo != null) addressBuilder.append(firstNo);
            if (secondNo != null && !secondNo.equals("0")) addressBuilder.append("-").append(secondNo);

            String address = addressBuilder.toString().trim();

            double latitude = Double.parseDouble((String) poi.get("noorLat"));
            double longitude = Double.parseDouble((String) poi.get("noorLon"));

            // TMAP API는 평점, 리뷰, 사진 정보를 제공하지 않으므로 기본값으로 설정
            places.add(new PlaceInfo(name, address, 0.0, new ArrayList<>(), latitude, longitude, null));
        }
        return places;
    }
    
    //================================================================
    // Private 헬퍼 메소드 (네트워크 통신)
    //================================================================
    

     //API호출 URL을 API 서버로 전송하여 응답을 String 타입 JSON 형태로 리턴
    private String sendGetRequest(String apiUrl) throws Exception {
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);   
        conn.setReadTimeout(5000);

        //정상 HTTP 응답을 받으면
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            return sb.toString();
        } else { //비정상 HTTP 응답
            System.err.println("API GET Request Failed: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                StringBuilder errorBody = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    errorBody.append(line);
                }
                System.err.println("Error Response: " + errorBody.toString());
            }
            return null;
        }
    }
}
