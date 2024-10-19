package com.aboher.sessionsecureapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, String> redisTemplate;

    public Set<String> getPrincipalNamesOfAllTheUsersWithActiveSessions() {
        String pattern = "spring:session:index:org.springframework.session.FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME:*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null) {
            return keys.stream()
                    .map(key -> key.substring(pattern.length() - 1))
                    .collect(Collectors.toSet());
        }
        return null;
    }
}
