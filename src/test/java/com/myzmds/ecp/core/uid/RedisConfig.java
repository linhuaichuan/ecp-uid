package com.myzmds.ecp.core.uid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@PropertySource("classpath:redis.properties")
public class RedisConfig {
    @Autowired
    private Environment environment;
    
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        JedisConnectionFactory fac = new JedisConnectionFactory();
        fac.setHostName(environment.getProperty("redis.hostName"));
        fac.setPort(Integer.parseInt(environment.getProperty("redis.port")));
        fac.setPassword(environment.getProperty("redis.password"));
        fac.setTimeout(Integer.parseInt(environment.getProperty("redis.timeout")));
        fac.getPoolConfig().setMaxIdle(Integer.parseInt(environment.getProperty("redis.maxIdle")));
        fac.getPoolConfig().setMaxTotal(Integer.parseInt(environment.getProperty("redis.maxTotal")));
        fac.getPoolConfig().setMaxWaitMillis(Integer.parseInt(environment.getProperty("redis.maxWaitMillis")));
        fac.getPoolConfig().setMinEvictableIdleTimeMillis(Integer.parseInt(environment.getProperty("redis.minEvictableIdleTimeMillis")));
        fac.getPoolConfig().setNumTestsPerEvictionRun(Integer.parseInt(environment.getProperty("redis.numTestsPerEvictionRun")));
        fac.getPoolConfig().setTimeBetweenEvictionRunsMillis(Integer.parseInt(environment.getProperty("redis.timeBetweenEvictionRunsMillis")));
        fac.getPoolConfig().setTestOnBorrow(Boolean.parseBoolean(environment.getProperty("redis.testOnBorrow")));
        fac.getPoolConfig().setTestWhileIdle(Boolean.parseBoolean(environment.getProperty("redis.testWhileIdle")));
        return fac;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redis = new RedisTemplate<>();
        StringRedisSerializer serializer = new StringRedisSerializer();
        redis.setKeySerializer(serializer);
        redis.setConnectionFactory(redisConnectionFactory);
        redis.afterPropertiesSet();
        return redis;
    }
}