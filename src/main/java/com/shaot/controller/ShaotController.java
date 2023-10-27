package com.shaot.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.shaot.dto.company.AlarmPoint;
import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyDto;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyTestAlarmPoint;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWageDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.PeriodDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.company.ScheduleConfigurationShiftTime;
import com.shaot.dto.company.SendMessageDto;
import com.shaot.dto.worker.MessageAnswerDto;
import com.shaot.dto.worker.WorkerDto;
import com.shaot.dto.worker.WorkerForCompanyDto;
import com.shaot.dto.worker.WorkerForCompanyView;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.dto.worker.WorkerUpdateDto;
import com.shaot.dto.worker.WorkerView;
import com.shaot.exceptions.AccessToCompanyRestrictedException;
import com.shaot.exceptions.AcessToWorkerRestrictedException;
import com.shaot.exceptions.CompanyAlreadyExistsException;
import com.shaot.exceptions.CompanyNotFoundException;
import com.shaot.exceptions.ResponseExceptionDto;
import com.shaot.exceptions.UserNotFoundException;
import com.shaot.exceptions.WorkerNotFoundException;
import com.shaot.model.CompanyMessage;
import com.shaot.model.ShaotLoginUser;
import com.shaot.model.WorkerMessage;
import com.shaot.schedule.generator.DayView;
import com.shaot.schedule.generator.ShiftView;
import com.shaot.service.ShaotService;

@RestController
@CrossOrigin(
		origins = {"http://localhost:3000", "https://master--precious-puffpuff-924a8c.netlify.app", "http://localhost:8080"},
		allowedHeaders = {"Authorization", "Origin", "Content-Type"},
		exposedHeaders = {"Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Access-Control-Allow-Headers"},
		maxAge = 3600
		)
public class ShaotController {
	
	@Autowired
	ShaotService service;
	
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseExceptionDto handleUserNotFoundException() {
		return new ResponseExceptionDto(404, "User not found");
	}
	
	@ExceptionHandler(CompanyAlreadyExistsException.class)
	public ResponseExceptionDto handleCompanyExistsException() {
		return new ResponseExceptionDto(403, "Company already exists");
	}
	
	@ExceptionHandler(CompanyNotFoundException.class)
	public ResponseExceptionDto handleCompanyNotFoundException() {
		return new ResponseExceptionDto(404, "Company not found");
	}
	
	@ExceptionHandler(WorkerNotFoundException.class)
	public ResponseExceptionDto handleWorkerNotFoundException() {
		return new ResponseExceptionDto(404, "Worker not dound");
	}
	
	@ExceptionHandler(AccessToCompanyRestrictedException.class)
	public ResponseExceptionDto handleAccessToCompanyRestrictedException() {
		return new ResponseExceptionDto(403, "Worker have no acess to the company");
	}
	
	@ExceptionHandler(AcessToWorkerRestrictedException.class)
	public ResponseExceptionDto handleAccessToWorkerRestrictedException() {
		return new ResponseExceptionDto(403, "Company have no acess to the worker");
	}
	
	@GetMapping("shaot/login")
	public ShaotLoginUser login(@RequestHeader("Authorization") String header) {
		String[] token = header.split(" ");
		String decoded = new String(Base64.getDecoder().decode(token[1]));
		String[] loginPass = decoded.split(":");
		return service.login(loginPass[0]);
	}
	
	@DeleteMapping("shaot/exit")
	public ShaotLoginUser exit(@RequestHeader("Authorization") String header) {
		String[] token = header.split(" ");
		String decoded = new String(Base64.getDecoder().decode(token[1]));
		String[] loginPass = decoded.split(":");
		return service.exit(loginPass[0]);
	}
	
	////////////////////////////////
	////////////Admin///////////////
	////////////////////////////////
	
	@GetMapping("shaot/workers")
	public List<WorkerView> getAllWorkers() {
		return service.getAllWorkers();
	}
	
	@GetMapping("shaot/companies")
	public List<CompanyView> getAllCompanies() {
		return service.getAllCompanies();
	}
	
	@DeleteMapping("shaot/company/{companyId}")
	public CompanyView deleteCompanyFromDataBase(@PathVariable long companyId) {
		return service.deleteCompanyFromDataBase(companyId);
	}
	
	@DeleteMapping("shaot/worker/{workerId}")
	public WorkerView deleteWorkerFromDataBase(@PathVariable long workerId) {
		return service.deleteWorkerFromDataBase(workerId);
	}
	
