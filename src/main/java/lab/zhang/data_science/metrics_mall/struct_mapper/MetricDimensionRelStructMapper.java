package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionRelVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Metric Dimension Relation structure mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface MetricDimensionRelStructMapper {

    /**
     * Convert MetricDimensionRelDAO to MetricDimensionRelVO.
     *
     * @param dao metric dimension relation DAO
     * @return metric dimension relation VO
     */
    MetricDimensionRelVO daoToVo(MetricDimensionRelDAO dao);

    /**
     * Convert MetricDimensionRelDAO list to MetricDimensionRelVO list.
     *
     * @param daoList metric dimension relation DAO list
     * @return metric dimension relation VO list
     */
    List<MetricDimensionRelVO> daoToVoBatch(List<MetricDimensionRelDAO> daoList);
}

