package com.mdc.mim.redis;

import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdc.mim.redis.entity.Student;
import com.mdc.mim.redis.service.MockService;
import com.mdc.mim.redis.utils.RedisUtils;

@SpringBootTest
public class SpringDataRedisTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MockService mockService;

    @Autowired
    private RedisUtils redisUtils;

    @Test
    public void testBasicTemplate() {
        // 检查连接是否成功
        Assertions.assertEquals(Boolean.TRUE, stringRedisTemplate.hasKey("test"));
    }

    @Test
    public void testOpForValue() {
        var op = stringRedisTemplate.opsForValue();
        Assertions.assertEquals("good", op.get("test"));
    }

    @Test
    public void testOpForList() {
        var op = stringRedisTemplate.opsForList();
        // 添加元素
        op.leftPush("testl", "a");
        op.leftPush("testl", "b");
        // 测试弹出元素
        Assertions.assertEquals("b", op.leftPop("testl"));
        Assertions.assertEquals("a", op.rightPop("testl"));
    }

    @Test
    public void testBasicService() {
        Assertions.assertNotNull(mockService);
    }

    @Test
    public void testQuery() throws IOException, ClassNotFoundException {
        int id = 10;
        String key = "a::student_" + id;
        stringRedisTemplate.delete(key);
        Assertions.assertEquals(false, stringRedisTemplate.hasKey(key));
        // 1 查询
        var stu = mockService.getStudent(id);
        // 2 检查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 3 反序列化
        var mapper = new ObjectMapper();
        var newStu = mapper.readValue(json, Student.class);
        Assertions.assertEquals(stu, newStu);
    }

    @Test
    public void testUpdate() throws JsonMappingException, JsonProcessingException {
        int id = 10;
        String key = "a::student_" + id;
        stringRedisTemplate.delete(key);
        Assertions.assertEquals(false, stringRedisTemplate.hasKey(key));
        // 1 插入
        var oldStudent = mockService.insert(id);
        Assertions.assertEquals(oldStudent.getAge(), redisUtils.getStudent(key).getAge());
        oldStudent.setId(oldStudent.getAge() + 10);
        // 2 更新
        mockService.update(oldStudent);
        Assertions.assertEquals(oldStudent.getAge(), redisUtils.getStudent(key).getAge());
    }

    @Test
    public void testDelete() throws JsonMappingException, JsonProcessingException {
        int id = 10;
        String key = "a::student_" + id;
        stringRedisTemplate.delete(key);
        var stu = mockService.insert(id);
        Assertions.assertEquals(stu.getAge(), redisUtils.getStudent(key).getAge());
        mockService.deleteStudent(id);
        Assertions.assertEquals(false, stringRedisTemplate.hasKey(key));
    }

}
