package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.DimensionMapper;
import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.DimensionDTO;
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
        DimensionDAO dao = getDimensionDaoByCode(code);
        if (dao == null) {
            return null;
        }
        return dimensionStructMapper.daoToModel(dao);
    }

    private DimensionDAO getDimensionDaoByCode(String code) {
        if (code == null) {
            return null;
        }
        LambdaQueryWrapper<DimensionDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DimensionDAO::getCode, code);
        return dimensionMapper.selectOne(queryWrapper);
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
    public boolean insert(DimensionDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[dimension] create failed, dto cannot be null");
        }
        if (StrUtil.isBlank(dto.getCode())) {
            throw new IllegalArgumentException("[dimension] create failed, code cannot be blank");
        }

        // validate dimension existence
        DimensionDAO existingDAO = getDimensionDaoByCode(dto.getCode());
        if (existingDAO != null) {
            throw new IllegalArgumentException("[dimension] create failed, code already exists");
        }

        dto.setTimeOnCreate();

        // query
        DimensionDAO dao = dimensionStructMapper.dtoToDao(dto);
        int rows = dimensionMapper.insert(dao);

        if (rows > 0) {
            dto.setId(dao.getId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(DimensionDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[dimension] update failed, dto cannot be null");
        }
        if (StrUtil.isBlank(dto.getCode())) {
            throw new IllegalArgumentException("[dimension] update failed, code cannot be blank");
        }

        // validate dimension existence
        DimensionDAO existingDAO = getDimensionDaoByCode(dto.getCode());
        if (existingDAO == null) {
            throw new IllegalArgumentException("[dimension] update failed, dimension does not exist");
        }

        dto.setId(existingDAO.getId());
        dto.setTimeOnUpdate();

        // query
        DimensionDAO dao = dimensionStructMapper.dtoToDao(dto);
        int rows = dimensionMapper.updateById(dao);

        return rows > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return dimensionMapper.deleteById(id) > 0;
    }
}

