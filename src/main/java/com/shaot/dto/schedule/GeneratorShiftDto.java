package com.shaot.dto.schedule;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class GeneratorShiftDto {
	private String shiftName;
	private String dayName;
	private List<String> workersOnShift;	
}
