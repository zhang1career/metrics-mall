package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetricRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetricRelVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Entity Metric Relation structure mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface EntityMetricRelStructMapper {

    /**
     * Convert EntityMetricRelQO to EntityMetricRelDAO.
     *
     * @param qo entity metric relation query object
     * @return entity metric relation DAO
     */
    EntityMetricRelDAO qoToDao(EntityMetricRelQO qo);

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

