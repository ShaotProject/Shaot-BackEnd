package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.shaot.dto.schedule.GeneratorShiftDto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@EqualsAndHashCode(of="shiftName")
public class GeneratorShift implements Comparable<GeneratorShift>{
	private LocalDateTime shiftName;
	private LocalDateTime shiftStart;
	private LocalDateTime shiftEnd;
	private LocalDate dayName;
	private List<String> workersOnShift;
	@Setter
	private int workerNeeded;
	private Set<GeneratorWorker> available;
	
	public GeneratorShift(LocalDateTime shiftName, LocalDate dayName, LocalDateTime shiftStart, LocalDateTime shiftEnd) {
		this.shiftName = shiftName;
		this.dayName = dayName;
		this.available = new HashSet<>();
		this.workersOnShift = new ArrayList<>();
	}
	
	public GeneratorWorker getCandidate() {
		if(available.size() > 0 && workerNeeded > 0) {
			List<GeneratorWorker> workersAvailableList = new ArrayList<>(available);
			Collections.sort(workersAvailableList);
			GeneratorWorker worker = workersAvailableList.get(0);
			available.remove(worker);
			if(!worker.getRestrict().contains(shiftName)) {
				return worker;
			}
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
