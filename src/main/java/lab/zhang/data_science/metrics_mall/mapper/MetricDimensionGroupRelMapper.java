package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelResultDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * YGroup mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface MetricDimensionGroupRelMapper extends BaseMapper<MetricDimensionGroupRelDAO> {

    @Select("<script>" +
            "SELECT m.code AS metricCode, yg.dids AS dimensionIds " +
            "FROM y_group yg " +
            "JOIN metric_meta m ON m.id = yg.mid " +
            "WHERE m.code IN " +
            " <foreach item='item' collection='codes' open='(' separator=',' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            "</script>")
    List<MetricDimensionGroupRelResultDAO> selectGroupByMetricCodes(@Param("codes") Collection<String> metricCodeColl);
}

