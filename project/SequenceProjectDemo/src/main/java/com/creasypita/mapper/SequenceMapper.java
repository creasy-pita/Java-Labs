package com.creasypita.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * Created by lujq on 6/25/2025.
 */
public interface SequenceMapper {
    int updateAndGetNextVal(String seqName);
    Long selectCurrentVal(String seqName);
    int insertSequence(@Param("seqName") String seqName, @Param("initVal") long initVal);
}
