package com.shaot.schedule.generator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of="dayDate")
@AllArgsConstructor
public class DayView implements Comparable<DayView>{
	private LocalDate dayDate;
	private String dayName;
	private List<ShiftView> shifts;
	
	public ShiftView containsWorker(String workerName) {
		for(int i = 0; i < shifts.size(); i++) {
			if(shifts.get(i).getWorkerNames().contains(workerName)) {
				return shifts.get(i);
			}
		}
		return null;
	}
	
	public boolean addWorkerManualy(String workerName, LocalDateTime shiftName) {
		for(int i =0; i < shifts.size(); i++) {
			if(shifts.get(i).getShiftName().equals(shiftName)) {
				shifts.get(i).addWorkerName(workerName);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int compareTo(DayView o) {
		return dayDate.compareTo(o.getDayDate());
	}
}
