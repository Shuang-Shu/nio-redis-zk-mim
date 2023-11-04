package com.mdc.mim.redis.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdc.mim.redis.entity.Student;

@Component
public class RedisUtils {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    ObjectMapper mapper = new ObjectMapper();

    public Student getStudent(String key) throws JsonMappingException, JsonProcessingException {
        var jsonStr = stringRedisTemplate.opsForValue().get(key);
        return mapper.readValue(jsonStr, Student.class);
    }
}
