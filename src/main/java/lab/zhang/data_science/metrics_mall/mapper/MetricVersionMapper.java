package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/**
 * Metric version mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface MetricVersionMapper extends BaseMapper<MetricVersionDAO> {

    /**
     * Get versions by metric codes.
     *
     * @param metricCodeColl metric codes
     * @return list of maps containing code, version, is_main
     */
    @Select("<script>" +
            "SELECT m.code AS metricCode, v.version, v.is_main AS isMain" +
            "FROM metric_meta m " +
            "JOIN metric_version v ON m.id = v.mid " +
            "WHERE m.code IN " +
            " <foreach item='item' collection='metricCodes' open='(' separator=',' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            "</script>")
    List<MetricVersionDTO> getMetricVersionBatch(@Param("metricCodes") Collection<String> metricCodeColl);
}

