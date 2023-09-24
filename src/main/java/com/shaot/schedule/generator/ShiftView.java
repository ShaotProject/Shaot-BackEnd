package com.shaot.schedule.generator;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShiftView {
	private LocalDateTime shiftName;
	private LocalTime shiftStart;
	private LocalTime shiftEnd;
	private Set<String> workerNames;
	private int workersNumber;
	
	public void addWorkerName(String workerName) {
		workerNames.add(workerName);
	}
}
