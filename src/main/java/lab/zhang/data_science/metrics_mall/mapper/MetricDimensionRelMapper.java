package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDimensionRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionRelDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * Metric and Dimension relation mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface MetricDimensionRelMapper extends BaseMapper<MetricDimensionRelDAO> {

    /**
     * Get is_hot by metric code.
     *
     * @param metricCode metric code
     * @param dimCodeColl dimension codes
     * @return is_hot value
     */
    @Select("<script>" +
            "SELECT m.code AS metricCode , y.is_hot AS isHot, d.code AS dimensionCode " +
            "FROM metric_meta m " +
            "JOIN y ON m.id = y.mid " +
            "JOIN dim d ON d.id = y.did " +
            "WHERE m.code=#{metricCode} " +
            "AND d.code IN " +
            " <foreach item='item' collection='dimCodes' open='(' separator=',' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            "</script>")
    List<MetricDimensionRelDTO> getIsHotBatch(@Param("metricCode") String metricCode, @Param("dimCodes") Collection<String> dimCodeColl);
}
