package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetricRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetricRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetricRelVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Entity Metric Relation structure mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface EntityMetricRelStructMapper {

    /**
     * Convert EntityMetricRelQO to EntityMetricRelDTO.
     *
     * @param qo entity metric relation QO
     * @return entity metric relation DTO
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    EntityMetricRelDTO qoToDto(EntityMetricRelQO qo);


    /**
     * Convert EntityMetricRelDAO to EntityMetricRelVO.
     *
     * @param dao entity metric relation DAO
     * @return entity metric relation VO
     */
    EntityMetricRelVO daoToVo(EntityMetricRelDAO dao);

    /**
     * Convert EntityMetricRelDAO list to EntityMetricRelVO list.
     *
     * @param daoList entity metric relation DAO list
     * @return entity metric relation VO list
     */
    List<EntityMetricRelVO> daoToVoBatch(List<EntityMetricRelDAO> daoList);
}

