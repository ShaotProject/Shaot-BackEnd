package com.shaot.dto.company;

import java.time.LocalDate;
import java.time.LocalTime;

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
	private String dayName;
    private LocalDate dayDate;
    private LocalTime shiftStart;
    private LocalTime shiftEnd;
    private int workerNeeded;
}
