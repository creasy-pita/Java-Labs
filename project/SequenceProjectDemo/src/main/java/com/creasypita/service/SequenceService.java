package com.creasypita.service;

import com.creasypita.mapper.SequenceMapper;
import com.creasypita.utils.MyDbUtil;
import org.apache.ibatis.session.SqlSession;

/**
 * Created by lujq on 6/25/2025.
 */
public class SequenceService {
    public static long nextVal(String seqName, MyDbUtil dbUtilReg3) {
        SqlSession sqlSession = dbUtilReg3.openSession(true);
        SequenceMapper mapper = sqlSession.getMapper(SequenceMapper.class);
        mapper.updateAndGetNextVal(seqName);
        return mapper.selectCurrentVal(seqName);
    }
}
