package busantravel.service;

import java.util.List;

import busantravel.dao.ScheduledPlaceDAO;
import busantravel.model.ScheduledPlace;

public class ScheduledPlaceService {
	
	
	public static void insertSchedule(List<ScheduledPlace> scheduledPlaces) {
		for (ScheduledPlace scheduledPlace : scheduledPlaces) {
			ScheduledPlaceDAO.insertSchedule(scheduledPlace);
		}
	}
	
	public static List<ScheduledPlace> getSchedulesByPlanId(String planId) {
		return ScheduledPlaceDAO.searchByPlanId(planId);
	}
	
	public static void deleteSchedule(ScheduledPlace schedule) {
		ScheduledPlaceDAO.deleteSchedule(schedule);
	}
}
