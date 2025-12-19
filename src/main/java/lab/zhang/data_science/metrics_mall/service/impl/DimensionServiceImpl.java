package lab.zhang.data_science.metrics_mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.DimensionMapper;
import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
import lab.zhang.data_science.metrics_mall.service.DimensionService;
import lab.zhang.data_science.metrics_mall.struct_mapper.DimensionStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Dimension service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DimensionServiceImpl implements DimensionService {

    private final DimensionMapper dimensionMapper;
    private final DimensionStructMapper dimensionStructMapper;

    @Override
    public Dimension get(Long id) {
        if (id == null) {
            return null;
        }
        DimensionDAO dao = dimensionMapper.selectById(id);
        return dimensionStructMapper.daoToModel(dao);
    }

    @Override
    public Dimension getByCode(String code) {
        if (code == null) {
            return null;
        }
        LambdaQueryWrapper<DimensionDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DimensionDAO::getCode, code);
        DimensionDAO dao = dimensionMapper.selectOne(queryWrapper);
        return dimensionStructMapper.daoToModel(dao);
    }

    @Override
    public List<Dimension> list() {
        List<DimensionDAO> daoList = dimensionMapper.selectList(null);
        return dimensionStructMapper.daoToModelBatch(daoList);
    }

    @Override
    public long count() {
        return dimensionMapper.selectCount(null);
    }

    @Override
    public boolean insert(Dimension dimension) {
        if (dimension == null) {
            return false;
        }
        DimensionDAO dao = dimensionStructMapper.modelToDao(dimension);
        dao.setTimeOnCreate();
        return dimensionMapper.insert(dao) > 0;
    }

    @Override
    public boolean update(Dimension dimension) {
        if (dimension == null || dimension.getId() == null) {
            return false;
        }
        DimensionDAO dao = dimensionStructMapper.modelToDao(dimension);
        dao.setTimeOnUpdate();
        return dimensionMapper.updateById(dao) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return dimensionMapper.deleteById(id) > 0;
    }
}

