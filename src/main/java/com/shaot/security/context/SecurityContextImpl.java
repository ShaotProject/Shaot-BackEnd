package com.shaot.security.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.shaot.security.model.ShaotUser;

@Component
public class SecurityContextImpl implements SecurityContext {

	private Map<String, ShaotUser> context = new ConcurrentHashMap<>();
	
	@Override
	public ShaotUser addUserSession(String sessionId, ShaotUser user) {
		return context.put(sessionId, user);
	}

	@Override
	public ShaotUser removeUserSession(String sessionId) {
		return context.remove(sessionId);
	}

	@Override
	public ShaotUser getUserBySession(String sessionId) {
		return context.get(sessionId);
	}

}
