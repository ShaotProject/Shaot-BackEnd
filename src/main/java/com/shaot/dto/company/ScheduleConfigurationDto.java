package com.shaot.dto.company;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
	private List<String> workDays;
	private LocalDate alarmPoint;
}
