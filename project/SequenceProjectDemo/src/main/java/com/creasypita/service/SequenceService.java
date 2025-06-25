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
        try {
            mapper.insertSequence(seqName, 0L);
        } catch (Exception e) {
//        } catch (DuplicateKeyException e) {
            // 已存在序列，忽略
//            System.out.println(e.getMessage());
        }
        mapper.updateAndGetNextVal(seqName);
        return mapper.selectCurrentVal(seqName);
    }
}
