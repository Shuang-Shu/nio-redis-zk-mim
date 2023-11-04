package com.mdc.mim;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisTest {
    Jedis jedis;

    static JedisPool jedisPool;
    static final int MAX_IDLE = 4;
    static final int MAX_TOTAL = 8;
    static final int MAX_WAIT_MS = 10 * 1000;

    @BeforeAll
    public static void initCreateJedisPool() {
        var config = new JedisPoolConfig();
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT_MS);
        config.setTestOnBorrow(false);
        jedisPool = new JedisPool(config, "localhost", 6379, 10000);
    }

    @BeforeEach
    public void init() {
        jedis = new Jedis("localhost", 6379);
    }

    @BeforeEach
    public void destroy() {
        jedis.close();
    }

    @Test
    public void basicTest() {
        jedis.set("test", "good");
        System.out.println(jedis.keys("*"));
    }

    @Test
    public void testJedisPool() {
        var jedis = jedisPool.getResource();
        System.out.println(jedis.keys("*"));
    }
}
