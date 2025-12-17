package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Entity meta mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface EntityMetaMapper extends BaseMapper<EntityMetaDAO> {
}

