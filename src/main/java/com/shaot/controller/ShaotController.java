package com.shaot.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.shaot.service.ShaotService;

@RestController
@CrossOrigin
public class ShaotController {
	
	@Autowired
	ShaotService service;
	
	
	////////////////////////////////
	////////////Admin///////////////
	////////////////////////////////
	
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
	
	CompanyWeekGeneratorDto basicWeek = CompanyWeekGeneratorDto
			.builder()
			.dayNames(new ArrayList<String>(Arrays.asList("sunday", "monday", "tuesday", "wednesday", "thursday", "friday", "saturday")))
			.shiftNames(new ArrayList<String>(Arrays.asList("morning", "afternoon")))
			.workersPerShift(2)
			.build();
	
	@PostMapping("/shaot/company")
	public CompanyView addCompany(@RequestBody CompanyDto companyDto) {
		CompanyView companyView = service.addCompanyToRepository(companyDto);
		service.generateEmptyWeek(Integer.valueOf(companyView.getId()), basicWeek);
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
	public Map<String, List<ShiftView>>  generateSchedule(@PathVariable long id) {
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
	public Map<String, List<GeneratorShift>> configurateSchedule(@PathVariable long id, @RequestBody ScheduleConfigurationDto configuration) {
		return service.configurateSchedule(id, configuration);
	}
	
	@PutMapping("shaot/company/{id}/wage")
	public Set<WorkerForCompanyView> setGeneralWage(@PathVariable long id, @RequestBody CompanyWageDto companyWageDto) {
		return service.setGeneralWage(id, companyWageDto);
	}
	
	@PutMapping("shaot/company/{companyId}/wage/worker/{workerId}")
	public WorkerForCompanyView setIndividualWage(@PathVariable long companyId, @PathVariable long workerId, @RequestBody CompanyWageDto companyWageDto) {
		return service.setIndividualWage(companyId, workerId, companyWageDto);
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
