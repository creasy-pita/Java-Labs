package com.creasypita.transaction.service;

import com.creasypita.transaction.mappers.Student1Mapper;
import com.creasypita.transaction.model.Student1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lujq on 11/14/2021.
 */
@Service
public class Student1Service {
    @Autowired
    private Student1Mapper student1Mapper;


    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequired(Student1 student){
        student1Mapper.insertStudent(student);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredException(Student1 student){
        student1Mapper.insertStudent(student);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredWithSQLExceptionCatched(Student1 student){
        try {
            //手动制造sql异常
            student.setName("66555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555");
            student1Mapper.insertStudent(student);
        }catch (Exception ex){
            System.out.println("Student1Service 发生异常");
        }
    }
}
