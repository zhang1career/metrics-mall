package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.EchoMetricDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EchoMetricQO;
import org.mapstruct.Mapper;

import java.util.List;


/**
 * Mapper for converting MetricDimension related objects.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface MetricDimensionStructMap {

    /**
     * Convert MetricDimensionQO to MetricDimensionDTO.
     * @param qo metric dimension query object
     * @return metric dimension data transfer object
     */
    EchoMetricDTO qoToDto(EchoMetricQO qo);

    /**
     * Convert list of MetricDimensionQO to list of MetricDimensionDTO.
     * @param qoList list of metric dimension query objects
     * @return list of metric dimension data transfer objects
     */
    List<EchoMetricDTO> qoToDtoBatch(List<EchoMetricQO> qoList);
}
