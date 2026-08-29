package com.springwater.easybot.clientip;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class ClientIpCache {
    private static final ConcurrentMap<UUID, String> CACHE = new ConcurrentHashMap<>();

    private ClientIpCache() {
    }

    public static void put(UUID uuid, String ip) {
        if (uuid == null || ip == null) {
            return;
        }
        String normalized = ip.trim();
        if (normalized.isEmpty()) {
            return;
        }
        CACHE.put(uuid, normalized);
    }

    public static String get(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        return CACHE.get(uuid);
    }

    public static String getOrDefault(UUID uuid, String fallback) {
        String cached = get(uuid);
        return Objects.requireNonNullElse(cached, fallback);
    }
}
