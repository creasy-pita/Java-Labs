package learning.mappers;

import learning.model.Student1;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by creasypita on 9/3/2019.
 *
 * @ProjectName: MybatisTutorial
 */
@Component
public interface Student1Mapper {
    void insertStudent(Student1 student);
}
