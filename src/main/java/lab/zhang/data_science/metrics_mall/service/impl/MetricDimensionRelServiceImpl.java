package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper;
import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO;
import lab.zhang.data_science.metrics_mall.service.DimensionService;
import lab.zhang.data_science.metrics_mall.service.MetricDimensionRelService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
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
    private MetricService metricService;

    @Autowired
    private DimensionService dimensionService;

    @Autowired
    private MetricDimensionRelMapper metricDimensionRelMapper;


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
    public boolean create(MetricDimensionRelDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] creating failed, dto cannot be null");
        }

        // validate the metric existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] creating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();

        // validate the dimension existence
        Dimension dimension = dimensionService.getByCode(dto.getDimensionCode());
        if (dimension == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] creating failed, dimension not found: dimensionCode=" + dto.getDimensionCode());
        }
        Long dimensionId = dimension.getId();

        // validate the relation existence
        QueryWrapper<MetricDimensionRelDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mid", metricMetaId)
                .eq("did", dimensionId);
        MetricDimensionRelDAO existingRel = metricDimensionRelMapper.selectOne(queryWrapper);
        if (existingRel != null) {
            throw new IllegalArgumentException(String.format("[metric_dimension_rel] creating failed, relation already exists: metricMetaId=%d, dimensionId=%d",
                    metricMetaId, dimensionId));
        }

        Integer isHot = dto.getIsHot();
        String validation = dto.getValidation();

        // insert new relation
        MetricDimensionRelDAO dao = new MetricDimensionRelDAO();
        dao.setMetricMetaId(metricMetaId);
        dao.setDimensionId(dimensionId);
        dao.setIsHot(isHot);
        dao.setValidation(validation);

        int rows = metricDimensionRelMapper.insert(dao);
        if (rows > 0) {
            if (log.isDebugEnabled()) {
                log.debug("[metric_dimension_rel] create success, metricMetaId={}, dimensionId={}, isHot={}",
                        metricMetaId, dimensionId, isHot);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean update(MetricDimensionRelDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] updating failed, dto cannot be null");
        }

        // validate the metric existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] updating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();

        // validate the dimension existence
        Dimension dimension = dimensionService.getByCode(dto.getDimensionCode());
        if (dimension == null) {
            throw new IllegalArgumentException("[metric_dimension_rel] updating failed, dimension not found: dimensionCode=" + dto.getDimensionCode());
        }
        Long dimensionId = dimension.getId();

        // validate the relation existence
        QueryWrapper<MetricDimensionRelDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mid", metricMetaId)
                .eq("did", dimensionId);
        MetricDimensionRelDAO existingRel = metricDimensionRelMapper.selectOne(queryWrapper);
        if (existingRel == null) {
            throw new IllegalArgumentException(String.format("[metric_dimension_rel] updating failed, relation not exists: metricMetaId=%d, dimensionId=%d",
                    metricMetaId, dimensionId));
        }

        Integer isHot = dto.getIsHot();
        String validation = dto.getValidation();

        // insert new relation
        MetricDimensionRelDAO dao = new MetricDimensionRelDAO();
        dao.setMetricMetaId(metricMetaId);
        dao.setDimensionId(dimensionId);
        dao.setIsHot(isHot);
        dao.setValidation(validation);

        int rows = metricDimensionRelMapper.updateByPrimaryKey(dao);
        if (rows > 0) {
            if (log.isDebugEnabled()) {
                log.debug("[metric_dimension_rel] update success, metricMetaId={}, dimensionId={}, isHot={}",
                        metricMetaId, dimensionId, isHot);
            }
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

