package learning.Service;

import learning.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequired(student2);
        throw new RuntimeException();
    }

    public void notransaction_required_required_exception(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredException(student2);
    }

    @Transactional
    public void transaction_exception_required_required(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequired(student2);
        throw new RuntimeException();
    }

    @Transactional
    public void transaction_required_required_exception(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredException(student2);
    }

    @Transactional
    public void transaction_required_required_exception_try(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");
        service1.insertRequired(student1);
        try {
            service2.insertRequiredException(student2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Transactional
    public void transaction_required_required_InnerSQLExceptionCatched(){
        Student student1 = new Student();
        student1.setName("a");
        Student student2 = new Student();
        student2.setName("b");

        service1.insertRequired(student1);
        service2.insertRequiredWithInnerSQLExceptionCatched(student2);
    }

}
