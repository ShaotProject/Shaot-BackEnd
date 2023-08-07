package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
	private Map<LocalDate, List<LocalDateTime>> weekPrefers = new ConcurrentHashMap<>();
	private List<LocalDateTime> restrict = new ArrayList<>();
	private Integer priorityByShiftsNumber = 0;
	private Integer workingShiftsCounter = 0;
	@Setter
	private Long hoursPerShift;
	
	public void addPrefers(LocalDate dayName, List<LocalDateTime> shiftsNames) {
		weekPrefers.put(dayName, shiftsNames);
		priorityByShiftsNumber += shiftsNames.size();
	}
	
	public void addToSchedule(LocalDateTime shift) {
		restrict.add(shift.plusHours(hoursPerShift));
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
