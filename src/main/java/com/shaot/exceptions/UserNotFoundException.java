package com.shaot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class UserNotFoundException extends ResponseStatusException {
	
	private static final long serialVersionUID = -590443679144445791L;

	public UserNotFoundException(HttpStatus status) {
		super(status);
	}

}
