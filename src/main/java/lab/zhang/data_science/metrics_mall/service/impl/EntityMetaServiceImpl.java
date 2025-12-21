package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.EntityMetaMapper;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetaDTO;
import lab.zhang.data_science.metrics_mall.service.EntityMetaService;
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetaStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Entity meta service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntityMetaServiceImpl implements EntityMetaService {

    private final EntityMetaMapper entityMetaMapper;
    private final EntityMetaStructMapper entityMetaStructMapper;

    @Override
    public EntityMeta get(Integer id) {
        if (id == null) {
            return null;
        }
        EntityMetaDAO dao = entityMetaMapper.selectById(id);
        return entityMetaStructMapper.daoToModelNew(dao);
    }

    @Override
    public EntityMeta getByCode(String code) {
        if (code == null) {
            return null;
        }
        LambdaQueryWrapper<EntityMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EntityMetaDAO::getCode, code);
        EntityMetaDAO dao = entityMetaMapper.selectOne(queryWrapper);
        return entityMetaStructMapper.daoToModelNew(dao);
    }

    @Override
    public List<EntityMeta> list() {
        List<EntityMetaDAO> daoList = entityMetaMapper.selectList(null);
        return entityMetaStructMapper.daoToModelBatch(daoList);
    }

    @Override
    public long count() {
        return entityMetaMapper.selectCount(null);
    }

    @Override
    public boolean insert(EntityMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[entity_meta] create failed, dto is null");
        }

        EntityMeta existingModel = getByCode(dto.getCode());
        if (existingModel != null) {
            throw new IllegalStateException("[entity_meta] create failed, existed model is existed");
        }

        dto.setTimeOnCreate();
        EntityMetaDAO dao = entityMetaStructMapper.dtoToDao(dto);

        int rows = entityMetaMapper.insert(dao);
        if (rows > 0) {
            dto.setId(dao.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean update(EntityMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[entity_meta] update failed, dto is null");
        }

        EntityMeta existingModel = getByCode(dto.getCode());
        if (existingModel == null) {
            throw new IllegalStateException("[entity_meta] update failed, existed model is not existed");
        }

        dto.setId(existingModel.getId());
        dto.setTimeOnUpdate();
        EntityMetaDAO dao = entityMetaStructMapper.dtoToDao(dto);

        return entityMetaMapper.updateById(dao) > 0;
    }

    @Override
    public boolean delete(Integer id) {
        if (id == null) {
            return false;
        }
        return entityMetaMapper.deleteById(id) > 0;
    }
}

