package busantravel.controller;

import busantravel.auth.MemberSession;
import busantravel.model.ScheduledPlace;
import busantravel.model.TravelPlan;
import busantravel.service.TravelPlanService;
import busantravel.ui.CreateTripDialog;
import busantravel.ui.EnterCodeDialog;
import busantravel.ui.LoginDialog;
import busantravel.ui.SchedulePanel;
import busantravel.ui.TravelPlannerApp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class PlanController {
    private JFrame parentFrame;
    private SchedulePanel schedulePanel;
    private String currentUserId; // 로그인 사용자 ID
    private TravelPlannerApp mainApp;

    public PlanController(JFrame parentFrame, SchedulePanel schedulePanel, String currentUserId, TravelPlannerApp mainApp) {
        this.parentFrame = parentFrame;
        this.schedulePanel = schedulePanel;
        this.currentUserId = currentUserId;
        this.mainApp = mainApp;
    }
    
    public void handleCreateTrip() {
        if (!MemberSession.isLoggedIn()) {
            int result = JOptionPane.showConfirmDialog(
                parentFrame,
                "로그인을 해야 이용할 수 있는 서비스입니다.\n로그인 하시겠습니까?",
                "로그인 필요",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                // 로그인 성공 시 다시 handleCreateTrip 실행
                new LoginDialog(parentFrame, mainApp, () -> handleCreateTrip()).showDialog();
            }
            return; 
        }

        // 로그인 되어있으면 바로 실행
        CreateTripDialog dialog = new CreateTripDialog(parentFrame);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            TravelPlanService.createPlan(
                MemberSession.getId(),
                dialog.getTitleInput(),
                dialog.getStartDate(),
                dialog.getEndDate()
            );
            refreshRoomLists();
        }
    }
    public void handleEnterCode() {
            if (!MemberSession.isLoggedIn()) {
            int result = JOptionPane.showConfirmDialog(
                parentFrame,
                "로그인을 해야 이용할 수 있는 서비스입니다.\n로그인 하시겠습니까?",
                "로그인 필요",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                // 로그인 성공 시 다시 handleCreateTrip 실행
                new LoginDialog(parentFrame, mainApp, () -> handleEnterCode()).showDialog();
            }
            return; 
        }
        EnterCodeDialog dialog = new EnterCodeDialog(parentFrame, this::refreshRoomLists);

        dialog.setVisible(true);
        refreshRoomLists();
    }

    /** 목록 새로고침: 서비스 호출 → 패널 렌더 */
    public void refreshRoomLists() {

        List<TravelPlan> allPlans = TravelPlanService.getAllPlansByMember(MemberSession.getId());

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
