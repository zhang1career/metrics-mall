package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.MetricSnapshot;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricSnapshotDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricSnapshotQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricSnapshotVO;
import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;


@MapperConfig(
        componentModel = "spring",
        uses = {
                MetricStructMapper.class,
        })
public interface MetricSnapshotStructMap {

    /**
     * Convert MetricSnapshotQO to MetricSnapshotDTO.
     * @param qo metric snapshot query object
     * @return metric snapshot data transfer object
     */
    @Mapping(target = "entityCode", source = "ec")
    @Mapping(target = "entityId", source = "eid")
    @Mapping(target = "metricList", source = "metrics")
    @Mapping(target = "isAtomic", expression = "java(qo.getIsAtomic() != null ? qo.getIsAtomic().equals(1) : false)")
    MetricSnapshotDTO qoToDto(MetricSnapshotQO qo);


    /**
     * Convert MetricSnapshot to MetricSnapshotVO.
     *
     * @param model metric snapshot result model
     * @return metric snapshot response VO
     */
    @Mapping(target = "entityCode", expression = "java(model.getEntity().getMeta().getCode())")
    @Mapping(target = "entityId", expression = "java(model.getEntity().getId())")
    @Mapping(target = "valueMap", expression = "java(metricStructMapper.modelToSampleValue(model.getMetricList(), model.getSnapshotDateTime()))")
    @Mapping(target = "sampleTimeMap", expression = "java(metricStructMapper.modelToSampleTime(model.getMetricList(), model.getSnapshotDateTime()))")
    MetricSnapshotVO modelToVo(MetricSnapshot model);
}
