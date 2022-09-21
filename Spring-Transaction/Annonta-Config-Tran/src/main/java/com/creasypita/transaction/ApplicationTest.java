package com.creasypita.transaction;

import com.creasypita.transaction.model.Student1;
import com.creasypita.transaction.service.CommonService;
import com.creasypita.transaction.service.Student1Service;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Created by lujq on 11/9/2021.
 */
@Configuration
public class ApplicationTest {
    public static void main(String[] args) {
        demo();

    }

    // 场景
    public static void demo(){
        ApplicationContext context = new AnnotationConfigApplicationContext("com.creasypita.transaction");
        CommonService commonService = context.getBean(CommonService.class);
//        commonService.notransaction_exception_required_required();
//        commonService.notransaction_required_required_exception();
//        commonService.transaction_exception_required_required();
//        commonService.transaction_required_required_exception();
        commonService.transaction_required_required_exception_try();
//        commonService.transaction_required_required_InnerSQLExceptionCatched();


//        Student1Service student1Service = context.getBean(Student1Service.class);
//        Student1 student1 = new Student1();
//        student1.setId(222);
//        student1.setName("var");
//        student1Service.insertRequired(student1);


        System.out.println("update complete");
    }


}
