package com.shaot.security.context;

import com.shaot.security.model.ShaotUser;

public interface SecurityContext {
	ShaotUser addUserSession(String sessionId, ShaotUser user);
	
	ShaotUser removeUserSession(String sessionId);
	
	ShaotUser getUserBySession(String sessionId);
}
