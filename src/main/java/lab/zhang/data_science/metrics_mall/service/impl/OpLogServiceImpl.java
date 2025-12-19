package lab.zhang.data_science.metrics_mall.service.impl;

import lab.zhang.data_science.metrics_mall.mapper.OpLogMapper;
import lab.zhang.data_science.metrics_mall.model.OpLog;
import lab.zhang.data_science.metrics_mall.pojo.dao.OpLogDAO;
import lab.zhang.data_science.metrics_mall.service.OpLogService;
import lab.zhang.data_science.metrics_mall.struct_mapper.OpLogStructMapper;
import lab.zhang.data_science.metrics_mall.util.TimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Operation log service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OpLogServiceImpl implements OpLogService {

    private final OpLogMapper opLogMapper;

    private final OpLogStructMapper opLogStructMapper;

    @Override
    public OpLog get(Long id) {
        if (id == null) {
            return null;
        }
        OpLogDAO dao = opLogMapper.selectById(id);
        return opLogStructMapper.daoToModel(dao);
    }

    @Override
    public List<OpLog> list() {
        List<OpLogDAO> daoList = opLogMapper.selectList(null);
        return opLogStructMapper.daoToModelBatch(daoList);
    }

    @Override
    public Long count() {
        return opLogMapper.selectCount(null);
    }

    @Override
    public OpLog insert(OpLog model) {
        if (model == null) {
            return null;
        }
        OpLogDAO dao = opLogStructMapper.modelToDao(model);
        if (dao.getCt() == null) {
            dao.setCt(TimeUtil.getCurrentTime());
        }
        opLogMapper.insert(dao);
        return opLogStructMapper.daoToModel(dao);
    }

    @Override
    public boolean update(OpLog model) {
        if (model == null || model.getId() == null) {
            return false;
        }
        OpLogDAO dao = opLogStructMapper.modelToDao(model);
        return opLogMapper.updateById(dao) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return opLogMapper.deleteById(id) > 0;
    }
}

