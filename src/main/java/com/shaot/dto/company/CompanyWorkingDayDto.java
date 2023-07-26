package com.shaot.dto.company;

import java.util.List;
import java.util.TreeSet;

import org.springframework.data.annotation.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class CompanyWorkingDayDto {
	@Id
	private String dayName;
	@Setter
	private TreeSet<CompanyShiftDto> shifts;
	private int workersQuantityPerShift;
	
	public CompanyWorkingDayDto(String dayName, int workersQuantityPerShift) {
		this.dayName = dayName;
		this.workersQuantityPerShift = workersQuantityPerShift;
		this.shifts = new TreeSet<>();
	}
	
	public boolean addShift(CompanyShiftDto shift) {
		return shifts.add(shift);
	}
	
	public void addWorkersToShift(List<String> workers, String shiftName) {
		shifts.forEach(shift -> {
			if(shift.getShiftName().equals(shiftName)) {
				shift.setWorkers(workers);
			}
		});
	}
}