	////////////////////////////////
	////////////Worker//////////////
	////////////////////////////////
	
	
	@PostMapping("/shaot/worker")
	public WorkerView addWorker(@RequestBody WorkerDto workerDto) {
		return service.addWorker(workerDto);
	}
	
	@PutMapping("/shaot/worker/{id}")
	public WorkerView updateWorker(@PathVariable long id, @RequestBody WorkerUpdateDto workerUpdate) {
		return service.updateWorker(workerUpdate, id);
	}
	
	@GetMapping("/shaot/worker/{id}")
	public WorkerView findWorkerById(@PathVariable long id) {
		return service.findWorker(id);
	}
	
	@PutMapping("/shaot/worker/{workerId}/company/{companyId}")
	public WorkerView addCompanyToWorker(@PathVariable long workerId, @PathVariable long companyId) {
		return service.addCompanyToWorker(workerId, companyId);
	}
	
	@PutMapping("/shaot/worker/{workerId}/prefers/{companyId}")
	public void sendPreferedSchedule(@PathVariable long workerId, @PathVariable long companyId, @RequestBody List<WorkerPreferShiftsDto> prefers) {
		service.sendPrefers(prefers, workerId, companyId);
	}
	
	@GetMapping("/shaot/worker/{workerId}/company/{companyId}/schedule")
	public List<WorkerShiftView> getWeeklySchedule(@PathVariable long workerId, @PathVariable long companyId) {
		return service.getWeeklySchedule(companyId, workerId);
	}
	
	@GetMapping("shaot/worker/{workerId}/messages")
	public List<WorkerMessage> getWorkerMessages(@PathVariable long workerId) {
		return service.getWorkerMessages(workerId);
	}
	
	@GetMapping("shaot/worker/{workerId}/message/{messageId}")
	public WorkerMessage getWorkerMessageById(@PathVariable long workerId, @PathVariable long messageId) {
		return service.getWorkerMessageById(workerId, messageId);
	}
	
	@PutMapping("shaot/worker/{workerId}/answer/{messageId}")
	public WorkerMessage answerMessage(@PathVariable long workerId, @PathVariable long messageId, @RequestBody MessageAnswerDto messageAnswerDto) {
		return service.answerMessage(workerId, messageId, messageAnswerDto);
	}
	
	@GetMapping("shaot/worker/{companyId}/schedule/posibilities")
	public List<WorkerPreferShiftsDto> getWeeklyPosibilities(@PathVariable Long companyId) {
		return service.getWorkerPosibilities(companyId);
	}
		
	////////////////////////////////
	////////////Company/////////////
	////////////////////////////////
	
//	LocalDate defaultWeekStart = LocalDate.now();
//	List<ScheduleConfigurationShiftTime> shiftTimes = buildDefaultShiftTimes();
//	List<String> workDays = new ArrayList<>(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"));
//	
//	private List<ScheduleConfigurationShiftTime> buildDefaultShiftTimes() {
//		List<ScheduleConfigurationShiftTime> shiftTimes = new ArrayList<>();
//		shiftTimes.add(new ScheduleConfigurationShiftTime(LocalTime.of(7, 0), LocalTime.of(15, 0), 1));
//		shiftTimes.add(new ScheduleConfigurationShiftTime(LocalTime.of(15, 0), LocalTime.of(23, 0), 1));
//		return shiftTimes;
//	}
//	
//	ScheduleConfigurationDto basicWeek = ScheduleConfigurationDto
//			.builder()
//			.weekStart(defaultWeekStart)
//			.weekEnd(defaultWeekStart.plusDays(7))
//			.shiftsTime(shiftTimes)
//			.build();
	
	@GetMapping("/shaot/company/{companyId}/week/names")
	public List<String> getWeekNames(@PathVariable long companyId) {
		return service.getWeekNames(companyId);
	}
	
	@PostMapping("/shaot/company/{companyId}/week/period")
	public List<DayView> getWeekByPeriod(@PathVariable long companyId, @RequestBody PeriodDto period) {
		return service.getWeekByPeriod(companyId, period.getPeriod());
	}
	
	@PostMapping("/shaot/company")
	public CompanyView addCompany(@RequestBody CompanyDto companyDto) {
		CompanyView companyView = service.addCompanyToRepository(companyDto);
//		service.generateEmptyWeek(Long.valueOf(companyView.getId()), basicWeek);
		return companyView;
	}

	@GetMapping("/shaot/company/{id}")
	public CompanyView findCompany(@PathVariable long id) {
		return service.findCompany(id);
	}
	
