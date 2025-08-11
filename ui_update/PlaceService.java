package busantravel;

import java.util.Arrays;
import java.util.List;

public class PlaceService {
    public static List<Place> getDummyPlaces() {
        return Arrays.asList(
                new Place("/busantravel/images/haeundae.jpg", "해운대해수욕장", "관광지", "해운대구 중제1동"),
                new Place("/busantravel/images/bupyeong.jpg", "부평깡통시장", "시장", "중구 부평1길"),
                new Place("/busantravel/images/gwangalli.jpg", "광안리해수욕장", "관광지", "수영구 광안2동"),
                new Place("/busantravel/images/songdo.jpg", "송도해상케이블카", "관광지", "서구 암남공원로"),
                new Place("/busantravel/images/gamcheon.jpg", "감천문화마을", "관광지", "사하구 감천2로"),
                new Place("/busantravel/images/jagalchi.jpg", "자갈치시장", "시장", "중구 자갈치해안로"),
                new Place("/busantravel/images/busan_tower.jpg", "부산타워", "랜드마크", "중구 용두산길"),
                new Place("/busantravel/images/cafe_rendezvous.jpg", "카페 랑데부", "카페", "전포대로209번길"),
                new Place("/busantravel/images/nampo.jpg", "남포동거리", "쇼핑", "중구 남포동")
        );
    }
}