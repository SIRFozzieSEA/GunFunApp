package com.codef.gunfunapp;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@SuppressWarnings("serial")
@ResponseStatus(HttpStatus.NOT_FOUND)
public class GunFunResourceNotFoundException extends RuntimeException {

	public GunFunResourceNotFoundException(String message) {
		super(message);
	}
}
