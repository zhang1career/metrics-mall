package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricVersionQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricVersionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Metric version structure mapper.
 *
 * @author Rongjin Zhang
 * @date 2025-12-19
 */
@Mapper(componentModel = "spring")
public interface MetricVersionStructMapper {

    /**
     * Convert QO to Model.
     *
     * @param qo metric version query object
     * @return metric version model
     */
    MetricVersion qoToModel(MetricVersionQO qo);

    /**
     * Convert DAO to Model.
     *
     * @param dao metric version entity
     * @return metric version model
     */
    @Mapping(target = "createTime", expression = "java(dao.getCt() != null ? new java.util.Date(dao.getCt()) : null)")
    @Mapping(target = "updateTime", expression = "java(dao.getUt() != null ? new java.util.Date(dao.getUt()) : null)")
    MetricVersion daoToModel(MetricVersionDAO dao);

    /**
     * Convert list of DAO to list of Model.
     *
     * @param daoList list of metric version entity
     * @return list of metric version model
     */
    List<MetricVersion> daoToModelBatch(List<MetricVersionDAO> daoList);

    /**
     * Convert Model to DAO.
     *
     * @param model metric version model
     * @return metric version entity
     */
    @Mapping(target = "ct", expression = "java(model.getCreateTime() != null ? model.getCreateTime().getTime() : null)")
    @Mapping(target = "ut", expression = "java(model.getUpdateTime() != null ? model.getUpdateTime().getTime() : null)")
    MetricVersionDAO modelToDao(MetricVersion model);

    /**
     * Convert Model to VO.
     *
     * @param model metric version model
     * @return metric version view object
     */
    MetricVersionVO modelToVo(MetricVersion model);

    /**
     * Convert list of Model to list of VO.
     *
     * @param modelList list of metric version model
     * @return list of metric version view object
     */
    List<MetricVersionVO> modelToVoBatch(List<MetricVersion> modelList);
}