	@PutMapping("/shaot/company/{companyId}/worker/{workerId}")
	public CompanyView addWorkerToCompany(@PathVariable long companyId, @PathVariable long workerId) {
		return service.addWorkerToCompany(companyId, workerId);
	}
	
	@PutMapping("shaot/company/{id}")
	public CompanyView updateCompany(@PathVariable long id, @RequestBody CompanyUpdateDto companyUpdateDto) {
		return service.updateCompany(companyUpdateDto, id);
	}
	
	@DeleteMapping("shaot/company/{companyId}/worker/{workerId}")
	public CompanyView deleteWorkerFromCompany(@PathVariable long companyId, @PathVariable long workerId) {
		return service.removeWorkerFromCompany(companyId, workerId);
	}
	
	@GetMapping("shaot/company/{id}/schedule")
	public List<DayView>  generateSchedule(@PathVariable long id) {
		return service.generateSchedule(id);
	}
	
	@PutMapping("shaot/company/{id}/shifts")
	public Set<CompanyShiftDto> addShift(@PathVariable  long id, @RequestBody CompanyAddShiftDto companyAddShiftDto) {
		return service.addShift(id, companyAddShiftDto);
	}

	@DeleteMapping("shaot/company/{id}/shifts")
	public Set<CompanyShiftDto> removeShift(@PathVariable long id, @RequestBody CompanyRemoveShiftDto companyRemoveShiftDto) {
		return service.removeShift(id, companyRemoveShiftDto);
	}
	
	@DeleteMapping("shaot/company/{id}/days")
	public Set<CompanyShiftDto> removeWorkingDay(@PathVariable long id, @RequestBody CompanyRemoveWorkingDayDto companyRemoveWorkingDayDto) {
		return service.removeWorkingDay(id, companyRemoveWorkingDayDto);
	}
	
	@PutMapping("shaot/company/{id}/schedule/configure")
	public ScheduleConfigurationDto configurateSchedule(@PathVariable long id, @RequestBody List<ScheduleConfigurationDto> configuration) {
		return service.configurateSchedule(id, configuration);
	}
	
	@PutMapping("shaot/company/{id}/wage")
	public List<WorkerForCompanyView> setGeneralWage(@PathVariable long id, @RequestBody CompanyWageDto companyWageDto) {
		return service.setGeneralWage(id, companyWageDto);
	}
	
	@PutMapping("shaot/company/{companyId}/wage/worker/{workerId}")
	public WorkerForCompanyView setIndividualWage(@PathVariable long companyId, @PathVariable long workerId, @RequestBody CompanyWageDto companyWageDto) {
		return service.setIndividualWage(companyId, workerId, companyWageDto);
	}
	
	@PostMapping("shaot/company/{companyId}/worker/{workerId}/message")
	public WorkerMessage sendMessage(@PathVariable long companyId, @PathVariable long workerId, @RequestBody SendMessageDto message) {
		return service.sendMessage(companyId, workerId, message);
	}
	
	@GetMapping("shaot/company/{companyId}/messages")
	public List<CompanyMessage> getCompanyMessages(@PathVariable long companyId) {
		return service.getAllCompanyMessages(companyId);
	}
	
	@GetMapping("shaot/company/{companyId}/message/{messageId}")
	public CompanyMessage getCompanyMessageById(@PathVariable long companyId, @PathVariable long messageId) {
		return service.getCompanyMessageById(companyId, messageId);
	}
	
	@GetMapping("shaot/company/{companyId}/worker/{workerId}/messages")
	public List<CompanyMessage> getAllCompanyMessagesByWorkerId(@PathVariable long companyId, @PathVariable long workerId) {
		return service.getCompanyMessageByWorkerId(companyId, workerId);
	}
	
	@GetMapping("shaot/company/{companyId}/configuration")
	public ScheduleConfigurationDto getCompanyConfiguration(@PathVariable long companyId) {
		return service.getCompanyConfiguration(companyId);
	}
	
	@PostMapping("shaot/company/{companyId}/schedule/save")
	public List<DayView> ScheduleSave(@PathVariable long companyId) {
		return service.saveSchedule(companyId);
	}
	
	@PutMapping("shaot/company/{companyId}/schedule/update")
	public List<DayView> updateSchedule(@PathVariable long companyId, @RequestBody List<DayView> upSchedule) {
		return service.updateSchedule(companyId, upSchedule);
	}	
}
