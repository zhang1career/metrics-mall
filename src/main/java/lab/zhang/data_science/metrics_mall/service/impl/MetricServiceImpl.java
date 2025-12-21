package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricDimensionRelMapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricMetaMapper;
import lab.zhang.data_science.metrics_mall.mapper.MetricVersionMapper;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricMetaDTO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static lab.zhang.data_science.metrics_mall.constant.NumConst.ONE;
import static lab.zhang.data_science.metrics_mall.constant.NumConst.ZERO;

/**
 * Metric service implementation.
 *
 * @author Rongjin Zhang
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricServiceImpl implements MetricService {

    @Autowired
    private MetricMetaMapper metricMapper;

    @Autowired
    private MetricDimensionRelMapper metricDimensionRelMapper;

    @Autowired
    private MetricVersionMapper metricVersionMapper;

    @Autowired
    private MetricStructMapper metricStructMapper;

    @Override
    public PrimeMetric getPrimeMetricByCode(String code) {
        MetricMetaDAO metricMetaDAO = getMetricMetaDaoByCode(code);
        if (metricMetaDAO == null) {
            return null;
        }
        // todo: should not convert only MetricMetaDAO to PrimeMetric
        return metricStructMapper.daoToPrimeModel(metricMetaDAO);
    }

    @Override
    public MetricMetaDAO getMetricMetaDaoByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }

        LambdaQueryWrapper<MetricMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricMetaDAO::getCode, code);
        return metricMapper.selectOne(queryWrapper);
    }

    @Override
    public Map<String, PrimeMetric> getPrimeMetricByCodeBatch(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return Collections.emptyMap();
        }

        List<MetricMetaDAO> metricDAOList = metricMapper.selectByCodes(codes);
        if (CollectionUtils.isEmpty(metricDAOList)) {
            return Collections.emptyMap();
        }

        List<PrimeMetric> metricList = metricStructMapper.daoToPrimeModelBatch(metricDAOList);
        return metricList.stream()
                .collect(Collectors.toMap(PrimeMetric::getCode, metric -> metric));
    }

    @Override
    public Pair<Boolean, String> validateCode(String code) {
        if (StrUtil.isBlank(code)) {
            return Pair.of(false, "[valid] metric code is blank");
        }

        LambdaQueryWrapper<MetricMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricMetaDAO::getCode, code);
        MetricMetaDAO metricMetaDAO = metricMapper.selectOne(queryWrapper);
        if (metricMetaDAO == null) {
            return Pair.of(false, "metric not found: code=" + code);
        }
        return Pair.of(true, StrUtil.EMPTY);
    }

    @Override
    public boolean validateCodesExist(List<String> codes) {
        if (CollectionUtils.isEmpty(codes)) {
            return false;
        }

        Map<String, PrimeMetric> metricMap = getPrimeMetricByCodeBatch(codes);
        return metricMap.size() == codes.size();
    }

    @Override
    public MetricAggregation queryAggregation(MetricAggregationDTO queryModel) {
        // This method is delegated to MetricAggregationService
        // Keeping it for backward compatibility
        log.warn("queryAggregation is deprecated, use MetricAggregationService instead");
        // Return empty result to avoid null
        return new MetricAggregation(Collections.emptyMap(), Collections.emptyMap());
    }

    @Override
    public Map<String, Boolean> checkHotBatch(String metricCode, List<String> dimensionCodeList) {
        if (log.isDebugEnabled()) {
            log.debug("[metric] check hot, param: metricCode={}, dimCodes={}", metricCode, dimensionCodeList);
        }

        if (StrUtil.isBlank(metricCode) || CollectionUtils.isEmpty(dimensionCodeList)) {
            log.warn("[metric] check hot, invalid param: metricCode={}, dimCodes={}", metricCode, dimensionCodeList);
            return MapUtil.empty();
        }
        Set<String> dimensionCodeSet = new HashSet<>(dimensionCodeList);
        List<MetricDimensionRelDTO> list = metricDimensionRelMapper.getIsHotBatch(metricCode, dimensionCodeSet);
        if (CollectionUtils.isEmpty(list)) {
            log.warn("[metric] check hot, no data found: metricCode={}, dimCodes={}", metricCode, dimensionCodeList);
            return MapUtil.empty();
        }
        return list.stream()
                .filter(dto -> dto != null && !StrUtil.isBlank(dto.getDimensionCode()))
                .collect(Collectors.toMap(MetricDimensionRelDTO::getDimensionCode,
                        dto -> dto.getIsHot() != null && dto.getIsHot().equals(ONE)));
    }

    @Override
    public Map<String, Integer> chooseVersionBatch(List<String> metricCodeList, Map<String, Integer> requiredVersionMap) {
        if (log.isDebugEnabled()) {
            log.debug("[metric] choose version, param: metricCodes={}, requiredVersions={}", metricCodeList, requiredVersionMap);
        }
        if (CollectionUtils.isEmpty(metricCodeList)) {
            throw new IllegalArgumentException("[metric] choose version, metricCodeList cannot be empty");
        }

        // unique metric codes
        Set<String> metricCodeSet = new HashSet<>(metricCodeList);

        // clone the required version map
        Map<String, Integer> tempVersionMap = requiredVersionMap != null
                ? ObjectUtil.cloneByStream(requiredVersionMap)
                : MapUtil.empty();
        // supplement with default versions
        for (String _metricCode : metricCodeList) {
            if (tempVersionMap.containsKey(_metricCode)) {
                tempVersionMap.putIfAbsent(_metricCode, ZERO);
                continue;
            }
            tempVersionMap.put(_metricCode, ZERO);
        }

        // query available versions
        List<MetricVersionDTO> availableList = metricVersionMapper.getMetricVersionBatch(metricCodeSet);
        if (CollectionUtils.isEmpty(availableList)) {
            log.warn("[metric] choose version, no data found: metricCodes={}", metricCodeList);
            return MapUtil.empty();
        }

        Map<String, Integer> retMap = new HashMap<>();
        // filter availableList by tempVersionMap
        for (MetricVersionDTO available : availableList) {
            String code = available.getMetricCode();
            // already found
            if (retMap.containsKey(code)) {
                continue;
            }
            Integer requiredVersion = tempVersionMap.get(code);
            if (requiredVersion == null) {
                throw new IllegalStateException("[metric] choose version, required version never should be null, metricCode=" + code);
            }
            // default version
            if (requiredVersion.equals(ZERO)) {
                // choose main version
                if (available.getIsMain() != null && available.getIsMain().equals(ONE)) {
                    retMap.put(code, available.getVersion());
                }
                continue;
            }
            // specified version - exact match only
            if (requiredVersion.equals(available.getVersion())) {
                retMap.put(code, available.getVersion());
            }
        }

        return retMap;
    }

    @Override
    public PrimeMetric get(Long id) {
        if (id == null) {
            return null;
        }
        MetricMetaDAO dao = metricMapper.selectById(id);
        if (dao == null) {
            return null;
        }
        return metricStructMapper.daoToPrimeModel(dao);
    }

    @Override
    public List<PrimeMetric> list(MetricMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_meta] create failed, dto is null");
        }

        MetricMetaDAO dao = metricStructMapper.dtoToDao(dto);

        LambdaQueryWrapper<MetricMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        if (dao.getCode() != null) {
            queryWrapper.eq(MetricMetaDAO::getCode, dao.getCode());
        }
        if (dao.getName() != null) {
            queryWrapper.like(MetricMetaDAO::getName, dao.getName());
        }
        if (dao.getMetricType() != null) {
            queryWrapper.eq(MetricMetaDAO::getMetricType, dao.getMetricType());
        }
        if (dao.getAggregationType() != null) {
            queryWrapper.eq(MetricMetaDAO::getAggregationType, dao.getAggregationType());
        }
        List<MetricMetaDAO> daoList = metricMapper.selectList(queryWrapper);
        return metricStructMapper.daoToPrimeModelBatch(daoList);
    }

    @Override
    public boolean insert(MetricMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_meta] create failed, dto is null");
        }
        MetricMetaDAO exitedDAO = getMetricMetaDaoByCode(dto.getCode());
        if (exitedDAO != null) {
            throw new IllegalArgumentException("[metric_meta] create failed, metric code already exists: " + dto.getCode());
        }

        dto.setTimeOnCreate();
        MetricMetaDAO dao = metricStructMapper.dtoToDao(dto);

        int rows = metricMapper.insert(dao);
        if (rows > 0) {
            dto.setId(dao.getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean update(MetricMetaDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("[metric_meta] update failed, dto is null");
        }
        MetricMetaDAO exitedDAO = getMetricMetaDaoByCode(dto.getCode());
        if (exitedDAO == null) {
            throw new IllegalArgumentException("[metric_meta] update failed, metric code not exists: " + dto.getCode());
        }

        dto.setId(exitedDAO.getId());
        dto.setTimeOnUpdate();
        MetricMetaDAO dao = metricStructMapper.dtoToDao(dto);

        return metricMapper.updateById(dao) > 0;
    }

    @Override
    public boolean delete(Long id) {
        if (id == null) {
            return false;
        }
        return metricMapper.deleteById(id) > 0;
    }
}

