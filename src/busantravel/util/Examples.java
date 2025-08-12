package busantravel.util;

import java.util.List;

public class Examples {

    public static void main(String[] args) {
        // ========================================================================
        // TMAP API 관련 설정
        // ========================================================================
        ApiHandler apiHandler = new ApiHandler();
        String tmapApiKey = "h5baCQSfp37eFmXohbwx23t7n1KLSIcn3RyFtMcg";
        
        // --- 1. 장소 자동 완성 ---
        System.out.println("--- 1. 장소 자동 완성 ---");
        String searchInput = "롯데월드";
        List<AutocompletePrediction> predictions = apiHandler.getAutocompletePredictions(searchInput, tmapApiKey);
        if (!predictions.isEmpty()) {
            System.out.println("'" + searchInput + "'에 대한 예상 검색어:");
            for (AutocompletePrediction p : predictions) {
                System.out.println(" - " + p.getDescription());
            }
        } else {
            System.out.println("예상 검색어를 찾지 못했습니다.");
        }

        System.out.println("\n============================================\n");

        // --- 2. 장소 상세 정보 ---
        System.out.println("--- 2. 장소 상세 정보 ---");
        String placeToSearch = "해운대해수욕장";
        PlaceInfo placeInfo = apiHandler.getPlaceDetailsByName(placeToSearch, tmapApiKey);

        if (placeInfo != null) {
            System.out.println("'" + placeToSearch + "'의 정보:");
            System.out.println(" - 이름: " + placeInfo.getName());
            System.out.println(" - 주소: " + placeInfo.getAddress());
            System.out.println(" - 좌표: " + placeInfo.getLatitude() + ", " + placeInfo.getLongitude());
        } else {
            System.out.println("\n'" + placeToSearch + "'에 대한 정보를 가져오는 데 실패했습니다.");
        }

        System.out.println("\n============================================\n");

        // ========================================================================
        // Tmap 경로 API 관련 설정
        // ========================================================================
        TmapApiHandler tmapApiHandler = new TmapApiHandler(tmapApiKey);
        
        TmapRouteResult routeResult = tmapApiHandler.getRouteInfo(
            tmapApiKey,
            129.223088, 35.188301, 
            129.042206, 35.114777,
            "해동용궁사", "부산역"
        );

        // --- 3. 지점 간 소요시간, 비용 안내 (Tmap) ---
        System.out.println("--- 3. Tmap 경로 탐색 결과 ---");
        if (routeResult != null) {
            TmapRouteInfo summary = routeResult.getSummary();
            System.out.printf("총 거리: %.2f km\n", summary.getTotalDistance() / 1000.0);

            int totalSeconds = summary.getTotalTime();
            int hours = totalSeconds / 3600;
            int minutes = (totalSeconds % 3600) / 60;
            System.out.printf("총 소요 시간: %d시간 %d분\n", hours, minutes);

            System.out.printf("총 통행 요금: %,d 원\n", summary.getTotalFare());
            System.out.printf("예상 택시 요금: %,d 원\n", summary.getTaxiFare());
        } else {
            System.out.println("Tmap 경로 정보를 가져오는 데 실패했습니다.");
        }

        System.out.println("\n============================================\n");

        // --- 4. 핀이 표시된 Static Map을 GUI 창에 표시 ---
        System.out.println("--- 4. 핀이 표시된 Static Map 표시 ---");
        if (placeInfo != null) {
            StaticMapGenerator mapGenerator = new StaticMapGenerator(tmapApiKey);
            Coordinate pinCoordinate = new Coordinate(placeInfo.getLatitude(), placeInfo.getLongitude());
            String mapUrl = mapGenerator.generateMapWithPinUrl(pinCoordinate, 540, 480, 15);
            if (mapUrl != null) {
                MapViewer.showMap(mapUrl, tmapApiKey);
            } else {
                System.out.println("지도 URL 생성에 실패했습니다.");
            }
        } else {
            System.out.println("장소 정보가 없어 지도를 표시할 수 없습니다.");
        }

        System.out.println("\n============================================\n");

        // --- 5. Tmap 경로 API를 사용하여 그린 지도를 HTML 파일로 확인 ---
        System.out.println("--- 5. Tmap 경로 API 지도 예제 ---");
        MapViewer.showHtmlFile("doc/tmap_route_example.html");
    }
}
