package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.VersionedMetricDimensionQO;
import org.mapstruct.Mapper;

import java.util.List;


@Mapper(componentModel = "spring")
public interface MetricDimensionStructMap {

    /**
     * Convert MetricDimensionQO to MetricDimensionDTO.
     * @param qo metric dimension query object
     * @return metric dimension data transfer object
     */
    MetricDimensionDTO qoToDto(VersionedMetricDimensionQO qo);

    /**
     * Convert list of MetricDimensionQO to list of MetricDimensionDTO.
     * @param qoList list of metric dimension query objects
     * @return list of metric dimension data transfer objects
     */
    List<MetricDimensionDTO> qoToDtoBatch(List<VersionedMetricDimensionQO> qoList);
}
