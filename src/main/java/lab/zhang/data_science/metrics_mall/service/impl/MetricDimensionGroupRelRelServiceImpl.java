package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.common.OrderedList;
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionGroupRelMapper;
import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel;
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelResultDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO;
import lab.zhang.data_science.metrics_mall.service.MetricDimensionGroupRelService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricDimensionGroupRelStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * YGroup service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricDimensionGroupRelRelServiceImpl implements MetricDimensionGroupRelService {

    @Autowired
    private MetricDimensionGroupRelMapper metricDimensionGroupRelMapper;

    @Autowired
    private MetricDimensionGroupRelStructMapper metricDimensionGroupRelStructMapper;


    @Override
    public MetricDimensionGroupRel get(Long id) {
        if (id == null) {
            return null;
        }
        MetricDimensionGroupRelDAO dao = metricDimensionGroupRelMapper.selectById(id);
        return metricDimensionGroupRelStructMapper.daoToModel(dao);
    }

    @Override
    public List<MetricDimensionGroupRel> list() {
        List<MetricDimensionGroupRelDAO> daoList = metricDimensionGroupRelMapper.selectList(null);
        return metricDimensionGroupRelStructMapper.daoToModelBatch(daoList);
    }

    @Override
    public List<MetricDimensionGroupRel> listByMetricId(Long metricId) {
        if (metricId == null) {
            return ListUtil.empty();
        }
        LambdaQueryWrapper<MetricDimensionGroupRelDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDimensionGroupRelDAO::getMid, metricId);
        List<MetricDimensionGroupRelDAO> daoList = metricDimensionGroupRelMapper.selectList(queryWrapper);
        return metricDimensionGroupRelStructMapper.daoToModelBatch(daoList);
    }

    public List<MetricDimensionGroupRelResultDAO> listYGroupByMetricCodeBatch(Collection<String> metricCodeColl) {
        return metricDimensionGroupRelMapper.selectYGroupByMetricCodes(metricCodeColl);
    }

    @Override
    public Map<String, Set<OrderedList<String>>> mapDimensionIdsByMetricCodeBatch(Collection<String> metricCodeColl) {
        if (metricCodeColl == null) {
            return MapUtil.empty();
        }

        List<MetricDimensionGroupRelResultDAO> resultList = listYGroupByMetricCodeBatch(metricCodeColl);
        if (resultList == null) {
            return MapUtil.empty();
        }

        Map<String, Set<OrderedList<String>>> retMap = new HashMap<>();
        for (MetricDimensionGroupRelResultDAO resultDAO : resultList) {
            if (resultDAO == null) {
                continue;
            }
            String metricCode = resultDAO.getMetricCode();
            String dimensionIds = resultDAO.getDimensionIds();
            List<String> dimensionIdList = metricDimensionGroupRelStructMapper.explode(dimensionIds);
            if (dimensionIdList == null) {
                continue;
            }
            retMap.putIfAbsent(metricCode, new HashSet<>());
            retMap.get(metricCode).add(new OrderedList<>(dimensionIdList));
        }

        return retMap;
    }


    @Override
    public long count() {
        return metricDimensionGroupRelMapper.selectCount(null);
    }

    @Override
    public boolean insert(MetricDimensionGroupRelDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[y_group] creating failed, dto cannot be null");
        }
        if (dto.getMetricId() == null) {
            throw new IllegalArgumentException("[y_group] creating failed, mid cannot be null");
        }

        // query
        MetricDimensionGroupRelDAO dao = metricDimensionGroupRelStructMapper.dtoToDao(dto);
        int rows = metricDimensionGroupRelMapper.insert(dao);

        if (rows > 0) {
            dto.setId(dao.getId());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean update(MetricDimensionGroupRelDTO dto) {
        // validate input
        if (dto == null) {
            throw new IllegalArgumentException("[y_group] updating failed, dto cannot be null");
        }
        if (dto.getId() == null) {
            throw new IllegalArgumentException("[y_group] updating failed, id cannot be null");
        }

        // validate yGroup existence
        MetricDimensionGroupRelDAO existingDAO = metricDimensionGroupRelMapper.selectById(dto.getId());
        if (existingDAO == null) {
            throw new IllegalStateException("[y_group] updating failed, yGroup does not exist");
        }

        // query
        MetricDimensionGroupRelDAO dao = metricDimensionGroupRelStructMapper.dtoToDao(dto);
        int rows = metricDimensionGroupRelMapper.updateById(dao);

        return rows > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return metricDimensionGroupRelMapper.deleteById(id) > 0;
    }
}

