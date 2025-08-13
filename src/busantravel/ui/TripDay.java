package busantravel.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

/**
 * 여행 계획 중 특정 하루의 일정을 관리하는 클래스입니다.
 * 해당 날짜와 그날 방문할 장소 목록을 포함합니다.
 */
public class TripDay {
    private String displayName; // UI에 표시될 날짜 문자열 (예: "1일차(8월 8일 금요일)")
    private LocalDate date; // 실제 날짜
    private List<Place> places = new ArrayList<>(); // 해당 일차에 포함된 장소 리스트

    /**
     * TripDay의 새 인스턴스를 생성합니다.
     *
     * @param displayName UI에 표시될 형식의 날짜 문자열
     * @param date        해당 일차의 LocalDate 객체
     */
    public TripDay(String displayName, LocalDate date) {
        this.displayName = displayName;
        this.date = date;
    }

    // --- Getter 메서드 ---

    /**
     * UI에 표시될 날짜 문자열을 반환합니다.
     * @return 형식화된 날짜 문자열
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 해당 일차의 실제 날짜를 반환합니다.
     * @return LocalDate 객체
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * 해당 일차에 계획된 장소 목록을 반환합니다.
     * 외부에서의 직접적인 수정을 방지하기 위해 수정 불가능한 리스트(UnmodifiableList)로 반환됩니다.
     * @return 수정 불가능한 Place 객체 리스트
     */
    public List<Place> getPlaces() {
        return Collections.unmodifiableList(places);
    }

    // --- 장소 조작 메서드 ---

    /**
     * 해당 일차에 새로운 장소를 추가합니다.
     * 중복된 장소는 추가되지 않습니다.
     *
     * @param place 추가할 Place 객체
     */
    public void addPlace(Place place) {
        if (place != null && !places.contains(place)) { // 중복 추가 방지
            places.add(place);
        }
    }

    /**
     * 해당 일차에서 특정 장소를 제거합니다.
     *
     * @param place 제거할 Place 객체
     */
    public void removePlace(Place place) {
        if (place != null) {
            places.remove(place);
        }
    }

    /**
     * 특정 위치(인덱스)의 장소를 새로운 장소로 교체합니다.
     * 교체하려는 장소가 이미 목록에 존재할 경우, 경고 메시지를 표시하고 교체하지 않습니다.
     *
     * @param index    교체할 장소의 리스트 내 인덱스
     * @param newPlace 새로 추가될 Place 객체
     */
    public void replacePlace(int index, Place newPlace) {
        if (index >= 0 && index < places.size() && newPlace != null) {
            if (!places.contains(newPlace)) { // 교체하려는 장소가 현재 리스트에 이미 없는 경우에만
                 places.set(index, newPlace);
            } else {
                JOptionPane.showMessageDialog(null, "선택하신 장소가 이미 해당 일차에 추가되어 있습니다.", "중복 알림", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            System.err.println("경고: 유효하지 않은 인덱스 또는 null Place 객체로 교체 시도: index=" + index + ", newPlace=" + newPlace);
        }
    }
}
