package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dao.x.EntityMetricRelResultDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Collection;
import java.util.List;

/**
 * Entity and Metric relation mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface EntityMetricRelMapper extends BaseMapper<EntityMetricRelDAO> {

    @Update("<script>" +
            "UPDATE x " +
            "<set>" +
            " <if test='rel.alias != null'>alias=#{rel.alias},</if>" +
            " <if test='rel.dataUri != null'>data_uri=#{rel.dataUri},</if>" +
            "</set>" +
            "WHERE eid=#{rel.entityMetaId} " +
            "AND mid=#{rel.metricMetaId}" +
            "</script>")
    int updateByPrimaryKey(@Param("rel") EntityMetricRelDAO rel);


    @Select("<script>" +
            "SELECT m.code AS metricCode, x.alias " +
            "FROM x " +
            "JOIN metric_meta m ON m.id=x.mid " +
            "WHERE x.eid=#{entityMetaId} " +
            "AND x.alias IN " +
            " <foreach item='item' collection='aliases' open='(' separator=',' close=')'>" +
            "  #{item}" +
            " </foreach>" +
            "</script>")
    List<EntityMetricRelResultDAO> listByAliasBatch(@Param("entityMetaId") Long entityMetaId,
                                                    @Param("aliases") Collection<String> aliasColl);
}
