package busantravel.service;

import busantravel.auth.MemberSession;
import busantravel.dao.InterestDAO;
import busantravel.model.Interest;
import java.util.List;

public class InterestService {
    public static void insertInterest(String m_id, String p_id, String p_type) {
        InterestDAO.insertInterest(MemberSession.getId(), p_id, p_type);
    }

    public static List<Interest> getInterestById(String m_id) {
        return InterestDAO.getInterestById(MemberSession.getId());
    }

    public static void deleteInterest(Interest intereset) {
        InterestDAO.deleteInterest(intereset);
    }
}
