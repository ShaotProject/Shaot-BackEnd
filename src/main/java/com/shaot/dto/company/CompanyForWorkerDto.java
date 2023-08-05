package com.shaot.dto.company;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CompanyForWorkerDto {
	private long id;
	@Setter
	private String name;
	
	public CompanyForWorkerDto(long id) {
		this.id = id;
	}
}
