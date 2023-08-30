package com.shaot.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ShaotLoginUser {
	private long id;
	private String name;
	private String mail;
	private String role;
}
