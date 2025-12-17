package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricDAO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Metric mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface MetricMapper extends BaseMapper<MetricDAO> {

    /**
     * Query metrics by codes.
     *
     * @param codes metric codes list
     * @return metric list
     */
    List<MetricDAO> selectByCodes(List<String> codes);
}

