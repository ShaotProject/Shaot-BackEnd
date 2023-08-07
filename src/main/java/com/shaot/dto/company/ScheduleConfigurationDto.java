package com.shaot.dto.company;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ScheduleConfigurationDto {
	private LocalDate weekStart;
	private LocalDate weekEnd;
	private List<ScheduleConfigurationShiftTime> shiftsTime;
	private int workersNumberPerShift;
}
