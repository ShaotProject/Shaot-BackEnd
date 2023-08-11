package com.shaot.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyDto;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWageDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.worker.WorkerDto;
import com.shaot.dto.worker.WorkerForCompanyDto;
import com.shaot.dto.worker.WorkerForCompanyView;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.dto.worker.WorkerUpdateDto;
import com.shaot.dto.worker.WorkerView;
import com.shaot.model.Worker;
import com.shaot.schedule.generator.GeneratorShift;
import com.shaot.schedule.generator.ShiftView;

public interface ShaotService {
	
	//Admin
	WorkerView deleteWorkerFromDataBase(long workerId);
	CompanyView deleteCompanyFromDataBase(long companyId);
	List<WorkerView> getAllWorkers();
	List<CompanyView> getAllCompanies();
	
	//Worker
	WorkerView addWorker(WorkerDto workerDto);
	
	WorkerView findWorker (long id);
	
	WorkerView updateWorker(WorkerUpdateDto workerUpdateDto, long id);
	
	List<WorkerPreferShiftsDto> sendPrefers (List<WorkerPreferShiftsDto> workerPreferShiftsDto, long workerId, long companyId);
	
	WorkerView addCompanyToWorker (long workerId, long companyId);
	
	List<WorkerShiftView> getWeeklySchedule(long companyId, long workerId);
	
	
	//Company
	public CompanyView addCompanyToRepository(CompanyDto companyDto);
	
	public CompanyView findCompany(long id);
	
	CompanyView addWorkerToCompany(long companyId, long workerId);

	CompanyView updateCompany(CompanyUpdateDto companyUpdateDto, long id);

	CompanyView removeWorkerFromCompany(long companyId, long workerId);
	
	WorkerForCompanyView setIndividualWage (long companyId, long workerId, CompanyWageDto companyWageDto);
	
	List<WorkerForCompanyView> setGeneralWage(long companyId, CompanyWageDto companyWageDto);

	Set<ShiftView> generateSchedule(long companyId);

	Set<CompanyShiftDto> addShift(long companyId, CompanyAddShiftDto companyShiftAddShiftDto);

	Set<CompanyShiftDto> addWorkingDay(long companyId, CompanyAddWorkingDay companyAddWorkingDayDto);

	Set<CompanyShiftDto> removeShift(long companyId, CompanyRemoveShiftDto companyRemoveShiftDto);

	Set<CompanyShiftDto> removeWorkingDay(long companyId, CompanyRemoveWorkingDayDto removeWorkingDayDto);
	
	Set<ShiftView> generateEmptyWeek(long companyId, ScheduleConfigurationDto companyWeekGeneratorDto);
	
	Set<ShiftView> configurateSchedule(long companyId, ScheduleConfigurationDto configuration);
	
	
}
