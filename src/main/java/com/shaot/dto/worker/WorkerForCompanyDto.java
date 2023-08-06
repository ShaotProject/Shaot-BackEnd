package com.shaot.dto.worker;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class WorkerForCompanyDto {
	private long id;
	@Setter
	private String name;
	@Setter
	private double wage;
	@Setter
	private boolean individualWage;
	private int shiftsCounter;
	private int priorityByShiftsNumber;
	
	public WorkerForCompanyDto(long id, String name) {
		this.name = name;
		this.id = id;
	}
	
	public void addShiftCounter() {
		shiftsCounter++;
	}
	
	public void addPriorityByShiftsNumber(int shiftsNumber) {
		priorityByShiftsNumber += shiftsNumber;
	}
	
}
