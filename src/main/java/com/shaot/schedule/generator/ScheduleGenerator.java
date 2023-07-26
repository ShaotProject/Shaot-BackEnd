package com.shaot.schedule.generator;

import java.util.List;
import java.util.Map;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.schedule.GeneratorShiftDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;

public interface ScheduleGenerator {
	
	void addPrefer(Long id, String workerName, WorkerPreferShiftsDto shift);
	
	Map<String, List<ShiftView>> generateSchedule();
	
	Map<String, List<GeneratorShift>> generateWeek(CompanyWeekGeneratorDto weekGenerator);
	
	List<WorkerPreferShiftsDto> getWorkerPrefers(Long id);
	
	void addShift(CompanyAddShiftDto companyAddShiftDto);
	
	void addWorkingDay(CompanyAddWorkingDay companyAddWorkingDay);
	
	void removeShift(CompanyRemoveShiftDto companyRemoveShiftDto);
	
	void removeWorkingDay(CompanyRemoveWorkingDayDto companyRemoveWorkingDayDto);
	
}
