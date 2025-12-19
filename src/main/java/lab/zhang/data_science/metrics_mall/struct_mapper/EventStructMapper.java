package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dto.EventDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EventQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EventResponseVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting Event related objects.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface EventStructMapper {

    /**
     * Convert to EventResponseDTO.
     * This is a placeholder method, actual conversion should be done in service layer.
     *
     * @param accepted accepted count
     * @param rejected rejected count
     * @return EventResponseDTO
     */
    default EventResponseVO modelToVo(Integer accepted, Integer rejected) {
        return EventResponseVO.builder()
                .accepted(accepted)
                .rejected(rejected)
                .build();
    }


    @Mapping(target = "dimensionMap", source = "dim")
    EventDTO qoToDto(EventQO qo);

    List<EventDTO> qoToDtoBatch(List<EventQO> qoList);

}

