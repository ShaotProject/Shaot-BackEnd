package com.shaot.dto.worker;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkerShiftView {
	private String dayName;
	private LocalDateTime shiftName;
}
