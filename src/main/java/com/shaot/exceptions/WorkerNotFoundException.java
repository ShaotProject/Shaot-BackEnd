package com.shaot.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkerNotFoundException extends ResponseStatusException {
	
	public WorkerNotFoundException(HttpStatus status) {
		super(status);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4019810984988391673L;
}

