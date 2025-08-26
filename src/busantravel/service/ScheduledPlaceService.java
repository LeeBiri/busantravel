package busantravel.service;

import busantravel.dao.ScheduledPlaceDAO;
import busantravel.model.ScheduledPlace;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 여행 일정(ScheduledPlace) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 DAO(Data Access Object)를 사용하여 데이터베이스와 상호작용하며,
 * UI 계층에 필요한 데이터를 제공하거나 데이터 변경을 요청합니다.
 */
public class ScheduledPlaceService {

    private static final ScheduledPlaceDAO scheduledPlaceDAO = new ScheduledPlaceDAO();

    /**
     * 특정 여행 계획(plan)에 속한 모든 예정된 장소 목록을 가져옵니다.
     *
     * @param planId 조회할 여행 계획의 ID
     * @return 해당 여행 계획에 포함된 ScheduledPlace 객체의 리스트
     */
    public static List<ScheduledPlace> getSchedulesByPlanId(String planId) {
        return scheduledPlaceDAO.findSchedulesByPlanId(planId);
    }

    /**
     * 새로운 여행 일정을 데이터베이스에 추가합니다.
     *
     * @param schedule 추가할 ScheduledPlace 객체
     */
    public static void insertSchedule(ScheduledPlace schedule) {
        scheduledPlaceDAO.insertSchedule(schedule);
    }

    /**
     * 여러 개의 새로운 여행 일정을 한 번에 데이터베이스에 추가합니다.
     *
     * @param schedules 추가할 ScheduledPlace 객체들의 리스트
     */
    public static void insertSchedule(List<ScheduledPlace> schedules) {
        for (ScheduledPlace schedule : schedules) {
            scheduledPlaceDAO.insertSchedule(schedule);
        }
    }

    /**
     * 기존 여행 일정을 데이터베이스에서 업데이트합니다.
     *
     * @param schedule 업데이트할 정보가 담긴 ScheduledPlace 객체
     */
    public static void updateSchedule(ScheduledPlace schedule) {
        scheduledPlaceDAO.updateSchedule(schedule);
    }

    /**
     * 기존 여행 일정을 데이터베이스에서 삭제합니다.
     *
     * @param schedule 삭제할 ScheduledPlace 객체
     */
    public static void deleteSchedule(ScheduledPlace schedule) {
        scheduledPlaceDAO.deleteSchedule(schedule);
    }

    /**
     * 특정 여행 계획에 대한 모든 일정을 저장합니다.
     * 기존의 모든 일정을 삭제하고 새로운 일정 목록으로 대체합니다.
     *
     * @param planId    저장할 여행 계획의 ID
     * @param schedules 새로운 ScheduledPlace 객체들의 리스트
     */
    public static void saveSchedulesForPlan(String planId, List<ScheduledPlace> newSchedules) {
        // 1. 기존 메모 백업
        Map<String, String> memoMap = new HashMap<>();
        List<ScheduledPlace> oldSchedules = getSchedulesByPlanId(planId);
        for (ScheduledPlace old : oldSchedules) {
            String key = old.getP_type() + ":" + old.getP_id() + ":" + old.getScheduled_date().toLocalDate() + ":" + old.getVisit_order();
            memoMap.put(key, old.getSchedule_memo());
        }

        // 2. 기존 삭제
        scheduledPlaceDAO.deleteAllByPlanId(planId);

        // 3. 새로 insert (이때 기존 메모가 있다면 복구)
        for (ScheduledPlace sp : newSchedules) {
            String key = sp.getP_type() + ":" + sp.getP_id() + ":" + sp.getScheduled_date().toLocalDate() + ":" + sp.getVisit_order();
            if (memoMap.containsKey(key) && sp.getSchedule_memo() == null) {
                sp.setSchedule_memo(memoMap.get(key));
            }
            scheduledPlaceDAO.insertSchedule(sp);
        }
    }



    public static void updateDayScheduleOrder(String planId, LocalDate date, List<ScheduledPlace> newSchedule) {
        scheduledPlaceDAO.updateScheduleOrder(newSchedule);
    }
}
