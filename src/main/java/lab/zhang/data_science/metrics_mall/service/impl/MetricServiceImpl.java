package lab.zhang.data_science.metrics_mall.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lab.zhang.data_science.metrics_mall.model.MetricAggregation;
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import lab.zhang.data_science.metrics_mall.mapper.MetricMapper;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.service.MetricService;
import lab.zhang.data_science.metrics_mall.struct_mapper.MetricStructMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Metric service implementation.
 *
 * @author Rongjin Zhang
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MetricMapper metricMapper;
    private final MetricStructMapper metricStructMapper;


    @Override
    public PrimeMetric getPrimeMetricByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }

        LambdaQueryWrapper<MetricDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDAO::getCode, code);
        MetricDAO metricDAO = metricMapper.selectOne(queryWrapper);

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

        List<MetricDAO> metricDAOList = metricMapper.selectByCodes(codes);
        if (CollectionUtils.isEmpty(metricDAOList)) {
            return Collections.emptyMap();
        }

        List<PrimeMetric> metricList = metricStructMapper.daoToPrimeModelBatch(metricDAOList);
        return metricList.stream()
                .collect(Collectors.toMap(PrimeMetric::getCode, metric -> metric));
    }

    @Override
    public MetricDAO getMetricDaoByCode(String code) {
        if (StrUtil.isBlank(code)) {
            return null;
        }

        LambdaQueryWrapper<MetricDAO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MetricDAO::getCode, code);
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
}

