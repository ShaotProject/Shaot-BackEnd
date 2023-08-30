package com.shaot.schedule.generator;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of="dayDate")
@AllArgsConstructor
public class DayView implements Comparable<DayView>{
	private LocalDate dayDate;
	private String dayName;
	List<ShiftView> shifts;
	
	@Override
	public int compareTo(DayView o) {
		return dayDate.compareTo(o.getDayDate());
	}
}
