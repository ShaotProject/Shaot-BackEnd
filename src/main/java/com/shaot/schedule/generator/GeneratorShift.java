package com.shaot.schedule.generator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import com.shaot.dto.schedule.GeneratorShiftDto;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of="shiftName")
public class GeneratorShift implements Comparable<GeneratorShift>{
	private String shiftName;
	private String dayName;
	private List<String> workersOnShift;
	private List<GeneratorWorker> available;
	
	public GeneratorShift(String shiftName, String dayName) {
		this.shiftName = shiftName;
		this.dayName = dayName;
		this.available = new ArrayList<>();
		this.workersOnShift = new ArrayList<>();
	}
	
	public GeneratorWorker getCandidate() {
		if(available.size() > 0) {
			Collections.sort(available);
			return available.get(0);
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
