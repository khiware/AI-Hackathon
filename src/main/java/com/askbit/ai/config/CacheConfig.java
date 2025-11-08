package com.askbit.ai.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("queries", "documents");
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(5000)  // Increased from 1000 for more popular questions
                .expireAfterWrite(7200, TimeUnit.SECONDS)  // 2 hours TTL
                .expireAfterAccess(3600, TimeUnit.SECONDS)  // 1 hour idle timeout
                .recordStats()
                .initialCapacity(100));  // Pre-allocate space for faster writes
        return cacheManager;
    }
}


