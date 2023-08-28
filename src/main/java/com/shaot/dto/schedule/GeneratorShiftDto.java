package com.shaot.dto.schedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GeneratorShiftDto {
	private LocalDateTime shiftName;
	private String dayName;
	private List<String> workersOnShift;	
}
