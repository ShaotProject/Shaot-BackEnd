package com.shaot.model;

import java.util.ArrayList;
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
@EqualsAndHashCode(of="id")
public class Company {
	private long id;
	@Setter
	private String name;
	@Setter
	private String password;
	private Set<WorkerForCompanyDto> workers = new HashSet<>();
	private Map<Long, WorkerForCompanyDto> workersMap = new ConcurrentHashMap<>();
	private Set<CompanyShiftDto> shifts = new HashSet<>();
	private ScheduleGeneratorImpl generator = new ScheduleGeneratorImpl();
	
	public Company(long id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
	
	public void addWorkerPrefers(Long workerId, List<WorkerPreferShiftsDto> workerPreferShiftsDtoList) {
		WorkerForCompanyDto worker = workersMap.get(workerId);
		if(worker != null) {
			workerPreferShiftsDtoList.forEach(prefer -> generator.addPrefer(worker.getId(), worker.getName(), prefer));
		}
	}
	
	public List<WorkerPreferShiftsDto> getWorkerPrefers(Long workerId) {
		return generator.getWorkerPrefers(workerId);
	}
	
	public void removeWorker(WorkerForCompanyDto worker) {
		workersMap.remove(worker.getId());
	}
	
	public void addWorker(WorkerForCompanyDto worker) {
		workersMap.put(worker.getId(), worker);
		workers.add(worker);
	}
	
	public WorkerForCompanyDto getWorker(Long id) {
		return workersMap.get(id);
	}
	
	public void addShift(CompanyShiftDto shift) {
		shifts.add(shift);
	}
}