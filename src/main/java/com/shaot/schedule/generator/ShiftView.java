package com.shaot.schedule.generator;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;

@Getter
public class ShiftView {
	private String shiftName;
	private Set<String> workerNames;
	
	public ShiftView(String shiftName) {
		this.shiftName = shiftName;
		this.workerNames = new HashSet<>();
	}
	
	public void addWorkerName(String workerName) {
		workerNames.add(workerName);
	}
}
