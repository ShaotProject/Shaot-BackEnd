package com.shaot.service;

import java.util.List;
import java.util.Map;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyDto;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.schedule.GeneratorShiftDto;
import com.shaot.dto.worker.WorkerDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerScheduleDto;
import com.shaot.dto.worker.WorkerUpdateDto;
import com.shaot.model.Worker;
import com.shaot.schedule.generator.GeneratorShift;
import com.shaot.schedule.generator.ShiftView;

public interface ShaotService {
	
	//Worker
	Worker addWorker(WorkerDto workerDto);
	
	Worker findWorker (long id);
	
	Worker updateWorker(WorkerUpdateDto workerUpdateDto, long id);
	
	List<WorkerPreferShiftsDto> sendPrefers (List<WorkerPreferShiftsDto> workerPreferShiftsDto, long workerId, long companyId);
	
	Worker addCompanyToWorker (long workerId, long companyId);
	
	List<WorkerScheduleDto> getWeeklySchedule(long workerId);
	
	
	//Company
	public CompanyView addCompanyToRepository(CompanyDto companyDto);
	
	public CompanyView findCompany(long id);
	
	CompanyView addWorkerToCompany(long companyId, long workerId);

	CompanyView updateCompany(CompanyUpdateDto companyUpdateDto, long id);

	CompanyView removeWorkerFromCompany(long companyId, long workerId);

	Map<String, List<ShiftView>> generateSchedule(long companyId);

	List<CompanyShiftDto> addShift(long companyId, CompanyAddShiftDto companyShiftAddShiftDto);

	List<CompanyShiftDto> addWorkingDay(long companyId, CompanyAddWorkingDay companyAddWorkingDayDto);

	List<CompanyShiftDto> removeShift(long companyId, CompanyRemoveShiftDto companyRemoveShiftDto);

	List<CompanyShiftDto> removeWorkingDay(long companyId, CompanyRemoveWorkingDayDto removeWorkingDayDto);
	
	Map<String, List<GeneratorShift>> generateEmptyWeek(long companyId, CompanyWeekGeneratorDto companyWeekGeneratorDto);
	
	Map<String, List<GeneratorShift>> configurateSchedule(long companyId, ScheduleConfigurationDto configuration);
	
	
}
