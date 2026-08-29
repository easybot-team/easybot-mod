package com.springwater.easybot.clientip;

import com.springwater.easybot.config.ConfigLoader;
import com.springwater.easybot.platforms.ModData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClientIpResolver {
    private static final Pattern JSON_IP_PATTERN = Pattern.compile("\"ip\"\\s*:\\s*\"([^\"]+)\"");

    private ClientIpResolver() {
    }

    public static String fetchPublicIp() {
        try {
            if (ConfigLoader.get().getClient() == null || !ConfigLoader.get().getClient().isEnableClientIpSync()) {
                return null;
            }
        } catch (IllegalStateException ignored) {
            return null;
        }

        String endpoint = ConfigLoader.get().getClient().getPublicIpServiceUrl();
        if (endpoint == null || endpoint.trim().isEmpty()) {
            return null;
        }

        int timeout = Math.max(1000, ConfigLoader.get().getClient().getPublicIpRequestTimeoutMs());

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endpoint).openConnection();
            connection.setConnectTimeout(timeout);
            connection.setReadTimeout(timeout);
            connection.setUseCaches(false);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json,text/plain;q=0.9,*/*;q=0.8");

            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                ModData.LOGGER.warn("公网IP查询失败,HTTP状态码={}", status);
                return null;
            }

            String body = readBody(connection);
            String ip = extractIp(body);
            if (ip == null || ip.isBlank()) {
                ModData.LOGGER.warn("公网IP查询失败,响应内容无法解析: {}", body);
                return null;
            }

            return ip.trim();
        } catch (IOException e) {
            ModData.LOGGER.warn("公网IP查询失败: {}", e.getMessage());
            return null;
        }
    }

    private static String readBody(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString().trim();
        }
    }

    private static String extractIp(String body) {
        if (body == null) {
            return null;
        }

        Matcher matcher = JSON_IP_PATTERN.matcher(body);
        if (matcher.find()) {
            return matcher.group(1);
        }

        String trimmed = body.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
