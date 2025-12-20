package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import lab.zhang.data_science.metrics_mall.mapper.EntityMetricRelMapper;
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Entity Metric Relation service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityMetricRelServiceImpl implements EntityMetricRelService {

    @Autowired
    private EntityMetricRelMapper entityMetricRelMapper;

    @Autowired
    private EntityMetaMapper entityMetaMapper;

    @Autowired
    private MetricMetaMapper metricMetaMapper;

    @Override
    public boolean create(Long entityMetaId, Long metricMetaId, String alias, String dataUri) {
        if (entityMetaId == null || metricMetaId == null) {
            throw new IllegalArgumentException("[entity_metric_rel] entityMetaId and metricMetaId cannot be null");
        }
        if (StrUtil.isBlank(alias)) {
            throw new IllegalArgumentException("[entity_metric_rel] alias cannot be blank");
        }
        if (StrUtil.isBlank(dataUri)) {
            throw new IllegalArgumentException("[entity_metric_rel] dataUri cannot be blank");
        }

        // validate entity meta exists
        EntityMetaDAO entityMetaDAO = entityMetaMapper.selectById(entityMetaId);
        if (entityMetaDAO == null) {
            log.warn("[entity_metric_rel] entity meta not found: entityMetaId={}", entityMetaId);
            throw new IllegalArgumentException("[entity_metric_rel] entity meta not found: entityMetaId=" + entityMetaId);
        }

        // validate metric meta exists
        MetricMetaDAO metricMetaDAO = metricMetaMapper.selectById(metricMetaId);
        if (metricMetaDAO == null) {
            log.warn("[entity_metric_rel] metric meta not found: metricMetaId={}", metricMetaId);
            throw new IllegalArgumentException("[entity_metric_rel] metric meta not found: metricMetaId=" + metricMetaId);
        }

        // check if relation already exists
        LambdaQueryWrapper<EntityMetricRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetricRelDAO::getEntityMetaId, entityMetaId)
                .eq(EntityMetricRelDAO::getMetricMetaId, metricMetaId);
        EntityMetricRelDAO existingRel = entityMetricRelMapper.selectOne(queryWrapper);
        if (existingRel != null) {
            log.warn("[entity_metric_rel] relation already exists: entityMetaId={}, metricMetaId={}", 
                    entityMetaId, metricMetaId);
            throw new IllegalArgumentException(
                    String.format("[entity_metric_rel] relation already exists: entityMetaId=%d, metricMetaId=%d", 
                            entityMetaId, metricMetaId));
        }

        // insert new relation
        EntityMetricRelDAO dao = new EntityMetricRelDAO();
        dao.setEntityMetaId(entityMetaId);
        dao.setMetricMetaId(metricMetaId);
        dao.setAlias(alias);
        dao.setDataUri(dataUri);

        int rows = entityMetricRelMapper.insert(dao);
        if (rows > 0) {
            log.info("[entity_metric_rel] created: entityMetaId={}, metricMetaId={}, alias={}", 
                    entityMetaId, metricMetaId, alias);
            return true;
        }
        return false;
    }

    @Override
    public EntityMetricRelDAO get(Long entityMetaId, Long metricMetaId) {
        if (entityMetaId == null || metricMetaId == null) {
            return null;
        }
        LambdaQueryWrapper<EntityMetricRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetricRelDAO::getEntityMetaId, entityMetaId)
                .eq(EntityMetricRelDAO::getMetricMetaId, metricMetaId);
        return entityMetricRelMapper.selectOne(queryWrapper);
    }

    @Override
    public List<EntityMetricRelDAO> listByEntityMetaId(Long entityMetaId) {
        if (entityMetaId == null) {
            return List.of();
        }
        LambdaQueryWrapper<EntityMetricRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetricRelDAO::getEntityMetaId, entityMetaId);
        return entityMetricRelMapper.selectList(queryWrapper);
    }

    @Override
    public List<EntityMetricRelDAO> listByMetricMetaId(Long metricMetaId) {
        if (metricMetaId == null) {
            return List.of();
        }
        LambdaQueryWrapper<EntityMetricRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetricRelDAO::getMetricMetaId, metricMetaId);
        return entityMetricRelMapper.selectList(queryWrapper);
    }

    @Override
    public List<EntityMetricRelDAO> list() {
        return entityMetricRelMapper.selectList(null);
    }

    @Override
    public boolean update(Long entityMetaId, Long metricMetaId, String alias, String dataUri) {
        if (entityMetaId == null || metricMetaId == null) {
            throw new IllegalArgumentException("[entity_metric_rel] entityMetaId and metricMetaId cannot be null");
        }
        if (StrUtil.isBlank(alias)) {
            throw new IllegalArgumentException("[entity_metric_rel] alias cannot be blank");
        }
        if (StrUtil.isBlank(dataUri)) {
            throw new IllegalArgumentException("[entity_metric_rel] dataUri cannot be blank");
        }

        // check if relation exists
        EntityMetricRelDAO existingRel = get(entityMetaId, metricMetaId);
        if (existingRel == null) {
            log.warn("[entity_metric_rel] relation not found: entityMetaId={}, metricMetaId={}", 
                    entityMetaId, metricMetaId);
            throw new IllegalArgumentException(
                    String.format("[entity_metric_rel] relation not found: entityMetaId=%d, metricMetaId=%d", 
                            entityMetaId, metricMetaId));
        }

        // update relation
        UpdateWrapper<EntityMetricRelDAO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("eid", entityMetaId)
                .eq("mid", metricMetaId)
                .set("alias", alias)
                .set("data_uri", dataUri);

        int rows = entityMetricRelMapper.update(null, updateWrapper);
        if (rows > 0) {
            log.info("[entity_metric_rel] updated: entityMetaId={}, metricMetaId={}, alias={}", 
                    entityMetaId, metricMetaId, alias);
            return true;
        }
        return false;
    }

    @Override
    public boolean delete(Long entityMetaId, Long metricMetaId) {
        if (entityMetaId == null || metricMetaId == null) {
            throw new IllegalArgumentException("[entity_metric_rel] entityMetaId and metricMetaId cannot be null");
        }

        LambdaQueryWrapper<EntityMetricRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetricRelDAO::getEntityMetaId, entityMetaId)
                .eq(EntityMetricRelDAO::getMetricMetaId, metricMetaId);

        int rows = entityMetricRelMapper.delete(queryWrapper);
        if (rows > 0) {
            log.info("[entity_metric_rel] deleted: entityMetaId={}, metricMetaId={}", 
                    entityMetaId, metricMetaId);
            return true;
        }
        return false;
    }
}

