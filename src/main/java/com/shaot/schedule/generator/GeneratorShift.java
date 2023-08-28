package com.shaot.schedule.generator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.shaot.dto.schedule.GeneratorShiftDto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GeneratorShift implements Comparable<GeneratorShift>{
	private LocalDateTime shiftName;
	private String dayName;
	private List<String> workersOnShift;
	private LocalTime shiftStart;
	private LocalTime shiftEnd;
	@Setter
	private int workerNeeded;
	@Setter
	private List<GeneratorWorker> available;
	private int hoursPerShift;
	
	public GeneratorShift(LocalDateTime shiftName, String dayName, LocalTime shiftStart, LocalTime shiftEnd) {
		this.shiftName = shiftName;
		this.dayName = dayName;
		this.shiftStart = shiftStart;
		this.shiftEnd = shiftEnd;
		this.available = new ArrayList<>();
		this.workersOnShift = new ArrayList<>();
		this.hoursPerShift = countHoursPerShift();
	}
	
	public int countHoursPerShift() {
		int res = 0;
		LocalTime temp = shiftStart;
		if(temp.getHour() == LocalTime.MAX.getHour()) {
			++res;
			temp = LocalTime.MIN;
		}
		while(temp.isBefore(shiftEnd)) {
			temp = temp.plusHours(1);
			++res;
		}
		return res;
	}

	public boolean updateAvailable(GeneratorWorker worker, LocalTime shiftEnd, LocalDateTime restrict) {
		if (available.contains(worker)) {
			for (int i = 0; i < available.size(); i++) {
				if (available.get(i).equals(worker)) {
					available.get(i).addToSchedule(restrict, shiftEnd, hoursPerShift);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean addAvailable(GeneratorWorker worker) {
		return available.add(worker);
	}
	
	public boolean removeAvailable() {
		GeneratorWorker worker = Collections.min(available);
		return available.remove(worker);
	}
	
	public GeneratorShiftDto convertToDto() {
		return new GeneratorShiftDto(shiftName, dayName, workersOnShift);
	}
	
	@Override
	public int compareTo(GeneratorShift o) {
		Integer res = Integer.valueOf(o.getAvailable().size()).compareTo(Integer.valueOf(available.size()));
		return res == 0 ? Integer.valueOf(o.getShiftName().compareTo(shiftName)) : res;
	}
}
