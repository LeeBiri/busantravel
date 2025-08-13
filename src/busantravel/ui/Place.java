package busantravel.ui;

import java.util.Objects;

/**
 * 여행 장소에 대한 정보를 담는 데이터 객체(DTO) 클래스입니다.
 * 장소의 사진 URL, 이름, 카테고리, 위치 정보를 포함합니다.
 * 객체의 동등성은 '이름'과 '위치'를 기준으로 판단합니다.
 */
public class Place {
    private String photoUrl; // 이미지 파일 경로 (예: "images/haeundae.jpg")
    private String name;     // 장소명 (예: "해운대해수욕장")
    private String category; // 분류 (예: "관광지", "음식점")
    private String location; // 위치 상세 (예: "해운대구 중제1동")

    /**
     * Place 객체의 새 인스턴스를 생성합니다.
     *
     * @param photoUrl 장소의 사진 URL
     * @param name     장소의 이름
     * @param category 장소의 카테고리
     * @param location 장소의 위치
     */
    public Place(String photoUrl, String name, String category, String location) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.category = category;
        this.location = location;
    }

    // --- Getter 메서드 ---

    /**
     * 장소의 사진 URL을 반환합니다.
     * @return 사진 URL 문자열
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * 장소의 이름을 반환합니다.
     * @return 장소 이름 문자열
     */
    public String getName() {
        return name;
    }

    /**
     * 장소의 카테고리를 반환합니다.
     * @return 카테고리 문자열
     */
    public String getCategory() {
        return category;
    }

    /**
     * 장소의 위치를 반환합니다.
     * @return 위치 문자열
     */
    public String getLocation() {
        return location;
    }

    /**
     * 두 Place 객체가 동일한지 비교합니다.
     * 이름과 위치가 모두 같을 경우 동일한 객체로 간주합니다.
     * 이 메서드는 컬렉션에서 객체의 동등성을 판단하는 데 사용됩니다.
     *
     * @param o 비교할 객체
     * @return 두 객체의 이름과 위치가 같으면 true, 그렇지 않으면 false
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Objects.equals(name, place.name) &&
               Objects.equals(location, place.location);
    }

    /**
     * 객체의 해시코드를 반환합니다.
     * 이름과 위치를 기반으로 해시코드를 생성합니다.
     *
     * @return 객체의 해시코드
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, location);
    }

    /**
     * 객체의 문자열 표현을 반환합니다.
     * 디버깅 및 로깅 목적으로 사용됩니다.
     *
     * @return "Place{name='...', location='...'}" 형식의 문자열
     */
    @Override
    public String toString() {
        return "Place{" +
               "name='" + name + '\'' +
               ", location='" + location + '\'' +
               '}';
    }
}
