package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.EntityMetricRelMapper;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelResultDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetricRelDTO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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
    private MetricService metricService;

    @Autowired
    private EntityMetricRelMapper entityMetricRelMapper;

    @Autowired
    private EntityService entityService;


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
    public Map<String, EntityMetricRelResultDAO> mapByAliasBatch(Long entityMetaId, Collection<String> aliasColl) {
        if (entityMetaId == null || aliasColl == null || aliasColl.isEmpty()) {
            log.warn("[x] querying by alias failed, invalid param: entityId={}, aliasList={}", entityMetaId, aliasColl);
            return MapUtil.empty();
        }

        List<EntityMetricRelResultDAO> resultList = entityMetricRelMapper.listByAliasBatch(entityMetaId, aliasColl);
        if (resultList == null) {
            log.warn("[x] querying by alias failed, result is null: entityId={}, aliasList={}", entityMetaId, aliasColl);
            return MapUtil.empty();
        }

        return resultList.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        EntityMetricRelResultDAO::getAlias,
                        dao -> dao,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public List<EntityMetricRelDAO> list() {
        return entityMetricRelMapper.selectList(null);
    }

    @Override
    public boolean create(EntityMetricRelDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[entity_metric_rel] creating failed, dto cannot be null");
        }

        // validate the entity existence
        EntityMeta entityMeta = entityService.getByCode(dto.getEntityCode());
        if (entityMeta == null) {
            throw new IllegalArgumentException("[entity_metric_rel] creating failed, entity meta not found: entityCode=" + dto.getEntityCode());
        }
        Long entityMetaId = entityMeta.getId();

        // validate the metric existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[entity_metric_rel] creating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();

        // validate the relation existence
        QueryWrapper<EntityMetricRelDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid", entityMetaId)
                .eq("mid", metricMetaId);
        EntityMetricRelDAO existingRel = entityMetricRelMapper.selectOne(queryWrapper);
        if (existingRel != null) {
            throw new IllegalStateException(String.format("[entity_metric_rel] creating failed, relation already exists: entityMetaId=%d, metricMetaId=%d",
                    entityMetaId, metricMetaId));
        }

        String alias = dto.getAlias();
        String dataUri = dto.getDataUri();

        // insert new relation
        EntityMetricRelDAO dao = new EntityMetricRelDAO();
        dao.setEntityMetaId(entityMetaId);
        dao.setMetricMetaId(metricMetaId);
        dao.setAlias(alias);
        dao.setDataUri(dataUri);

        int rows = entityMetricRelMapper.insert(dao);
        if (rows > 0) {
            if (log.isDebugEnabled()) {
                log.debug("[entity_metric_rel] create success, entityMetaId={}, metricMetaId={}, alias={}",
                        entityMetaId, metricMetaId, alias);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean update(EntityMetricRelDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[entity_metric_rel] updating failed, dto cannot be null");
        }

        // validate the entities existence
        EntityMeta entityMeta = entityService.getByCode(dto.getEntityCode());
        if (entityMeta == null) {
            throw new IllegalArgumentException("[entity_metric_rel] updating failed, entity meta not found: entityCode=" + dto.getEntityCode());
        }
        Long entityMetaId = entityMeta.getId();

        // validate the metrics existence
        MetricMetaDAO metricMetaDAO = metricService.getMetricMetaDaoByCode(dto.getMetricCode());
        if (metricMetaDAO == null) {
            throw new IllegalArgumentException("[entity_metric_rel] updating failed, metric meta not found: metricCode=" + dto.getMetricCode());
        }
        Long metricMetaId = metricMetaDAO.getId();

        // validate relationships existence
        QueryWrapper<EntityMetricRelDAO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("eid", entityMetaId)
                .eq("mid", metricMetaId);
        EntityMetricRelDAO existingRel = entityMetricRelMapper.selectOne(queryWrapper);
        if (existingRel == null) {
            throw new IllegalStateException(String.format("[entity_metric_rel] updating failed, relation not exists: entityMetaId=%d, metricMetaId=%d",
                    entityMetaId, metricMetaId));
        }

        String alias = dto.getAlias();
        String dataUri = dto.getDataUri();

        // insert new relation
        EntityMetricRelDAO dao = new EntityMetricRelDAO();
        dao.setEntityMetaId(entityMetaId);
        dao.setMetricMetaId(metricMetaId);
        dao.setAlias(alias);
        dao.setDataUri(dataUri);

        int rows = entityMetricRelMapper.updateByPrimaryKey(dao);
        if (rows > 0) {
            if (log.isDebugEnabled()) {
                log.debug("[entity_metric_rel] update success: entityMetaId={}, metricMetaId={}, alias={}",
                        entityMetaId, metricMetaId, alias);
            }
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

