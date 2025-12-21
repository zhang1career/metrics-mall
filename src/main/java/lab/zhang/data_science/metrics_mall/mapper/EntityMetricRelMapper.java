package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Entity and Metric relation mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface EntityMetricRelMapper extends BaseMapper<EntityMetricRelDAO> {

    int updateByPrimaryKey(@Param("rel") EntityMetricRelDAO rel);
}

