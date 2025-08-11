package busantravel.util;

/**
 * TMAP 통합 검색 API의 예상 검색어 결과를 저장하는 데이터 모델 클래스입니다.
 * <p>
 * 각 예측은 장소에 대한 이름(description)과 주소(address)를 포함합니다.
 *
 * @see ApiHandler#getAutocompletePredictions(String, String)
 */
public class AutocompletePrediction {

    private final String description;
    private final String address;

    public AutocompletePrediction(String description, String address) {
        this.description = description;
        this.address = address;
    }

    /**
     * API가 제공하는 장소 이름(description)을 반환합니다.
     * 예: "롯데월드타워"
     * @return 장소 이름 문자열
     */
    public String getDescription() {
        return description;
    }

    /**
     * 장소의 주소를 반환합니다.
     * @return 주소 문자열
     */
    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return description; // UI 표시를 위해 이름만 반환
    }
}
