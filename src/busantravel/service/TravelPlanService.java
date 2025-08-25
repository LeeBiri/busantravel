package busantravel.service;

import busantravel.dao.TravelPlanDAO;
import busantravel.dao.TravelPlanMemberDAO;
import busantravel.model.TravelPlan;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TravelPlanService {

    private static TravelPlanDAO travelPlanDAO;

    public TravelPlanService() {
        this.travelPlanDAO = new TravelPlanDAO();
    }

	// 멤버의 모든 계획 가져오기
    public static List<TravelPlan> getAllPlansByMember(String memberId) {
    	return travelPlanDAO.findAllByMember(memberId);
    }

    // 멤버의 진행 예정 계획 가져오기
    public static List<TravelPlan> getUpcomingPlansByMember(String memberId) {
    	LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
    	return travelPlanDAO.findUpcomingByMember(memberId, todayEnd);
    }

    // 멤버의 이미 끝난 계획 가져오기
	public static List<TravelPlan> getPastPlansByMember(String memberId) {
	    LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
	    return travelPlanDAO.findPastByMember(memberId, todayEnd);
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
	    travelPlanDAO.insert(plan);
	    TravelPlanMemberService.insert(planId, creatorId);
	}

	// 계획 ID 중복 체크
	private static boolean dupCheck(String planId) {
		return travelPlanDAO.findById(planId) != null;
	}

	//계획 ID만드는 메소드. 
	private static String generatePlanId() {
	    String planId;
		do {
			planId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		} while (dupCheck(planId));
		return planId;
	}

	public static TravelPlan findById(String planId) {
		return travelPlanDAO.findById(planId);
	}

    public static void deletePlan(String planId) {
        // First, delete all members associated with the plan
        TravelPlanMemberDAO.deleteByPlanId(planId);
        // Then, delete the plan itself
        travelPlanDAO.deleteById(planId);
    }
}
