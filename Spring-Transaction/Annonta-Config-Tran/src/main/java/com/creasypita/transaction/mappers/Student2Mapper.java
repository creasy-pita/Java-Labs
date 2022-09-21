package com.creasypita.transaction.mappers;

import com.creasypita.transaction.model.Student2;
import org.springframework.stereotype.Component;

/**
 * Created by creasypita on 9/3/2019.
 *
 * @ProjectName: MybatisTutorial
 */
@Component
public interface Student2Mapper {
    void insertStudent(Student2 student);
}
