package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ShiftView implements Comparable<ShiftView>{
	private String shiftName;
	private LocalDate dayName;
	private LocalTime shiftStart;
	private LocalTime shiftEnds;
	private Set<String> workerNames;
	
	public ShiftView(LocalDate dayName, LocalTime shiftStart, LocalTime shiftEnds) {
		LocalDateTime date = shiftStart.atDate(dayName);
		this.shiftName = date.format(DateTimeFormatter.ISO_DATE_TIME);
		this.shiftStart = shiftStart;
		this.shiftEnds = shiftEnds;
		this.dayName = dayName;
		this.workerNames = new HashSet<>();
	}
	
	public void addWorkerName(String workerName) {
		workerNames.add(workerName);
	}

	@Override
	public int compareTo(ShiftView o) {
		return shiftName.compareTo(o.getShiftName());
	}
}
