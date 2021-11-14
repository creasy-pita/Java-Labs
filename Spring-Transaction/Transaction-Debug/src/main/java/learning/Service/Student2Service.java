package learning.Service;

import learning.mappers.StudentMapper;
import learning.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by lujq on 11/14/2021.
 */
@Service
public class Student2Service {

    @Autowired
    private StudentMapper studentMapper;

    public Student getById(int id){
        return studentMapper.getById(id);
    }
    @Transactional
    public void insertRequired(Student student){
        studentMapper.insertStudent(student);
    }

    @Transactional
    public void insertRequiredException(Student student){
        studentMapper.insertStudent(student);
        throw new RuntimeException();
    }

    @Transactional
    public void insertRequiredWithInnerSQLExceptionCatched(Student student){
        try {
            //手动制作sql异常
            student.setName("55555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555");
            studentMapper.insertStudent(student);
        }catch (Exception ex){
            System.out.println("Student1Service 发生异常");
        }
    }
}
