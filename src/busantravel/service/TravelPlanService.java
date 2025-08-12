package busantravel.service;

import busantravel.dao.TravelPlanDAO;
import busantravel.dao.TravelPlanMemberDAO;

import java.util.List;
import java.util.UUID;

import busantravel.model.TravelPlan;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class TravelPlanService {

	// 멤버의 모든 계획 가져오기
    public static List<TravelPlan> getAllPlansByMember(String memberId) {
    	return TravelPlanDAO.findAllByMember(memberId);
    }

    // 멤버의 진행 예정 계획 가져오기
    public static List<TravelPlan> getUpcomingPlansByMember(String memberId) {
    	LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
    	return TravelPlanDAO.findUpcomingByMember(memberId, todayEnd);
    }

    // 멤버의 이미 끝난 계획 가져오기
	public static List<TravelPlan> getPastPlansByMember(String memberId) {
	    LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
	    return TravelPlanDAO.findPastByMember(memberId, todayEnd);
	}

	// 계획 생성
	public static void createPlan(String creatorId, String title, LocalDate start, LocalDate end) {
	    String planId = generatePlanId();
	    TravelPlan plan = new TravelPlan(
	        planId,
	        creatorId,
	        title,
	        start.atStartOfDay(),
	        end.atStartOfDay()
	    );
	    TravelPlanDAO.insert(plan);
	    TravelPlanMemberDAO.insert(planId, creatorId, null);
	}

	//계획 ID만드는 메소드. ***************중복처리도 추가해야댐****************
	private static String generatePlanId() {
	    return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
	}

}
