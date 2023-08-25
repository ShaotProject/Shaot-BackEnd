package com.shaot.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.shaot.dto.company.CompanyShiftDto;
import com.shaot.dto.worker.WorkerForCompanyDto;
import com.shaot.dto.worker.WorkerPreferShiftsDto;
import com.shaot.schedule.generator.ScheduleGeneratorImpl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of="mail")
public class Company {
	private long id;
	@Setter
	private String name;
	@Setter
	private String mail;
	@Setter
	private String password;
	private double generalWage;
	private Set<WorkerForCompanyDto> workers = new HashSet<>();
	private Map<Long, WorkerForCompanyDto> workersMap = new ConcurrentHashMap<>();
	private Set<CompanyShiftDto> shifts = new HashSet<>();
	private Map<Long, CompanyMessage> messages = new ConcurrentHashMap<>();
	private ScheduleGeneratorImpl generator = new ScheduleGeneratorImpl();
	
	public Company(long id, String name, String mail, String password, double generalWage) {
		this.id = id;
		this.name = name;
		this.mail = mail;
		this.password = password;
		this.generalWage = generalWage;
	}
	
	public void addWorkerPrefers(Long workerId, List<WorkerPreferShiftsDto> workerPreferShiftsDtoList) {
		WorkerForCompanyDto worker = workersMap.get(workerId);
		if(worker != null) {
			int priority = 0;
			for(int i = 0; i < workerPreferShiftsDtoList.size(); i++) {
				priority += workerPreferShiftsDtoList.get(i).getShifts().size();
			}
			for(int i = 0; i < workerPreferShiftsDtoList.size(); i++) {
				generator.addPrefer(workerId, worker.getName(), workerPreferShiftsDtoList.get(i), priority);
			}
		}
	}
	
	public void removeWorker(long workerId) {
		WorkerForCompanyDto worker = workersMap.remove(workerId);
		workers.remove(worker);
	}
	
	public void addWorker(WorkerForCompanyDto worker) {
		worker.setWage(generalWage);
		workersMap.put(worker.getId(), worker);
		workers.add(worker);
	}
	
	public WorkerForCompanyDto getWorker(Long id) {
		return workersMap.get(id);
	}
	
	public void addShift(CompanyShiftDto shift) {
		shifts.add(shift);
	}
	
	public void setIndividualWage(long workerId, double newWage) {
		WorkerForCompanyDto worker = workersMap.get(workerId);
		workers.remove(worker);
		worker.setWage(newWage);
		worker.setIndividualWage(true);
		workers.add(worker);
		workersMap.put(workerId, worker);
	}
	
	public WorkerForCompanyDto updateWorker(long workerId, String newName) {
		WorkerForCompanyDto worker = workersMap.get(workerId);
		worker.setName(newName);
		removeWorker(worker.getId());
		addWorker(worker);
		return worker;
	}
	
	public void setGeneralWage(double newWage) {
		workersMap.entrySet().stream().forEach(entry -> {
			WorkerForCompanyDto worker = entry.getValue();
			if(!worker.isIndividualWage()) {
				worker.setWage(newWage);
			}
		});
		workers.forEach(w -> {
			if(!w.isIndividualWage()) {
				w.setWage(newWage);
			}
		});	
	}
	
	public void setWorkerOnShiftManual(Long workerId, String workerName, LocalDateTime shiftName) {
		generator.setWorkerOnShiftManual(workerId, workerName, shiftName);
	}
	
	public CompanyMessage addMessage(Long workerId, Long messageId, String reason, boolean answer) {
		return messages.put(messageId, new CompanyMessage(workerId, reason, answer));
	}
	
	public CompanyMessage getMessageById(Long messageId) {
		return messages.get(messageId);
	}
	
	public List<CompanyMessage> getAllMessagesByWorkerId(Long workerId) {
		return messages.values().stream().filter(m -> m.getWorkerId().equals(workerId)).toList();
	}	
}