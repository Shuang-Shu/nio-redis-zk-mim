package com.mdc.mim.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.mdc.mim.redis.dao.MockDao;
import com.mdc.mim.redis.entity.Student;

@Service
public class MockService {
    @Autowired
    MockDao dao;

    @CachePut(value = "a", key = "'student_'+#result.id")
    public Student insert(int id) {
        var student=dao.insert(id);
        return student;
    }

    @Cacheable(value = "a", key = "'student_'+#id")
    public Student getStudent(int id) {
        return dao.selectById(id);
    }

    @CacheEvict(value = "a", key = "'student_'+#id", beforeInvocation = false)
    public void deleteStudent(int id) {
        dao.deleteById(id);
        System.out.println("删除缓存");
    }

    @CachePut(value = "a", key = "'student_'+#result.id")
    public Student update(Student student) {
        return dao.updateById(student.getId(), student);
    }
}
