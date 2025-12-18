package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.map.MapUtil;
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
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

import static lab.zhang.data_science.metrics_mall.constant.NumConst.ONE;

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
        if (StrUtil.isBlank(code)) {
            return null;
        }

        LambdaQueryWrapper<MetricMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricMetaDAO::getCode, code);
        MetricMetaDAO metricDAO = metricMapper.selectOne(queryWrapper);

        if (metricDAO == null) {
            return null;
        }

        return metricStructMapper.daoToPrimeModel(metricDAO);
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
    public MetricMetaDAO getMetricDaoByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }

        LambdaQueryWrapper<MetricMetaDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricMetaDAO::getCode, code);
        return metricMapper.selectOne(queryWrapper);
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
    public Map<String, PrimeMetric> getMetricModelsByCodes(List<String> metricCodes) {
        return getPrimeMetricByCodeBatch(metricCodes);
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
    public Map<String, Integer> getNearestVersionBatch(List<String> metricCodeList, Map<String, Integer> requiredVersionMap) {
        if (log.isDebugEnabled()) {
            log.debug("[metric] get version, param: metricCodes={}, requiredVersions={}", metricCodeList, requiredVersionMap);
        }
        if (CollectionUtils.isEmpty(metricCodeList)) {
            throw new IllegalArgumentException("[metric] nearest version, metricCodeList cannot be empty");
        }
        if (requiredVersionMap == null) {
            throw new IllegalArgumentException("[metric] nearest version, requiredVersionMap should not be null");
        }
        Set<String> metricCodeSet = new HashSet<>(metricCodeList);
        List<MetricVersionDTO> availableList = metricVersionMapper.getMetricVersionBatch(metricCodeSet);
        if (CollectionUtils.isEmpty(availableList)) {
            log.warn("[metric] get version, no data found: metricCodes={}", metricCodeList);
            return MapUtil.empty();
        }
        Map<String, List<MetricVersionDTO>> availableListOfMetric = availableList.stream()
                .collect(Collectors.groupingBy(MetricVersionDTO::getMetricCode));

        Map<String, Integer> retMap = new HashMap<>();
        for (Map.Entry<String, List<MetricVersionDTO>> entry : availableListOfMetric.entrySet()) {
            String code = entry.getKey();
            List<MetricVersionDTO> _availableList = entry.getValue();
            Integer requiredVersion = requiredVersionMap.get(code);
            // choose main version if no version required
            if (requiredVersion == null) {
                Integer mainVersion = getMainVersion(_availableList);
                if (mainVersion == null) {
                    throw new IllegalStateException("[metric] get version, no main version found, metricCode=" + code);
                }
                retMap.put(code, mainVersion);
                continue;
            }
            // choose nearest version
            List<Integer> avList = _availableList.stream()
                    .map(MetricVersionDTO::getVersion)
                    .collect(Collectors.toList());
            Integer nearestVersion = doGetNearestVersion(avList, requiredVersion);
            if (nearestVersion == null) {
                log.warn("[metric] get version, no version near before the required one, metricCode=" + code + ", requiredVersion=" + requiredVersion);
                continue;
            }
            retMap.put(code, nearestVersion);
        }

        return retMap;
    }

    private Integer getMainVersion(List<MetricVersionDTO> vList) {
        return vList.stream()
                .filter(v -> v.getIsMain() != null && v.getIsMain().equals(ONE))
                .map(MetricVersionDTO::getVersion)
                .findFirst()
                .orElse(null);
    }

    private Integer doGetNearestVersion(List<Integer> availableVersionList, Integer requiredVersion) {
        return availableVersionList.stream()
                .filter(av -> av <= requiredVersion)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}

