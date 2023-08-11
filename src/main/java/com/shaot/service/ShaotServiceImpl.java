package com.shaot.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shaot.dto.company.CompanyAddShiftDto;
import com.shaot.dto.company.CompanyAddWorkingDay;
import com.shaot.dto.company.CompanyDto;
import com.shaot.dto.company.CompanyForWorkerDto;
import com.shaot.dto.company.CompanyRemoveShiftDto;
import com.shaot.dto.company.CompanyRemoveWorkingDayDto;
import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.company.CompanyUpdateDto;
import com.shaot.dto.company.CompanyView;
import com.shaot.dto.company.CompanyWageDto;
import com.shaot.dto.company.ScheduleConfigurationDto;
import com.shaot.dto.worker.WorkerDto;
import com.shaot.dto.worker.WorkerForCompanyDto;
import com.shaot.dto.worker.WorkerForCompanyView;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.dto.worker.WorkerShiftView;
import com.shaot.dto.worker.WorkerUpdateDto;
import com.shaot.dto.worker.WorkerView;
import com.shaot.exceptions.AccessToCompanyRestrictedException;
import com.shaot.exceptions.CompanyAlreadyExistsException;
import com.shaot.exceptions.CompanyNotFoundException;
import com.shaot.exceptions.WorkerNotFoundException;
import com.shaot.model.Company;
import com.shaot.model.Worker;
import com.shaot.repository.CompaniesRepository;
import com.shaot.repository.WorkersRepository;
import com.shaot.schedule.generator.ShiftView;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ShaotServiceImpl implements ShaotService {

	final WorkersRepository workersRepository;
	final CompaniesRepository companiesRepository;
	final ModelMapper modelMapper;
	
	@Override
	public List<WorkerView> getAllWorkers() {
		return workersRepository.findAll().stream().map(w -> modelMapper.map(w, WorkerView.class)).toList();
	}

	@Override
	public List<CompanyView> getAllCompanies() {
		return companiesRepository.findAll().stream().map(w -> modelMapper.map(w, CompanyView.class)).toList();
	}
	
	@Override
	@Transactional
	public WorkerView deleteWorkerFromDataBase(long workerId) {
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		workersRepository.delete(worker);
		worker.getCompanies().forEach(c -> {
			Company company = companiesRepository.findCompanyById(c.getId()).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
			company.removeWorker(worker.getId());
			companiesRepository.save(company);
		});
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public CompanyView deleteCompanyFromDataBase(long companyId) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		companiesRepository.delete(company);
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findById(w.getId()).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			worker.removeCompany(modelMapper.map(company, CompanyForWorkerDto.class));
			workersRepository.save(worker);
		});
		return modelMapper.map(company, CompanyView.class);
	}
	
	@Override
	@Transactional
	public WorkerView addWorker(WorkerDto workerDto) {
		if(!workersRepository.existsById(workerDto.getId())) {
			Worker worker = workersRepository.save(modelMapper.map(workerDto, Worker.class));
			return modelMapper.map(worker, WorkerView.class);
		}
		throw new WorkerNotFoundException(HttpStatus.NOT_FOUND);
	}

	@Override
	@Transactional
	public WorkerView updateWorker(WorkerUpdateDto workerUpdateDto, long id) {
		Worker worker = workersRepository.findWorkerById(id).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.setName(workerUpdateDto.getName());
		workersRepository.save(worker);
		worker.getCompanies().forEach(c -> {
			Company company = companiesRepository.findCompanyById(c.getId()).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
			company.updateWorker(worker.getId(), worker.getName());
			companiesRepository.save(company);
		});
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public WorkerView addCompanyToWorker(long workerId, long companyId) {
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		worker.addCompany(modelMapper.map(company, CompanyForWorkerDto.class));
		workersRepository.save(worker);
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	public WorkerView findWorker(long id) {
		Worker worker = workersRepository.findWorkerById(id).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public void sendPrefers(List<WorkerPreferShiftsDto> workerPreferShiftsDtoList, long workerId, long companyId) {
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		if(worker.getCompanies().contains(new CompanyForWorkerDto(companyId))) {
			Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
			company.addWorkerPrefers(worker.getId(), workerPreferShiftsDtoList);
			companiesRepository.save(company);
		} else {
			throw new AccessToCompanyRestrictedException(HttpStatus.FORBIDDEN);
		}
	}

	@Override
	public List<WorkerShiftView> getWeeklySchedule(long companyId, long workerId) {
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		if(worker.getCompanies().contains(new CompanyForWorkerDto(companyId))) {
			List<WorkerShiftView> workerSchedule = company.getGenerator().getWorkerSchedule(worker);
			return workerSchedule;
		} else {
			throw new AccessToCompanyRestrictedException(HttpStatus.FORBIDDEN);
		}
	}

	///////////////////////////////////////////////////////////////////////
	// -----------------------Company Service----------------------------//
	///////////////////////////////////////////////////////////////////////
	
	
	@Override
	@Transactional
	public CompanyView addCompanyToRepository(CompanyDto companyDto) {
		if(!companiesRepository.existsById(companyDto.getId())) {
			Company company = new Company(companyDto.getId(), companyDto.getName(), companyDto.getPassword(), companyDto.getGeneralWage());
			companiesRepository.save(company);
			return modelMapper.map(company, CompanyView.class);
		}
		throw new CompanyAlreadyExistsException(HttpStatus.FORBIDDEN);
	}
	
	@Override
	public CompanyView findCompany(long id) {
		Company company = companiesRepository.findCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return modelMapper.map(company, CompanyView.class);
	}
	
	@Override
	@Transactional
	public CompanyView addWorkerToCompany(long companyId, long workerId) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));	
		company.addWorker(new WorkerForCompanyDto(workerId, worker.getName()));
		if(!worker.getCompanies().contains(new CompanyForWorkerDto(company.getId()))) {
			worker.addCompany(modelMapper.map(company, CompanyForWorkerDto.class));
			worker.setWage(company.getGeneralWage());
		}
		workersRepository.save(worker);
		companiesRepository.save(company);
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	@Transactional
	public CompanyView updateCompany(CompanyUpdateDto companyUpdateDto, long id) {
		Company company = companiesRepository.findCompanyById(id).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.setName(companyUpdateDto.getName());
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findById(w.getId()).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			worker.updateCompany(company.getId(), company.getName());
			workersRepository.save(worker);
		});
		companiesRepository.save(company);
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	@Transactional
	public CompanyView removeWorkerFromCompany(long companyId, long workerId) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.removeCompany(modelMapper.map(company, CompanyForWorkerDto.class));
		workersRepository.save(worker);
		company.removeWorker(worker.getId());
		companiesRepository.save(company);
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	public Set<ShiftView> generateSchedule(long companyId) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().generateSchedule();
		companiesRepository.save(company);
		return company.getGenerator().getSchedule();
	}

	@Override
	public Set<CompanyShiftDto> addShift(long companyId, CompanyAddShiftDto companyShiftAddShiftDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.addShift(modelMapper.map(companyShiftAddShiftDto, CompanyShiftDto.class));
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> addWorkingDay(long companyId, CompanyAddWorkingDay companyAddWorkingDayDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().addWorkingDay(companyAddWorkingDayDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> removeShift(long companyId, CompanyRemoveShiftDto companyRemoveShiftDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().removeShift(companyRemoveShiftDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> removeWorkingDay(long companyId, CompanyRemoveWorkingDayDto removeWorkingDayDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().removeWorkingDay(removeWorkingDayDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<ShiftView> generateEmptyWeek(long companyId, ScheduleConfigurationDto companyWeekGeneratorDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().generateWeek(companyWeekGeneratorDto);
		companiesRepository.save(company);
		return company.getGenerator().getSchedule();
	}

	@Override
	@Transactional
	public Set<ShiftView> configurateSchedule(long companyId, ScheduleConfigurationDto configuration) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().generateWeek(configuration);
		companiesRepository.save(company);
		return company.getGenerator().getSchedule();
	}

	@Override
	@Transactional
	public WorkerForCompanyView setIndividualWage(long companyId, long workerId, CompanyWageDto companyWageDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.setWage(companyWageDto.getNewWage());
		worker.setIndividualWage(true);
		workersRepository.save(worker);
		company.setIndividualWage(worker.getId(), companyWageDto.getNewWage());
		companiesRepository.save(company);
		return modelMapper.map(worker, WorkerForCompanyView.class);
	}

	@Override
	@Transactional
	public List<WorkerForCompanyView> setGeneralWage(long companyId, CompanyWageDto companyWageDto) {
		Company company = companiesRepository.findCompanyById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.setGeneralWage(companyWageDto.getNewWage());
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findWorkerById(w.getId()).orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			if(!worker.isIndividualWage()) {
				worker.setWage(companyWageDto.getNewWage());
			}
			workersRepository.save(worker);
		});
		companiesRepository.save(company);
		return company.getWorkers().stream().map(w -> modelMapper.map(w, WorkerForCompanyView.class)).toList();
	}

	









}