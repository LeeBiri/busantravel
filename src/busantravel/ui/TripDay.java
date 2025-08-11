package busantravel.ui;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JOptionPane;

public class TripDay {
    private String displayName; // "1일차(8월 8일 금요일)"
    private LocalDate date;
    private List<Place> places = new ArrayList<>(); // 해당 일차에 포함된 장소 리스트

    public TripDay(String displayName, LocalDate date) {
        this.displayName = displayName;
        this.date = date;
    }

    // --- Getter 메서드 ---
    public String getDisplayName() {
        return displayName;
    }

    public LocalDate getDate() {
        return date;
    }

    // 외부에서 places 리스트를 직접 수정하는 것을 방지하기 위해 UnmodifiableList로 반환
    public List<Place> getPlaces() {
        return Collections.unmodifiableList(places);
    }

    // --- 장소 조작 메서드 ---
    public void addPlace(Place place) {
        if (place != null && !places.contains(place)) { // 중복 추가 방지
            places.add(place);
        }
    }

    public void removePlace(Place place) {
        if (place != null) {
            places.remove(place);
        }
    }

    // 특정 인덱스의 장소를 새로운 장소로 교체
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
