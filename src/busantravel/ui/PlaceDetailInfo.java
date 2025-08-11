package busantravel.ui;

public class PlaceDetailInfo {
    private String name;
    private String description;
    private String imagePath; // 이미지 파일 경로 (예: "haeundae.jpg")
    private String transportation;
    private String restDay;
    private String address;

    public PlaceDetailInfo(String name, String description, String imagePath, String transportation, String restDay, String address) {
        this.name = name;
        this.description = description;
        this.imagePath = imagePath;
        this.transportation = transportation;
        this.restDay = restDay;
        this.address = address;
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
}
