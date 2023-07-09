package com.yt.ytbibackend.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissionConfig  {

    private Integer database;

    private String address;

    private Integer port;

    private String password;

    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setDatabase(database)
                .setAddress("redis://" + address + ":" + port)
                .setPassword(password);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }

}
