package com.shaot.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mindrot.jbcrypt.BCrypt;
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
import com.shaot.exceptions.CompanyAlreadyExistsException;
import com.shaot.exceptions.CompanyNotFoundException;
import com.shaot.exceptions.WorkerNotFoundException;
import com.shaot.model.Company;
import com.shaot.model.CompanyMessage;
import com.shaot.model.Worker;
import com.shaot.model.WorkerMessage;
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
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		workersRepository.delete(worker);
		long companyId = worker.getCompany().getId();
		Company company = companiesRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.removeWorker(workerId);
		companiesRepository.save(company);
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public CompanyView deleteCompanyFromDataBase(long companyId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		companiesRepository.delete(company);
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findById(w.getId())
					.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			worker.removeCompany(modelMapper.map(company, CompanyForWorkerDto.class));
			workersRepository.save(worker);
		});
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	@Transactional
	public WorkerView addWorker(WorkerDto workerDto) {
		if (!workersRepository.existsById(workerDto.getId())) {
			Worker worker = modelMapper.map(workerDto, Worker.class);
			String password = BCrypt.hashpw(workerDto.getPassword(), BCrypt.gensalt());
			worker.setPassword(password);
			workersRepository.save(worker);
			return modelMapper.map(worker, WorkerView.class);
		}
		throw new WorkerNotFoundException(HttpStatus.NOT_FOUND); //worker is already exists exception !!!
	}

	@Override
	@Transactional
	public WorkerView updateWorker(WorkerUpdateDto workerUpdateDto, long id) {
		Worker worker = workersRepository.findWorkerById(id)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.setName(workerUpdateDto.getName());
		workersRepository.save(worker);
		long companyId = worker.getCompany().getId();
		Company company = companiesRepository.findById(companyId).orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.updateWorker(id, workerUpdateDto.getName());
		companiesRepository.save(company);
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public WorkerView addCompanyToWorker(long workerId, long companyId) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		worker.addCompany(modelMapper.map(company, CompanyForWorkerDto.class));
		workersRepository.save(worker);
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	public WorkerView findWorker(long id) {
		Worker worker = workersRepository.findWorkerById(id)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		return modelMapper.map(worker, WorkerView.class);
	}

	@Override
	@Transactional
	public void sendPrefers(List<WorkerPreferShiftsDto> workerPreferShiftsDtoList, long workerId, long companyId) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		if (worker.getCompany().equals(new CompanyForWorkerDto(companyId))) {
			Company company = companiesRepository.findCompanyById(companyId)
					.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
			company.addWorkerPrefers(worker.getId(), workerPreferShiftsDtoList);
			companiesRepository.save(company);
		} else {
			throw new AccessToCompanyRestrictedException(HttpStatus.FORBIDDEN);
		}
	}

	@Override
	public List<WorkerShiftView> getWeeklySchedule(long companyId, long workerId) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		if (worker.getCompany().equals(new CompanyForWorkerDto(companyId))) {
			List<WorkerShiftView> workerSchedule = company.getGenerator().getWorkerSchedule(worker);
			return workerSchedule;
		} else {
			throw new AccessToCompanyRestrictedException(HttpStatus.FORBIDDEN);
		}
	}

	@Override
	public List<WorkerMessage> getWorkerMessages(Long workerId) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		return worker.getMessages().values().stream().toList();
	}

	@Override
	public WorkerMessage getWorkerMessageById(Long workerId, Long messageId) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		return worker.getMessageById(messageId);
	}

	@Override
	@Transactional
	public WorkerMessage answerMessage(Long workerId, Long messageId, MessageAnswerDto messageAnswerDto) {
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.answerMessage(messageId, messageAnswerDto.isAnswer(), messageAnswerDto.getReason());
		WorkerMessage message = worker.getMessageById(messageId);
		Long companyId = message.getSenderId();
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.addMessage(workerId, messageId, messageAnswerDto.getReason(), messageAnswerDto.isAnswer());
		if(messageAnswerDto.isAnswer()) {
			company.setWorkerOnShiftManual(workerId, worker.getName(), message.getShiftName());
		}
		companiesRepository.save(company);
		workersRepository.save(worker);
		return worker.getMessageById(messageId);
	}

	///////////////////////////////////////////////////////////////////////
	// -----------------------Company Service----------------------------//
	///////////////////////////////////////////////////////////////////////

	@Override
	@Transactional
	public CompanyView addCompanyToRepository(CompanyDto companyDto) {
		if (!companiesRepository.existsById(companyDto.getId())) {
			Company company = modelMapper.map(companyDto, Company.class);
			String password = BCrypt.hashpw(companyDto.getPassword(), BCrypt.gensalt());
			company.setPassword(password);
			companiesRepository.save(company);
			return modelMapper.map(company, CompanyView.class);
		}
		throw new CompanyAlreadyExistsException(HttpStatus.FORBIDDEN);
	}

	@Override
	public CompanyView findCompany(long id) {
		Company company = companiesRepository.findCompanyById(id)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	@Transactional
	public CompanyView addWorkerToCompany(long companyId, long workerId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		company.addWorker(new WorkerForCompanyDto(workerId, worker.getName()));
		if (worker.getCompany() == null) {
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
		Company company = companiesRepository.findCompanyById(id)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.setName(companyUpdateDto.getName());
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findById(w.getId())
					.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			worker.updateCompany(company.getId(), company.getName());
			workersRepository.save(worker);
		});
		companiesRepository.save(company);
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	@Transactional
	public CompanyView removeWorkerFromCompany(long companyId, long workerId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		worker.removeCompany(modelMapper.map(company, CompanyForWorkerDto.class));
		workersRepository.save(worker);
		company.removeWorker(worker.getId());
		companiesRepository.save(company);
		return modelMapper.map(company, CompanyView.class);
	}

	@Override
	public Set<ShiftView> generateSchedule(long companyId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return company.getGenerator().generateSchedule();
	}

	@Override
	public Set<CompanyShiftDto> addShift(long companyId, CompanyAddShiftDto companyShiftAddShiftDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.addShift(modelMapper.map(companyShiftAddShiftDto, CompanyShiftDto.class));
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> addWorkingDay(long companyId, CompanyAddWorkingDay companyAddWorkingDayDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().addWorkingDay(companyAddWorkingDayDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> removeShift(long companyId, CompanyRemoveShiftDto companyRemoveShiftDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().removeShift(companyRemoveShiftDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<CompanyShiftDto> removeWorkingDay(long companyId, CompanyRemoveWorkingDayDto removeWorkingDayDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().removeWorkingDay(removeWorkingDayDto);
		companiesRepository.save(company);
		return company.getShifts();
	}

	@Override
	public Set<ShiftView> generateEmptyWeek(long companyId, ScheduleConfigurationDto companyWeekGeneratorDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().generateWeek(companyWeekGeneratorDto);
		companiesRepository.save(company);
		return company.getGenerator().getSchedule();
	}

	@Override
	@Transactional
	public Set<ShiftView> configurateSchedule(long companyId, ScheduleConfigurationDto configuration) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.getGenerator().generateWeek(configuration);
		companiesRepository.save(company);
		return company.getGenerator().getSchedule();
	}

	@Override
	@Transactional
	public WorkerForCompanyView setIndividualWage(long companyId, long workerId, CompanyWageDto companyWageDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
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
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		company.setGeneralWage(companyWageDto.getNewWage());
		company.getWorkers().forEach(w -> {
			Worker worker = workersRepository.findWorkerById(w.getId())
					.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
			if (!worker.isIndividualWage()) {
				worker.setWage(companyWageDto.getNewWage());
			}
			workersRepository.save(worker);
		});
		companiesRepository.save(company);
		return company.getWorkers().stream().map(w -> modelMapper.map(w, WorkerForCompanyView.class)).toList();
	}

	@Override
	@Transactional
	public WorkerMessage sendMessage(Long companyId, Long workerId, SendMessageDto sendMessageDto) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		Worker worker = workersRepository.findWorkerById(workerId)
				.orElseThrow(() -> new WorkerNotFoundException(HttpStatus.NOT_FOUND));
		LocalDateTime shiftName = sendMessageDto.getShiftNeed();
		DateTimeFormatter shiftFormat = DateTimeFormatter.ofPattern("dd.MM HH:mm");
		String messageText = "Need worker on shift " + shiftName.format(shiftFormat);
		worker.addMessage(sendMessageDto.getMessageId(), company.getId(), messageText, shiftName);
		workersRepository.save(worker);
		return worker.getMessageById(sendMessageDto.getMessageId());
	}

	@Override
	public List<CompanyMessage> getAllCompanyMessages(Long companyId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return company.getMessages().values().stream().toList();
	}

	@Override
	public CompanyMessage getCompanyMessageById(Long companyId, Long messageId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return company.getMessageById(messageId);
	}

	@Override
	public List<CompanyMessage> getCompanyMessageByWorkerId(Long companyId, Long workerId) {
		Company company = companiesRepository.findCompanyById(companyId)
				.orElseThrow(() -> new CompanyNotFoundException(HttpStatus.NOT_FOUND));
		return company.getAllMessagesByWorkerId(workerId);
	}

}