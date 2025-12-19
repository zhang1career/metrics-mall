package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricVersionMapper;
import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.service.MetricVersionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricVersionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Metric version service implementation.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricVersionServiceImpl implements MetricVersionService {

    @Autowired
    private MetricVersionMapper metricVersionMapper;

    @Autowired
    private MetricVersionStructMapper metricVersionStructMapper;

    @Override
    public MetricVersion get(Long id) {
        if (id == null) {
            return null;
        }
        MetricVersionDAO dao = metricVersionMapper.selectById(id);
        return metricVersionStructMapper.daoToModel(dao);
    }

    @Override
    public List<MetricVersion> list(MetricVersion queryModel) {
        LambdaQueryWrapper<MetricVersionDAO> queryWrapper = new LambdaQueryWrapper<>();
        if (queryModel.getMetricId() != null) {
            queryWrapper.eq(MetricVersionDAO::getMetricId, queryModel.getMetricId());
        }
        if (queryModel.getVersion() != null) {
            queryWrapper.eq(MetricVersionDAO::getVersion, queryModel.getVersion());
        }
        if (queryModel.getIsMain() != null) {
            queryWrapper.eq(MetricVersionDAO::getIsMain, queryModel.getIsMain());
        }
        if (queryModel.getLifeStatus() != null) {
            queryWrapper.eq(MetricVersionDAO::getLifeStatus, queryModel.getLifeStatus());
        }
        List<MetricVersionDAO> daoList = metricVersionMapper.selectList(queryWrapper);
        return metricVersionStructMapper.daoToModelBatch(daoList);
    }

    @Override
    public boolean insert(MetricVersion model) {
        if (model == null) {
            return false;
        }
        MetricVersionDAO dao = metricVersionStructMapper.modelToDao(model);
        dao.setTimeOnCreate();
        int rows = metricVersionMapper.insert(dao);
        if (rows > 0) {
            model.setId(dao.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean update(MetricVersion model) {
        if (model == null || model.getId() == null) {
            return false;
        }
        MetricVersionDAO dao = metricVersionStructMapper.modelToDao(model);
        dao.setTimeOnUpdate();
        return metricVersionMapper.updateById(dao) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return metricVersionMapper.deleteById(id) > 0;
    }
}

