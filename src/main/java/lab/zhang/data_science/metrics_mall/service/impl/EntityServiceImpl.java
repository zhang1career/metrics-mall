package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper;
import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.Entity.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.service.EntityService;
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class EntityServiceImpl implements EntityService {

    @Autowired
    private EntityMetaMapper entityMetaMapper;

    @Autowired
    private EntityMetaStructMapper entityMetaStructMapper;


    @Override
    public Entity getEntity(String entityCode, Long entityId) {
        // query
        LambdaQueryWrapper<EntityMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetaDAO::getCode, entityCode);
        EntityMetaDAO entityMetaDAO = entityMetaMapper.selectOne(queryWrapper);
        if (entityMetaDAO == null) {
            if (log.isDebugEnabled()) {
                log.debug("[entity] entity meta not found: entityCode={}", entityCode);
            }
            return null;
        }

        EntityMeta entityMeta = entityMetaStructMapper.daoToModel(entityMetaDAO);
        return Entity.builder()
                .meta(entityMeta)
                .id(entityId)
                .build();
    }
}
