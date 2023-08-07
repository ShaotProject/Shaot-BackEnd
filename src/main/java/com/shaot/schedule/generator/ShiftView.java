package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ShiftView {
	private LocalDateTime shiftName;
	private LocalDate dayName;
	private LocalTime shiftStart;
	private LocalTime shiftEnds;
	private Set<String> workerNames;
	
	public ShiftView(LocalDate dayName, LocalTime shiftStart, LocalTime shiftEnds) {
		this.shiftName = shiftStart.atDate(dayName);
		this.shiftStart = shiftStart;
		this.shiftEnds = shiftEnds;
		this.dayName = dayName;
		this.workerNames = new HashSet<>();
	}
	
	public void addWorkerName(String workerName) {
		workerNames.add(workerName);
	}
}
