package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.TimeRangeDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.TimeRangeQO;
import org.mapstruct.Mapper;

/**
 * Mapper for converting QO to DTO.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface QueryTimeRangeStructMapper {

    TimeRangeDTO qoToDto(TimeRangeQO qo);

}

