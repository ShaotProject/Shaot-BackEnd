package com.shaot.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.shaot.dto.company.CompanyForWorkerDto;
import com.shaot.dto.worker.WorkerScheduleDto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Worker {
	private long id;
	@Setter
	private String name;
	@Setter
	private String password;
	@Setter
	private double wage;
	@Setter
	private boolean individualWage;
	private List<CompanyForWorkerDto> companies = new ArrayList<>();
	private List<WorkerScheduleDto> shifts = new ArrayList<>();
	private Map<Long, WorkerMessage> messages = new ConcurrentHashMap<>();
	
	public Worker(long id, String name, String password) {
		this.id = id;
		this.name = name;
		this.password = password;
	}
	
	public boolean addCompany(CompanyForWorkerDto company) {
		if(!companies.contains(company)) {
			return companies.add(company);
		}
		return false;
	}
	
	public void removeCompany(CompanyForWorkerDto company) {
		if(companies.contains(company)) {
			companies.remove(company);
		}
	}
	
	public boolean addShift(WorkerScheduleDto shift) {
		if(!shifts.contains(shift)) {
			return shifts.add(shift);
		}
		return false;
	}
	
	public WorkerMessage getMessageById(Long messageId) {
		return messages.get(messageId);
	}
	
	public void addMessage(Long messageId, Long senderId, String text) {
		messages.put(messageId, new WorkerMessage(text, senderId));
	}
	
	public WorkerMessage answerMessage(Long messageId, boolean answer, String reason) {
		WorkerMessage message = messages.get(messageId);
		message.setAnswer(answer);
		message.setReason(reason);
		return message;
	}
	
	public CompanyForWorkerDto updateCompany(long companyId, String newName) {
		List<CompanyForWorkerDto> companyList = companies.stream().filter(c -> c.getId() == companyId).toList();
		CompanyForWorkerDto company = companyList.get(0);
		removeCompany(company);
		company.setName(newName);
		addCompany(company);
		return company;
	}
}
