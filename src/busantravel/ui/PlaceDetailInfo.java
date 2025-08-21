package busantravel.ui;

/**
 * 장소의 상세 정보를 담는 데이터 객체(DTO) 클래스입니다.
 * 이름, 설명, 이미지 경로, 교통 정보, 휴무일, 주소, 메뉴, 전화번호 등
 * 장소에 대한 구체적인 정보를 관리합니다.
 */
public class PlaceDetailInfo {
    private String name;
    private String description;
    private String imagePath; // 이미지 파일 경로 (예: "haeundae.jpg")
    private String transportation;
    private String restDay;
    private String address;
    private String menu;
    private String phone;

    /**
     * PlaceDetailInfo 객체를 생성합니다. (메뉴와 전화번호 제외)
     *
     * @param name           장소 이름
     * @param description    장소 설명
     * @param imagePath      이미지 경로
     * @param transportation 교통 정보
     * @param restDay        휴무일 정보
     * @param address        주소
     */
    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address) {
        this(name, description, imagePath, transportation, restDay, address, null, null);
    }

    /**
     * PlaceDetailInfo 객체를 생성합니다. (전화번호 제외)
     *
     * @param name           장소 이름
     * @param description    장소 설명
     * @param imagePath      이미지 경로
     * @param transportation 교통 정보
     * @param restDay        휴무일 정보
     * @param address        주소
     * @param menu           메뉴 정보
     */
    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address, String menu) {
        this(name, description, imagePath, transportation, restDay, address, menu, null);
    }

    /**
     * 모든 정보를 포함하는 PlaceDetailInfo 객체를 생성합니다.
     *
     * @param name           장소 이름
     * @param description    장소 설명
     * @param imagePath      이미지 경로
     * @param transportation 교통 정보
     * @param restDay        휴무일 정보
     * @param address        주소
     * @param menu           메뉴 정보
     * @param phone          전화번호
     */
    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address, String menu, String phone) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.transportation = transportation;
        this.restDay = restDay;
        this.address = address;
        this.menu = menu;
        this.phone = phone;
    }

    /** @return 장소 이름 */
    public String getName() {
        return name;
    }

    /** @return 장소 설명 */
    public String getDescription() {
        return description;
    }

    /** @return 이미지 경로 */
    public String getImagePath() {
        return imagePath;
    }

    /** @return 교통 정보 */
    public String getTransportation() {
        return transportation;
    }

    /** @return 휴무일 정보 */
    public String getRestDay() {
        return restDay;
    }

    /** @return 주소 */
    public String getAddress() {
        return address;
    }

    /** @return 메뉴 정보 */
    public String getMenu() {
        return menu;
    }

    /** @return 전화번호 */
    public String getPhone() {
        return phone;
    }
}
