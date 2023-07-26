package com.shaot.dto.company;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompanyForWorkerDto {
	private long id;
	private String name;
	
	public CompanyForWorkerDto(long id) {
		this.id = id;
	}
}
