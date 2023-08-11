package com.shaot.dto.schedule;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GeneratorShiftDto {
	private String shiftName;
	private LocalDate dayName;
	private List<String> workersOnShift;	
}
