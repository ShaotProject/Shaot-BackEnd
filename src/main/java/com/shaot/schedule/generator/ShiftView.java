package com.shaot.schedule.generator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShiftView {
	private LocalDateTime shiftName;
	private LocalTime shiftStart;
	private LocalTime shiftEnd;
	private Set<String> workerNames;
	
	public void addWorkerName(String workerName) {
		workerNames.add(workerName);
	}
}
