package com.shaot.dto.company;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyShiftDto {
	private String dayName;
	private String shiftName;
	@Setter
	private List<String> workers;
}
