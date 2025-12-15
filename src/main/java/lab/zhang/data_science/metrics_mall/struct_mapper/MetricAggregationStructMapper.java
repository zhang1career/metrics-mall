package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.MetricAggregationResult;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricAggregationDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricAggregationQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.AggregationRowVO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricAggregationVO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricMetaVO;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Mapper for converting QO to Model.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface MetricAggregationStructMapper {

    /**
     * Convert MetricAggregationQO to MetricAggregationQuery.
     *
     * @param qo metric aggregation QO
     * @return MetricAggregationQuery model
     */
    MetricAggregationDTO qoToDto(MetricAggregationQO qo);


    /**
     * Convert MetricAggregationResult to MetricAggregationDTO.
     *
     * @param result metric aggregation result
     * @return metric aggregation DTO
     */
    default MetricAggregationVO modelToVo(MetricAggregationResult result) {
        if (result == null) {
            return null;
        }

        // Convert meta
        Map<String, MetricMetaVO> metaMap = result.getMeta().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            MetricAggregationResult.MetricMeta meta = entry.getValue();
                            return MetricMetaVO.builder()
                                    .name(meta.getName())
                                    .unit(meta.getUnit())
                                    .precision(meta.getPrecision())
                                    .build();
                        }));

        // Convert rows
        List<AggregationRowVO> rowList = result.getRows().stream()
                .map(row -> AggregationRowVO.builder()
                        .bucketTime(row.getBucketTime())
                        .dims(row.getDimMap())
                        .metrics(row.getMetricMap())
                        .build())
                .collect(Collectors.toList());

        return MetricAggregationVO.builder()
                .meta(metaMap)
                .rows(rowList)
                .build();
    }
}

