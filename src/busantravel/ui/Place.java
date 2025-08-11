package busantravel.ui;

import java.util.Objects;

public class Place {
    private String photoUrl; // 이미지 파일 경로 (예: "images/haeundae.jpg")
    private String name;     // 장소명 (예: "해운대해수욕장")
    private String category; // 분류 (예: "관광지", "음식점")
    private String location; // 위치 상세 (예: "해운대구 중제1동")

    public Place(String photoUrl, String name, String category, String location) {
        this.photoUrl = photoUrl;
        this.name = name;
        this.category = category;
        this.location = location;
    }

    // --- Getter 메서드 ---
    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getLocation() {
        return location;
    }

    // --- Object 클래스 오버라이드 (핵심!) ---
    // List.remove(Object), contains(), Collections.unmodifiableList() 등
    // 컬렉션에서 객체 동등성을 판단할 때 사용됩니다.
    // 여기서는 name과 location이 같으면 같은 장소로 간주합니다.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Place place = (Place) o;
        return Objects.equals(name, place.name) &&
               Objects.equals(location, place.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location);
    }

    @Override
    public String toString() {
        return "Place{" +
               "name='" + name + '\'' +
               ", location='" + location + '\'' +
               '}';
    }
}
