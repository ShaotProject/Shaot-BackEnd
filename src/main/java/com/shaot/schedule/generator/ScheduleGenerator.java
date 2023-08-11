package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.model.Worker;

public interface ScheduleGenerator {
	
	void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shift);
	
	Set<ShiftView> generateSchedule();
	
	Set<GeneratorShift> generateWeek(ScheduleConfigurationDto weekGenerator);
	
	List<WorkerPreferShiftsDto> getWorkerPrefers(Long id);
	
	void addShift(CompanyAddShiftDto companyAddShiftDto);
	
	void addWorkingDay(CompanyAddWorkingDay companyAddWorkingDay);
	
	void removeShift(CompanyRemoveShiftDto companyRemoveShiftDto);
	
	void removeWorkingDay(CompanyRemoveWorkingDayDto companyRemoveWorkingDayDto);

	List<WorkerShiftView> getWorkerSchedule(Worker worker);
	
}
