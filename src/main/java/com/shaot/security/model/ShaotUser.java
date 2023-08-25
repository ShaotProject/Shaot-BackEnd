package com.shaot.security.model;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ShaotUser implements Principal{
	private String mail;
	@Getter
	private Set<String> roles = new HashSet<>();
	
	public void addRole(String role) {
		roles.add(role);
	}

	@Override
	public String getName() {
		return mail;
	}
}
