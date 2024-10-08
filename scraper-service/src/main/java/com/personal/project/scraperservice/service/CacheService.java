package com.personal.project.scraperservice.service;

import org.redisson.api.RLock;

public interface CacheService {

	RLock getLock(String lock);
}
