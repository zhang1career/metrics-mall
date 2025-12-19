package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.OpLogDAO;
import org.apache.ibatis.annotations.Mapper;

/**
 * Operation log mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface OpLogMapper extends BaseMapper<OpLogDAO> {
}

