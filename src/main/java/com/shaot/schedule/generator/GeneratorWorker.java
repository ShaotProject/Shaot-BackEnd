package com.shaot.schedule.generator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of="name")
public class GeneratorWorker implements Comparable<GeneratorWorker> {
	@Setter
	private String name;
	@Setter
	private Long id;
	private Map<String, List<String>> weekPrefers = new ConcurrentHashMap<>();
	private Integer priorityByShiftsNumber = 0;
	private Integer workingShiftsCounter = 0;
	
	public void addPrefers(String dayName, List<String> shiftsNames) {
		weekPrefers.put(dayName, shiftsNames);
		priorityByShiftsNumber += shiftsNames.size();
	}
	
	public void addToSchedule() {
		++workingShiftsCounter;
	}

	@Override
	public int compareTo(GeneratorWorker o) {
		int workingShiftsComparator = workingShiftsCounter.compareTo(o.getWorkingShiftsCounter());
		if(workingShiftsComparator == 0) {
			return priorityByShiftsNumber.compareTo(o.getPriorityByShiftsNumber());
		}
		return workingShiftsComparator;
	}
}
