package com.shaot.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.shaot.dto.company.CompanyForWorkerDto;
import com.shaot.dto.worker.WorkerScheduleDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of="mail")
public class Worker {
	private long id;
	@Setter
	private String name;
	@Setter
	private String mail;
	@Setter
	private String password;
	@Setter
	private double wage;
	@Setter
	private boolean individualWage;
	private CompanyForWorkerDto company;
	private List<WorkerScheduleDto> shifts = new ArrayList<>();
	private Map<Long, WorkerMessage> messages = new ConcurrentHashMap<>();
	
	public Worker(long id, String name, String mail, String password) {
		this.id = id;
		this.name = name;
		this.mail = mail;
		this.password = password;
	}
	
	public void addCompany(CompanyForWorkerDto company) {
		this.company = company;
		
	}
	
	public void removeCompany(CompanyForWorkerDto company) {
		if(this.company.equals(company)) {
			this.company = null;
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
	
	public void addMessage(Long messageId, Long senderId, String text, LocalDateTime shiftName) {
		messages.put(messageId, new WorkerMessage(text, senderId, shiftName));
	}
	
	public WorkerMessage answerMessage(Long messageId, boolean answer, String reason) {
		WorkerMessage message = messages.get(messageId);
		message.setAnswer(answer);
		message.setReason(reason);
		return message;
	}
	
	public CompanyForWorkerDto updateCompany(long companyId, String newName) {
		this.company.setName(newName);
		return company;
	}
}
