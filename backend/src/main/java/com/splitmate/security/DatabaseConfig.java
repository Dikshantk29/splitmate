package com.splitmate.security;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
public class DatabaseConfig {
    @Bean
    DataSource dataSource(Environment environment) {
        String jdbcUrl = environment.getProperty("DB_URL");
        String username = environment.getProperty("DB_USERNAME", environment.getProperty("spring.datasource.username"));
        String password = environment.getProperty("DB_PASSWORD", environment.getProperty("spring.datasource.password"));

        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            String databaseUrl = environment.getProperty("DATABASE_URL");
            if (databaseUrl != null && !databaseUrl.isBlank()) {
                URI uri = URI.create(databaseUrl);
                if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
                    throw new IllegalStateException("DATABASE_URL must be a PostgreSQL connection URL");
                }
                String query = uri.getRawQuery();
                if (query != null) query = query.replace("channel_binding=", "channelBinding=");
                jdbcUrl = "jdbc:postgresql://" + uri.getHost()
                        + (uri.getPort() > 0 ? ":" + uri.getPort() : "")
                        + uri.getRawPath()
                        + (query == null ? "" : "?" + query);
                String userInfo = uri.getRawUserInfo();
                if (userInfo != null) {
                    String[] credentials = userInfo.split(":", 2);
                    username = decode(credentials[0]);
                    if (credentials.length > 1) password = decode(credentials[1]);
                }
            } else {
                jdbcUrl = environment.getProperty("spring.datasource.url");
            }
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(jdbcUrl);
        if (username != null && !username.isBlank()) config.setUsername(username);
        if (password != null) config.setPassword(password);
        return new HikariDataSource(config);
    }

    private String decode(String value) {
        return URLDecoder.decode(value.replace("+", "%2B"), StandardCharsets.UTF_8);
    }
}
