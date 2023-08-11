package com.shaot.dto.worker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class WorkerPreferShiftsDto {
	private String dayName;
	//List of Strings or List of objects?
	private List<String> shifts;
}
