package com.shaot.dto.company;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyAddShiftDto {
	private String dayName;
	private String shiftName;
	private int workersQuantity;
}
