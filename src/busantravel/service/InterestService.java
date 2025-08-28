package busantravel.service;

import busantravel.auth.MemberSession;
import busantravel.dao.InterestDAO;
import busantravel.model.Interest;
import java.util.List;

public class InterestService {
    private InterestDAO interestDAO;

    public InterestService() {
        this.interestDAO = new InterestDAO();
    }

    public void insertInterest(String m_id, String p_id, String p_type) {
        InterestDAO.insertInterest(MemberSession.getId(), p_id, p_type);
    }

    public List<Interest> getInterestById(String m_id) {
        return InterestDAO.getInterestById(m_id);
    }

    public void deleteInterest(Interest intereset) {
        InterestDAO.deleteInterest(intereset);
    }

    public int getInterestCountByPlace(String placeId, String placeType) {
        return interestDAO.countInterestsByPlace(placeId, placeType);
    }
}
