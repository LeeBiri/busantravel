package busantravel.service;

import busantravel.dao.TravelPlanMemberDAO;

public class TravelPlanMemberService {
    public static void insert(String planId, String memberId) {
        TravelPlanMemberDAO.insert(planId, memberId);
    }
}
