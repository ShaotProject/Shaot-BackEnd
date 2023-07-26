package com.shaot.dto.company;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyWeekGeneratorDto {
	private List<String> dayNames;
	private List<String> shiftNames;
	private int workersPerShift;
}
