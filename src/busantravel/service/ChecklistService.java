package busantravel.service;

import busantravel.dao.ChecklistDAO;
import busantravel.model.Checklist;
import java.util.List;

public class ChecklistService {
    public static void insertChecklist(String plan_id, String creator_id, String checklist_content) {
        ChecklistDAO.insertChecklist(plan_id, creator_id, checklist_content);
    }

    public static void insertPublicChecklist(String plan_id, String creator_content) {
        ChecklistDAO.insertPublicChecklist(plan_id, creator_content);
    }

    public static List<Checklist> getChecklistByid(String plan_id, String creator_id) {
        return ChecklistDAO.getChecklistByid(plan_id, creator_id);
    }

    public static List<Checklist> getPublicChecklist(String plan_id) {
        return ChecklistDAO.getPublicChecklist(plan_id);
    }

    public static void completeChecklist(int checklist_id, boolean flag) {
        ChecklistDAO.completeChecklist(checklist_id, flag);
    }

    public static void updateChecklist(int checklist_id, String checklist_content) {
        ChecklistDAO.updateChecklist(checklist_id, checklist_content);
    }

    public static void deleteChecklist(int checklist_id) {
        ChecklistDAO.deleteChecklist(checklist_id);
    }
}
