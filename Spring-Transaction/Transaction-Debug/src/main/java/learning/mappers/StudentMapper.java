package learning.mappers;

import learning.model.Student;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by creasypita on 9/3/2019.
 *
 * @ProjectName: MybatisTutorial
 */
@Component
public interface StudentMapper {
//    @Select(value = "SELECT * FROM STUDENT;")
    List<Student> GetAll();

//    @Select(value = "select * from student where id=#{id}")
    Student getById(int id);

//    @Insert("INSERT INTO STUDENT (name, branch, percentage, phone, email ) values (#{name}, #{branch}, #{percentage}, #{phone}, #{email})")
    void insertStudent(Student student);

    void insertStudents(List<Student> list);
//    @Update("UPDATE STUDENT SET name=#{name}, branch=#{branch}, percentage=#{percentage}, phone=#{phone}, email=#{email} where id=#{id}")
    void updateStudent(Student student);

//    @Delete("delete from student where id=#{id}")
    void deleteStudent(int id);
}
