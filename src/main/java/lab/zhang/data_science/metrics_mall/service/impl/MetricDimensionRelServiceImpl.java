package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lab.zhang.data_science.metrics_mall.mapper.DimensionMapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.service.MetricDimensionRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Metric Dimension Relation service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricDimensionRelServiceImpl implements MetricDimensionRelService {

    @Autowired
    private MetricDimensionRelMapper metricDimensionRelMapper;

    @Autowired
    private MetricMetaMapper metricMetaMapper;

    @Autowired
    private DimensionMapper dimensionMapper;

    @Override
    public boolean create(Long metricMetaId, Long dimensionId, Integer isHot, String validation) {
        if (metricMetaId == null || dimensionId == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] metricMetaId and dimensionId cannot be null");
        }
        if (isHot == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] isHot cannot be null");
        }

        // validate metric meta exists
        MetricMetaDAO metricMetaDAO = metricMetaMapper.selectById(metricMetaId);
        if (metricMetaDAO == null) {
            log.warn("[metric_dimension_rel] metric meta not found: metricMetaId={}", metricMetaId);
            throw new IllegalArgumentException("[metric_dimension_rel] metric meta not found: metricMetaId=" + metricMetaId);
        }

        // validate dimension exists
        DimensionDAO dimensionDAO = dimensionMapper.selectById(dimensionId);
        if (dimensionDAO == null) {
            log.warn("[metric_dimension_rel] dimension not found: dimensionId={}", dimensionId);
            throw new IllegalArgumentException("[metric_dimension_rel] dimension not found: dimensionId=" + dimensionId);
        }

        // check if relation already exists
        LambdaQueryWrapper<MetricDimensionRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionRelDAO::getMetricMetaId, metricMetaId)
                .eq(MetricDimensionRelDAO::getDimensionId, dimensionId);
        MetricDimensionRelDAO existingRel = metricDimensionRelMapper.selectOne(queryWrapper);
        if (existingRel != null) {
            log.warn("[metric_dimension_rel] relation already exists: metricMetaId={}, dimensionId={}", 
                    metricMetaId, dimensionId);
            throw new IllegalArgumentException(
                    String.format("[metric_dimension_rel] relation already exists: metricMetaId=%d, dimensionId=%d", 
                            metricMetaId, dimensionId));
        }

        // insert new relation
        MetricDimensionRelDAO dao = new MetricDimensionRelDAO();
        dao.setMetricMetaId(metricMetaId);
        dao.setDimensionId(dimensionId);
        dao.setIsHot(isHot);
        dao.setValidation(validation);

        int rows = metricDimensionRelMapper.insert(dao);
        if (rows > 0) {
            log.info("[metric_dimension_rel] created: metricMetaId={}, dimensionId={}, isHot={}", 
                    metricMetaId, dimensionId, isHot);
            return true;
        }
        return false;
    }

    @Override
    public MetricDimensionRelDAO get(Long metricMetaId, Long dimensionId) {
        if (metricMetaId == null || dimensionId == null) {
            return null;
        }
        LambdaQueryWrapper<MetricDimensionRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionRelDAO::getMetricMetaId, metricMetaId)
                .eq(MetricDimensionRelDAO::getDimensionId, dimensionId);
        return metricDimensionRelMapper.selectOne(queryWrapper);
    }

    @Override
    public List<MetricDimensionRelDAO> listByMetricMetaId(Long metricMetaId) {
        if (metricMetaId == null) {
            return List.of();
        }
        LambdaQueryWrapper<MetricDimensionRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionRelDAO::getMetricMetaId, metricMetaId);
        return metricDimensionRelMapper.selectList(queryWrapper);
    }

    @Override
    public List<MetricDimensionRelDAO> listByDimensionId(Long dimensionId) {
        if (dimensionId == null) {
            return List.of();
        }
        LambdaQueryWrapper<MetricDimensionRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionRelDAO::getDimensionId, dimensionId);
        return metricDimensionRelMapper.selectList(queryWrapper);
    }

    @Override
    public List<MetricDimensionRelDAO> list() {
        return metricDimensionRelMapper.selectList(null);
    }

    @Override
    public boolean update(Long metricMetaId, Long dimensionId, Integer isHot, String validation) {
        if (metricMetaId == null || dimensionId == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] metricMetaId and dimensionId cannot be null");
        }
        if (isHot == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] isHot cannot be null");
        }

        // check if relation exists
        MetricDimensionRelDAO existingRel = get(metricMetaId, dimensionId);
        if (existingRel == null) {
            log.warn("[metric_dimension_rel] relation not found: metricMetaId={}, dimensionId={}", 
                    metricMetaId, dimensionId);
            throw new IllegalArgumentException(
                    String.format("[metric_dimension_rel] relation not found: metricMetaId=%d, dimensionId=%d", 
                            metricMetaId, dimensionId));
        }

        // update relation
        UpdateWrapper<MetricDimensionRelDAO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("mid", metricMetaId)
                .eq("did", dimensionId)
                .set("is_hot", isHot)
                .set("validation", validation);

        int rows = metricDimensionRelMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("[metric_dimension_rel] updated: metricMetaId={}, dimensionId={}, isHot={}", 
                    metricMetaId, dimensionId, isHot);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Long metricMetaId, Long dimensionId) {
        if (metricMetaId == null || dimensionId == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] metricMetaId and dimensionId cannot be null");
        }

        LambdaQueryWrapper<MetricDimensionRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionRelDAO::getMetricMetaId, metricMetaId)
                .eq(MetricDimensionRelDAO::getDimensionId, dimensionId);

        int rows = metricDimensionRelMapper.delete(queryWrapper);
        if (rows > 0) {
            log.info("[metric_dimension_rel] deleted: metricMetaId={}, dimensionId={}", 
                    metricMetaId, dimensionId);
            return true;
        }
        return false;
    }
}

