package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.OrderByDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.OrderByQO;
import org.mapstruct.Mapper;

/**
 * Mapper for converting QO to DTO.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface QueryOrderByStructMapper {

    OrderByDTO qoToDto(OrderByQO qo);
}

