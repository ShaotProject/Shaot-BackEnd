package com.shaot.controller;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyDto;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWageDto;
import com.shaot.dto.company.CompanyWeekGeneratorDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.company.ScheduleConfigurationShiftTime;
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
import com.shaot.exceptions.WorkerNotFoundException;
import com.shaot.schedule.generator.ShiftView;
import com.shaot.service.ShaotService;

@RestController
@CrossOrigin
public class ShaotController {
	
	@Autowired
	ShaotService service;
	
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
	public List<WorkerPreferShiftsDto> sendPreferedSchedule(@PathVariable long workerId, @PathVariable long companyId, @RequestBody List<WorkerPreferShiftsDto> prefers) {
		return service.sendPrefers(prefers, workerId, companyId);
	}
	
	@GetMapping("/shaot/worker/{workerId}/company/{companyId}/schedule")
	public List<WorkerShiftView> getWeeklySchedule(@PathVariable long workerId, @PathVariable long companyId) {
		return service.getWeeklySchedule(companyId, workerId);
	}
	
		
	////////////////////////////////
	////////////Company/////////////
	////////////////////////////////
	
	LocalDate defaultWeekStart = LocalDate.now();
	List<ScheduleConfigurationShiftTime> shiftTimes = buildDefaultShiftTimes();
	
	private List<ScheduleConfigurationShiftTime> buildDefaultShiftTimes() {
		List<ScheduleConfigurationShiftTime> shiftTimes = new ArrayList<>();
		shiftTimes.add(new ScheduleConfigurationShiftTime(LocalTime.of(7, 0), LocalTime.of(15, 0)));
		shiftTimes.add(new ScheduleConfigurationShiftTime(LocalTime.of(15, 0), LocalTime.of(23, 0)));
		return shiftTimes;
	}
	
	ScheduleConfigurationDto basicWeek = ScheduleConfigurationDto
			.builder()
			.weekStart(defaultWeekStart)
			.weekEnd(defaultWeekStart.plusDays(7))
			.workersNumberPerShift(1)
			.shiftsTime(shiftTimes)
			.build();
	
	@PostMapping("/shaot/company")
	public CompanyView addCompany(@RequestBody CompanyDto companyDto) {
		CompanyView companyView = service.addCompanyToRepository(companyDto);
		service.generateEmptyWeek(Long.valueOf(companyView.getId()), basicWeek);
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
	public Set<ShiftView>  generateSchedule(@PathVariable long id) {
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
	public Set<ShiftView> configurateSchedule(@PathVariable long id, @RequestBody ScheduleConfigurationDto configuration) {
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
