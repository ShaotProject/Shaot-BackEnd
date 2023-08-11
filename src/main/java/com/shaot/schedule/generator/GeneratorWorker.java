package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
@EqualsAndHashCode(of="id")
public class GeneratorWorker implements Comparable<GeneratorWorker> {
	@Setter
	private String name;
	@Setter
	private Long id;
	private Map<String, List<String>> weekPrefers = new ConcurrentHashMap<>();
	private List<String> restrict = new ArrayList<>();
	private Integer priorityByShiftsNumber = 0;
	private Integer workingShiftsCounter = 0;
	@Setter
	private Long hoursPerShift;
	
	public void addPrefers(String dayName, List<String> shiftsNames) {
		weekPrefers.put(dayName, shiftsNames);
		priorityByShiftsNumber += shiftsNames.size();
	}
	
	public void addToSchedule(String shift) {
		LocalDateTime shiftTime = LocalDateTime.parse(shift);
		if(restrict.contains(shiftTime.format(DateTimeFormatter.ISO_DATE_TIME))) {
			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			return;
		}
		LocalDateTime hoursPlus = shiftTime.plusHours(hoursPerShift);
		LocalDateTime hoursMinus = shiftTime.minusHours(hoursPerShift);
		restrict.add(hoursPlus.format(DateTimeFormatter.ISO_DATE_TIME));
		restrict.add(hoursMinus.format(DateTimeFormatter.ISO_DATE_TIME));
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
