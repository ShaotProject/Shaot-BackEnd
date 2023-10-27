package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.util.List;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.model.Worker;

public interface ScheduleGenerator {
	
	void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shift, int priority);
	
	List<DayView> generateSchedule();
	
	List<String> getLastWeeksNames();
	
	List<WorkerPreferShiftsDto> getWeeklyPosibilities();
	
	List<DayView> getWeekByPeriod(String period);
	
	List<ScheduleConfigurationDto> generateWeek(List<ScheduleConfigurationDto> weekGenerator);
	
	void addShift(CompanyAddShiftDto companyAddShiftDto);
	
	void addWorkingDay(CompanyAddWorkingDay companyAddWorkingDay);
	
	void removeShift(CompanyRemoveShiftDto companyRemoveShiftDto);
	
	void removeWorkingDay(CompanyRemoveWorkingDayDto companyRemoveWorkingDayDto);

	List<WorkerShiftView> getWorkerSchedule(Worker worker);
	
}
