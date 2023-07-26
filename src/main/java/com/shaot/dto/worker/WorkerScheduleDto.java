package com.shaot.dto.worker;

import java.util.ArrayList;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode(of="companyName")
public class WorkerScheduleDto {
	private String companyName;
	private List<WorkerDayDto> shifts = new ArrayList<>();
	
	public WorkerScheduleDto(String companyName) {
		this.companyName = companyName;
	}
	
	public boolean addShiftToSchedule(WorkerDayDto workerDay) {
		if(!shifts.contains(workerDay)) {
			return shifts.add(workerDay);
		}
		return false;
	}
}
