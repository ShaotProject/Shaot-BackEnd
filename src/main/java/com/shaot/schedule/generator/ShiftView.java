package com.shaot.schedule.generator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of="shiftName")
public class ShiftView implements Comparable<ShiftView>{
	private LocalDateTime shiftName;
	private String dayName;
	private LocalTime shiftStart;
	private LocalTime shiftEnds;
	private Set<String> workerNames;
	
	public ShiftView(LocalDateTime shiftName, String dayName, LocalTime shiftStart, LocalTime shiftEnds) {
		this.shiftName = shiftName;
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
