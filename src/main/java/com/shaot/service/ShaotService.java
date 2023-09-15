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
import com.shaot.dto.company.CompanyTestAlarmPoint;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWageDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.company.SendMessageDto;
import com.shaot.dto.worker.MessageAnswerDto;
import com.shaot.dto.worker.WorkerDto;
import com.shaot.dto.worker.WorkerForCompanyDto;
import com.shaot.dto.worker.WorkerForCompanyView;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.dto.worker.WorkerUpdateDto;
import com.shaot.dto.worker.WorkerView;
import com.shaot.model.CompanyMessage;
import com.shaot.model.ShaotLoginUser;
import com.shaot.model.Worker;
import com.shaot.model.WorkerMessage;
import com.shaot.schedule.generator.DayView;
import com.shaot.schedule.generator.GeneratorShift;
import com.shaot.schedule.generator.ShiftView;

public interface ShaotService {
	
	//Authorization
	ShaotLoginUser login(String mail);
	ShaotLoginUser exit(String mail);
	
	//Admin
	WorkerView deleteWorkerFromDataBase(long workerId);
	CompanyView deleteCompanyFromDataBase(long companyId);
	List<WorkerView> getAllWorkers();
	List<CompanyView> getAllCompanies();
	
	//Worker
	WorkerView addWorker(WorkerDto workerDto);
	
	WorkerView findWorker (long id);
	
	WorkerView updateWorker(WorkerUpdateDto workerUpdateDto, long id);
	
	void sendPrefers (List<WorkerPreferShiftsDto> workerPreferShiftsDto, long workerId, long companyId);
	
	WorkerView addCompanyToWorker (long workerId, long companyId);
	
	List<WorkerShiftView> getWeeklySchedule(long companyId, long workerId);
	
	List<WorkerMessage> getWorkerMessages(Long workerId);
	
	WorkerMessage getWorkerMessageById(Long workerId, Long messageId);
	
	WorkerMessage answerMessage(Long workerId, Long messageId, MessageAnswerDto messageAnswerDto);
	
	
	//Company
	
	List<String> getWeekNames(long companyId);
	
	List<DayView> getWeekByPeriod(long companyId, String period);
	
	CompanyView addCompanyToRepository(CompanyDto companyDto);
	
	CompanyView findCompany(long id);
	
	CompanyView addWorkerToCompany(long companyId, long workerId);

	CompanyView updateCompany(CompanyUpdateDto companyUpdateDto, long id);

	CompanyView removeWorkerFromCompany(long companyId, long workerId);
	
	WorkerForCompanyView setIndividualWage (long companyId, long workerId, CompanyWageDto companyWageDto);
	
	List<WorkerForCompanyView> setGeneralWage(long companyId, CompanyWageDto companyWageDto);

	Set<DayView> generateSchedule(long companyId);

	Set<CompanyShiftDto> addShift(long companyId, CompanyAddShiftDto companyShiftAddShiftDto);

	Set<CompanyShiftDto> addWorkingDay(long companyId, CompanyAddWorkingDay companyAddWorkingDayDto);

	Set<CompanyShiftDto> removeShift(long companyId, CompanyRemoveShiftDto companyRemoveShiftDto);

	Set<CompanyShiftDto> removeWorkingDay(long companyId, CompanyRemoveWorkingDayDto removeWorkingDayDto);
	
	Set<DayView> generateEmptyWeek(long companyId, ScheduleConfigurationDto companyWeekGeneratorDto);
	
	ScheduleConfigurationDto configurateSchedule(long companyId, ScheduleConfigurationDto configuration);
	
	WorkerMessage sendMessage(Long companyId, Long workerId, SendMessageDto message);
	
	List<CompanyMessage> getAllCompanyMessages(Long companyId);
	
	CompanyMessage getCompanyMessageById(Long companyId, Long messageId);
	
	List<CompanyMessage> getCompanyMessageByWorkerId(Long companyId, Long workerId);
	
	ScheduleConfigurationDto getCompanyConfiguration(long companyId);
}
