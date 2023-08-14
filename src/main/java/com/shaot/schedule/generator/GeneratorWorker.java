package com.shaot.schedule.generator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
@EqualsAndHashCode(of = "id")
public class GeneratorWorker implements Comparable<GeneratorWorker> {
	@Setter
	private String name;
	@Setter
	private Long id;
	private Map<String, List<String>> weekPrefers = new ConcurrentHashMap<>();
	private List<LocalDateTime> restrict = new ArrayList<>();
	@Setter
	private Integer priorityByShiftsNumber = 0;
	private Integer workingShiftsCounter = 0;

	public GeneratorWorker(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public void addPrefers(String dayName, List<String> shiftsNames) {
		weekPrefers.put(dayName, shiftsNames);
		priorityByShiftsNumber += shiftsNames.size();
	}

	public void addToSchedule(LocalDateTime shift, LocalTime shiftEnd, int hoursPerShift) {
		LocalDateTime hoursPlus = shift.plusHours(hoursPerShift);
		if (hoursPlus.equals(shift) && shift.getHour() > shiftEnd.getHour()) {
			hoursPlus = hoursPlus.plusDays(1);
			hoursPlus = hoursPlus.withHour(shiftEnd.getHour());
		}
		restrict.add(hoursPlus);
		++workingShiftsCounter;
	}

	@Override
	public int compareTo(GeneratorWorker o) {
		int workingShiftsComparator = workingShiftsCounter.compareTo(o.getWorkingShiftsCounter());
		if (workingShiftsComparator == 0) {
			return priorityByShiftsNumber.compareTo(o.getPriorityByShiftsNumber());
		}
		return workingShiftsComparator;
	}
}
