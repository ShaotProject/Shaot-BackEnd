package com.shaot.schedule.generator;

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
	private LocalDate dayName;
	private List<String> workersOnShift;
	private LocalTime shiftStart;
	private LocalTime shiftEnd;
	@Setter
	private int workerNeeded;
	@Setter
	private List<GeneratorWorker> available;
	
	public GeneratorShift(LocalDateTime shiftName, LocalDate dayName, LocalTime shiftStart, LocalTime shiftEnd) {
		this.shiftName = shiftName;
		this.dayName = dayName;
		this.shiftStart = shiftStart;
		this.shiftEnd = shiftEnd;
		this.available = new ArrayList<>();
		this.workersOnShift = new ArrayList<>();
	}
	
	public GeneratorWorker getCandidate() {
		if(available.size() > 0 && workerNeeded > 0) {
			Collections.sort(available);
			GeneratorWorker worker = available.get(0);
			return worker;
		}
		return null;
	}
	
	public boolean addAvailable(GeneratorWorker worker) {
		return available.add(worker);
	}
	
	public boolean removeAvailable(GeneratorWorker worker) {
		return available.remove(worker);
	}
	
	public GeneratorShiftDto convertToDto() {
		return new GeneratorShiftDto(shiftName, dayName, workersOnShift);
	}
	
	@Override
	public int compareTo(GeneratorShift o) {
		return Integer.valueOf(o.getAvailable().size()).compareTo(Integer.valueOf(available.size()));
	}
}
