package busantravel.service;

import busantravel.dao.TravelPlanDAO;
import busantravel.model.TravelPlan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
	    TravelPlanMemberService.insert(planId, creatorId);
	}

	// 계획 ID 중복 체크
	private static boolean dupCheck(String planId) {
		return TravelPlanDAO.findById(planId) != null;
	}

	//계획 ID만드는 메소드. 
	private static String generatePlanId() {
	    String planId;
		do {
			planId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} while (TravelPlanService.dupCheck(planId));
		return planId;
	}

	public static TravelPlan findById(String planId) {
		return TravelPlanDAO.findById(planId);
	}
}
