package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.MetricVersion;
import lab.zhang.data_science.metrics_mall.pojo.dao.metric_version.MetricVersionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricVersionDTO;
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
public interface MetricVersionStructMapper extends BaseStructMapper {

    /**
     * Convert QO to DTO.
     *
     * @param qo metric version query object
     * @return metric version data transfer object
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "metricId", ignore = true)
    @Mapping(target = "lifeStatus", expression = "java(mapLifeStatus(qo.getLifeStatus()))")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    MetricVersionDTO qoToDto(MetricVersionQO qo);

    /**
     * Convert DTO to DAO.
     *
     * @param dto metric version data transfer object
     * @return metric version entity
     */
    @Mapping(target = "lifeStatus", expression = "java(mapLifeStatusToInt(dto.getLifeStatus()))")
    @Mapping(target = "onlineTs", ignore = true)
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(dto.getCreateTime()))")
    @Mapping(target = "ut", expression = "java(mapDateToTimestamp(dto.getUpdateTime()))")
    MetricVersionDAO dtoToDao(MetricVersionDTO dto);

    /**
     * Convert DAO to Model.
     *
     * @param dao metric version entity
     * @return metric version model
     */
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createTime", expression = "java(mapTimestampToDate(dao.getCt()))")
    @Mapping(target = "updateTime", expression = "java(mapTimestampToDate(dao.getUt()))")
    MetricVersion daoToModel(MetricVersionDAO dao);

    /**
     * Convert list of DAO to list of Model.
     *
     * @param daoList list of metric version entity
     * @return list of metric version model
     */
    List<MetricVersion> daoToModelBatch(List<MetricVersionDAO> daoList);

    /**
     * Convert Model to VO.
     *
     * @param model metric version model
     * @return metric version view object
     */
    @Mapping(target = "createTime", expression = "java(mapDateToString(model.getCreateTime()))")
    @Mapping(target = "updateTime", expression = "java(mapDateToString(model.getUpdateTime()))")
    MetricVersionVO modelToVo(MetricVersion model);

    /**
     * Convert list of Model to list of VO.
     *
     * @param modelList list of metric version model
     * @return list of metric version view object
     */
    List<MetricVersionVO> modelToVoBatch(List<MetricVersion> modelList);
}

