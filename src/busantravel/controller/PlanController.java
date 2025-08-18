package busantravel.controller;

import busantravel.model.ScheduledPlace;
import busantravel.model.TravelPlan;
import busantravel.service.ScheduledPlaceService;
import busantravel.service.TravelPlanService;
import busantravel.ui.CreateTripDialog;
import busantravel.ui.EnterCodeDialog;
import busantravel.ui.SchedulePanel;
import busantravel.ui.TravelPlannerApp;
import busantravel.ui.TravelPlannerApp;

import javax.swing.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlanController {
    private JFrame parentFrame;
    private SchedulePanel schedulePanel;
    private String currentUserId; // 로그인 사용자 ID

    public PlanController(JFrame parentFrame, SchedulePanel schedulePanel, String currentUserId) {
        this.parentFrame = parentFrame;
        this.schedulePanel = schedulePanel;
        this.currentUserId = currentUserId;
    }
    
    public void handleCreateTrip() {
        CreateTripDialog dialog = new CreateTripDialog(parentFrame);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            TravelPlanService.createPlan(
                "aaa", // 로그인 미구현이므로 임시 생성자 ID
                dialog.getTitleInput(),
                dialog.getStartDate(),
                dialog.getEndDate()
            );
            refreshRoomLists();
        }
    }


    

    public void handleEnterCode() {
        EnterCodeDialog dialog = new EnterCodeDialog(parentFrame, schedulePanel);
        dialog.setVisible(true);
        // TODO: 코드로 공동 편집/참여 기능이 필요하면 별도 서비스/DAO에서 처리
        refreshRoomLists();
    }

    /** 목록 새로고침: 서비스 호출 → 패널 렌더 */
    public void refreshRoomLists() {
        List<TravelPlan> allPlans = TravelPlanService.getAllPlansByMember("aaa");

        LocalDateTime now = LocalDateTime.now();
        List<TravelPlan> upcoming = new ArrayList<TravelPlan>();
        List<TravelPlan> past = new ArrayList<>();

        for (TravelPlan plan : allPlans) {
            if (plan.getPlan_period_end().isBefore(now)) {
                past.add(plan);
            } else {
                upcoming.add(plan);
            }
        }

        schedulePanel.renderPlans(upcoming, past);
    }

    public void handleSavePlan(List<ScheduledPlace> scheduledPlaces, TravelPlannerApp mainApp) {

    }
}
