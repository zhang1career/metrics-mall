package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.FieldConditionDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.FieldConditionQO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Mapper for converting QO to DTO.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface QueryFieldConditionStructMapper {


    FieldConditionDTO qoToDto(FieldConditionQO qo);


    List<FieldConditionDTO> qoToDtoBatch(List<FieldConditionQO> qoList);

}

