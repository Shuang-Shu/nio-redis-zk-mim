package com.mdc.mim.redis.dao;

import org.springframework.stereotype.Repository;

import com.mdc.mim.redis.entity.Student;

@Repository
public class MockDao {
    public Student selectById(int id) {
        System.out.println("查找DB");
        return Student.builder().age(10).id(id).name("test").build();
    }

    public void deleteById(int id) {
        System.out.println("删除DB");
    }

    public Student updateById(int id, Student student) {
        System.out.println("更新DB");
        return student;
    }

    public Student insert(int id) {
        System.out.println("插入DB");
        return Student.builder().age(10).id(id).name("test").build();
    }
}
