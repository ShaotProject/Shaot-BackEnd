package com.shaot.dto.company;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleConfigurationShiftTime {
	private LocalTime start;
	private LocalTime end;
	private int workersNumberPerShift;
}
