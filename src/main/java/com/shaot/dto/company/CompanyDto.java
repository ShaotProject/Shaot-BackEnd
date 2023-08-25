package com.shaot.dto.company;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CompanyDto {
	private long id;
	private String name;
	private String mail;
	private double generalWage;
	private String password;
}
