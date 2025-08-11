package busantravel.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
 * Google Maps Platform의 다양한 API와의 통신을 처리하는 핸들러 클래스입니다.
 * <p>
 * 이 클래스는 다음과 같은 기능을 제공합니다:
 * <ul>
 *   <li>장소 이름 또는 Place ID를 이용한 상세 정보 조회 (Places API)</li>
 *   <li>사용자 입력에 대한 장소 자동 완성 목록 제공 (Places API)</li>
 *   <li>장소 사진 URL 생성 (Places API)</li>
 *   <li>현재 기기의 위치 정보 추정 (Geolocation API)</li>
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
     * @param key Google API 키
     * @return PlaceInfo 객체 (장소 정보)
     */
    public PlaceInfo getPlaceDetailsByName(String placeName, String key) {
        try {
            String placeId = findPlaceId(placeName, key);
            if (placeId == null) {
                System.err.println("장소를 찾을 수 없습니다: " + placeName);
                return null;
            }
            return getPlaceDetailsById(placeId, key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 현재 기기의 위치 정보를 가져옵니다.
     * @param key Google API 키
     * @return LocationData 객체 (위치 정보)
     */
    public LocationData getLocation(String key) {
        HttpURLConnection urlConnection = null;
        String apiUrl = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + key;

        try {
            URL url = new URL(apiUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setReadTimeout(3000);
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            urlConnection.setDoOutput(true);

            try (OutputStream os = urlConnection.getOutputStream();
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                writer.write("{}");
                writer.flush();
            }

            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                StringBuilder buffer = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(), "UTF-8"))) {
                    String readLine;
                    while ((readLine = br.readLine()) != null) {
                        buffer.append(readLine);
                    }
                }
                return parseLocationData(buffer.toString());
            } else {
                System.err.println("API 호출 실패: " + urlConnection.getResponseCode() + " " + urlConnection.getResponseMessage());
                return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
    
    
    /**
     * 입력된 검색어를 기반으로 장소 자동 완성 예상 목록을 반환합니다.
     * 검색 지역은 부산으로 제한되어 있습니다.
     * @param input 사용자가 입력한 검색어 (예: "롯데월드")
     * @param key Google API 키
     * @return AutocompletePrediction 객체의 리스트. 실패 시 비어있는 리스트 반환.
     */
    public List<AutocompletePrediction> getAutocompletePredictions(String input, String key) {
        try {
            String encodedInput = URLEncoder.encode(input, "UTF-8");
            
            // --- 부산 경계 정의 ---
            String busanBounds = "rectangle:34.98,128.76|35.31,129.29";
            String encodedBounds = URLEncoder.encode(busanBounds, "UTF-8");
            
            String apiUrl = "https://maps.googleapis.com/maps/api/place/autocomplete/json" +
                            "?input=" + encodedInput +
                            "&language=ko" +
                            "&components=country:kr" + // 한국 내 결과 우선
                            "&locationrestriction=" + encodedBounds + // <<--- 위치 제한 추가
                            "&strictbounds=true" +                     // <<--- 엄격한 제한 활성화
                            "&key=" + key;

            String jsonResponse = sendGetRequest(apiUrl);
            if (jsonResponse == null) {
                return Collections.emptyList(); // 실패 시 비어있는 리스트 반환
            }
            return parseAutocompletePredictions(jsonResponse);
        } catch (UnsupportedEncodingException e) {
            System.err.println("URL 인코딩 실패: " + e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    /**
     * 사진 참조 ID를 사용하여 장소의 사진을 볼 수 있는 URL을 생성합니다.
     * 이 메소드는 실제 네트워크 요청을 보내지 않고, URL 문자열만 조립하여 반환합니다.
     * @param photoReference PlaceInfo.getPhotoReference()를 통해 얻은 ID
     * @param maxWidth       사진의 최대 너비 (픽셀 단위)
     * @param key            Google API 키
     * @return 이미지 URL 문자열. photoReference가 없으면 null을 반환.
     */
    public String getPhotoUrl(String photoReference, int maxWidth, String key) {
        if (photoReference == null || photoReference.isEmpty()) {
            return null;
        }
        return String.format(
            "https://maps.googleapis.com/maps/api/place/photo?maxwidth=%d&photoreference=%s&key=%s",
            maxWidth, photoReference, key
        );
    }

    public String getStaticMapUrl(double latitude, double longitude, int zoom, int width, int height, String key) {
        return String.format(
            "https://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&markers=color:red%%7C%f,%f&key=%s",
            latitude, longitude, zoom, width, height, latitude, longitude, key
        );
    }
    
    

    //================================================================
    // Private 헬퍼 메소드 (API 호출)
    //================================================================

    private String findPlaceId(String placeName, String key) throws Exception {
    	
    	// --- 부산 경계 상자 정의 ---
        String busanBias = "rectangle:34.98,128.76|35.31,129.29";
        String encodedBias = URLEncoder.encode(busanBias, "UTF-8");
        
        String apiUrl = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json" +
                        "?input=" + placeName +
                        "&inputtype=textquery" +
                        "&fields=place_id" +
                        "&language=ko" +
                        "&locationbias=" + encodedBias + // <<--- 위치 편향
                        "&key=" + key;
        String jsonResponse = sendGetRequest(apiUrl);
        if (jsonResponse == null) return null;

        return parsePlaceId(jsonResponse);
    }

    public PlaceInfo getPlaceDetailsById(String placeId, String key) throws Exception {
        String apiUrl = "https://maps.googleapis.com/maps/api/place/details/json" +
                        "?place_id=" + placeId +
                        "&fields=name,formatted_address,rating,reviews,geometry,photos" +
                        "&fields=name,formatted_address,rating,reviews,geometry" +
                        "&language=ko" +
                        "&key=" + key;

        String jsonResponse = sendGetRequest(apiUrl);
        if (jsonResponse == null) return null;

        return parsePlaceDetails(jsonResponse);
    }
    
    //================================================================
    // Private 헬퍼 메소드들 (JSON 파싱)
    //================================================================

    private String parsePlaceId(String jsonResponse) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject responseObj = (JSONObject) parser.parse(jsonResponse);
        if (!responseObj.get("status").equals("OK")) return null;

        JSONArray candidates = (JSONArray) responseObj.get("candidates");
        if (candidates.isEmpty()) return null;

        return (String) ((JSONObject) candidates.get(0)).get("place_id");
    }
    
    //PlaceDetails JSON 응답 파싱 , 장소 정보 및 사진 URL 포함
    private PlaceInfo parsePlaceDetails(String jsonResponse) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject responseObj = (JSONObject) parser.parse(jsonResponse);
        if (!responseObj.get("status").equals("OK")) return null;

        JSONObject result = (JSONObject) responseObj.get("result");
        String name = (String) result.get("name");
        String address = (String) result.get("formatted_address");
        double rating = result.get("rating") != null ? ((Number) result.get("rating")).doubleValue() : 0.0;

        JSONObject geometry = (JSONObject) result.get("geometry");
        JSONObject location = (JSONObject) geometry.get("location");
        double latitude = (Double) location.get("lat");
        double longitude = (Double) location.get("lng");
        
        
        String photoReference = null;
        if (result.containsKey("photos")) {
            JSONArray photosArray = (JSONArray) result.get("photos");
            if (!photosArray.isEmpty()) {
                JSONObject firstPhoto = (JSONObject) photosArray.get(0);
                photoReference = (String) firstPhoto.get("photo_reference");
            }
        }

        List<PlaceReview> reviews = new ArrayList<>();
        if (result.containsKey("reviews")) {
            JSONArray reviewArray = (JSONArray) result.get("reviews");
            
            int limit = Math.min(reviewArray.size(), 5);
            for (int i = 0; i < limit; i++) {
                JSONObject reviewObj = (JSONObject) reviewArray.get(i);
                reviews.add(new PlaceReview(
                        (String) reviewObj.get("author_name"),
                        (long) reviewObj.get("rating"),
                        (String) reviewObj.get("text"),
                        (String) reviewObj.get("relative_time_description")
                ));
            }
        }
        // PlaceInfo 생성자에 좌표값 전달
        return new PlaceInfo(name, address, rating, reviews, latitude, longitude, photoReference);
    }
    
    
    // Autocomplete JSON응답 파싱
    private List<AutocompletePrediction> parseAutocompletePredictions(String jsonResponse) throws ParseException {
        List<AutocompletePrediction> predictionList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        JSONObject responseObj = (JSONObject) parser.parse(jsonResponse);

        if (!responseObj.get("status").equals("OK")) {
            return predictionList;
        }

        JSONArray predictions = (JSONArray) responseObj.get("predictions");
        for (Object obj : predictions) {
            JSONObject pred = (JSONObject) obj;
            String description = (String) pred.get("description");
            String placeId = (String) pred.get("place_id");
            predictionList.add(new AutocompletePrediction(description, placeId));
        }
        return predictionList;
    }
    
    //LocationData JSON 응답 파싱 
    private LocationData parseLocationData(String jsonResponse) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject responseObject = (JSONObject) parser.parse(jsonResponse);
            JSONObject locationObject = (JSONObject) responseObject.get("location");

            double latitude = (Double) locationObject.get("lat");
            double longitude = (Double) locationObject.get("lng");
            double accuracy = (Double) responseObject.get("accuracy");
            
            return new LocationData(latitude, longitude, accuracy);

        } catch (ParseException e) {
            System.err.println("JSON 파싱에 실패했습니다: " + jsonResponse);
            e.printStackTrace();
            return null;
        }
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
            System.err.println("API GET Request Failed: " + conn.getResponseCode());
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.err.println(line);
                }
            }
            return null;
        }
    }
}
