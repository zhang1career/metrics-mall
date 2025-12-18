package lab.zhang.data_science.metrics_mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricMetaDAO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Metric meta mapper interface.
 *
 * @author Rongjin Zhang
 */
@Mapper
public interface MetricMetaMapper extends BaseMapper<MetricMetaDAO> {

    /**
     * Query metrics by codes.
     *
     * @param codes metric codes list
     * @return metric meta list
     */
    List<MetricMetaDAO> selectByCodes(List<String> codes);
}

