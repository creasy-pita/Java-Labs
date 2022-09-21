package com.creasypita.transaction.service;

import com.creasypita.transaction.model.Student1;
import com.creasypita.transaction.model.Student2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lujq on 11/14/2021.
 */
@Service
public class CommonService {

    @Autowired
    private Student1Service service1;

    @Autowired
    private Student2Service service2;

    public void notransaction_exception_required_required(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequired(student2);
        throw new RuntimeException();
    }

    public void notransaction_required_required_exception(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredException(student2);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_exception_required_required(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequired(student2);
        throw new RuntimeException();
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_exception(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredException(student2);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_exception_try(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");
        service1.insertRequired(student1);
        try {
            service2.insertRequiredException(student2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void transaction_required_required_InnerSQLExceptionCatched(){
        Student1 student1 = new Student1();
        student1.setName("a");
        Student2 student2 = new Student2();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredWithInnerSQLExceptionCatched(student2);
    }

}
