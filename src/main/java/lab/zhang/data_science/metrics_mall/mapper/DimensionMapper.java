package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Dimension mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface DimensionMapper extends BaseMapper<DimensionDAO> {
}

