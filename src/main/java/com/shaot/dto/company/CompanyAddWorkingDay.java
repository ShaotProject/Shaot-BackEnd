package com.shaot.dto.company;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyAddWorkingDay {
	private String dayName;
	private List<String> shifts;
	private int workersQuantityPerShift;
	
	public CompanyAddWorkingDay(String dayName, int workersQuantityPerShift) {
		this.dayName = dayName;
		this.workersQuantityPerShift = workersQuantityPerShift;
	}
	
	public boolean addShift(String shiftName) {
		return shifts.add(shiftName);
	}
}
