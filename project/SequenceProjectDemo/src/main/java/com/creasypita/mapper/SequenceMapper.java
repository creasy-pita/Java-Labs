package com.creasypita.mapper;

/**
 * Created by lujq on 6/25/2025.
 */
public interface SequenceMapper {
    int updateAndGetNextVal(String seqName);
    Long selectCurrentVal(String seqName);
}
