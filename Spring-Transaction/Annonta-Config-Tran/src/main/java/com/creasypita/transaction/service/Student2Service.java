package com.creasypita.transaction.service;

import com.creasypita.transaction.mappers.Student2Mapper;
import com.creasypita.transaction.model.Student2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lujq on 11/14/2021.
 */
@Service
public class Student2Service {

    @Autowired
    private Student2Mapper student2Mapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequired(Student2 student){
        student2Mapper.insertStudent(student);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredException(Student2 student){
        student2Mapper.insertStudent(student);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void insertRequiredWithInnerSQLExceptionCatched(Student2 student){
        try {
            //手动制造sql异常
            student.setName("55555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555");
            student2Mapper.insertStudent(student);
        }catch (Exception ex){
            System.out.println("Student2Service 发生异常");
        }
    }
}
