package lab.zhang.data_science.metrics_mall.struct_mapper;

import org.mapstruct.MapperConfig;

/**
 * Mapper for converting MetricAggregation related objects.
 *
 * @author Rongjin Zhang
 */
@MapperConfig(
        componentModel = "spring",
        uses = {
                MetricStructMapper.class
        })
public interface BaseAggregationStructMapper {
}

