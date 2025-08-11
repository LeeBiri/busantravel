package busantravel.util;

import java.util.Collections;
import java.util.List;

/**
 * TMAP API 기반 장소 상세 정보를 저장하는 데이터 클래스입니다.
 * Google Places API와의 호환성을 위해 일부 필드와 메소드는 유지되었으나,
 * TMAP API에서 제공하지 않는 정보(평점, 리뷰, 사진)는 기본값을 반환합니다.
 *
 * @see ApiHandler
 */
public class PlaceInfo {
    private final String name;
    private final String address;
    private final double latitude;
    private final double longitude;

    /**
     * TMAP API 응답에 맞춰진 생성자입니다.
     *
     * @param name           장소의 이름
     * @param address        정규화된 주소
     * @param rating         (사용되지 않음)
     * @param reviews        (사용되지 않음)
     * @param latitude       장소의 위도
     * @param longitude      장소의 경도
     * @param photoReference (사용되지 않음)
     */
    public PlaceInfo(String name, String address, double rating, List<Object> reviews, double latitude, double longitude, String photoReference) {
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getter
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    // Google API 호환성을 위한 메소드
    public String getFormattedAddress() { return address; }
    public double getRating() { return 0.0; } // TMAP 미제공
    public List<Object> getReviews() { return Collections.emptyList(); } // TMAP 미제공
    public String getPhotoReference() { return null; } // TMAP 미제공

    @Override
    public String toString() {
        return "PlaceInfo {" +
                "\n Name: '" + name + '\'' +
                ",\n Address: '" + address + '\'' +
                ",\n Latitude: " + latitude +
                ",\n Longitude: " + longitude +
                "\n}";
    }
}
