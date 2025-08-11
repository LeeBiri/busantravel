package busantravel.ui;

public class PlaceDetailInfo {
    private String name;
    private String description;
    private String imagePath; // 이미지 파일 경로 (예: "haeundae.jpg")
    private String transportation;
    private String restDay;
    private String address;
    private String menu;
    private String phone;

    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address) {
        this(name, description, imagePath, transportation, restDay, address, null, null);
    }

    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address, String menu) {
        this(name, description, imagePath, transportation, restDay, address, menu, null);
    }

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

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getTransportation() {
        return transportation;
    }

    public String getRestDay() {
        return restDay;
    }

    public String getAddress() {
        return address;
    }

    public String getMenu() {
        return menu;
    }

    public String getPhone() {
        return phone;
    }
}
