package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.MetricDimensionGroupRel;
import lab.zhang.data_science.metrics_mall.pojo.dao.y_group.MetricDimensionGroupRelDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.MetricDimensionGroupRelDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.MetricDimensionGroupRelQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.MetricDimensionGroupRelVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * YGroup structure mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface MetricDimensionGroupRelStructMapper extends BaseStructMapper {

    /**
     * Convert YGroup QO to YGroup DTO.
     *
     * @param qo yGroup query object
     * @return yGroup data transfer object
     */
    @Mapping(target = "dimensionIdList", expression = "java(explode(qo.getDimensionIds()))")
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    MetricDimensionGroupRelDTO qoToDto(MetricDimensionGroupRelQO qo);

    /**
     * Convert YGroup DTO to YGroup DAO.
     *
     * @param dto yGroup data transfer object
     * @return yGroup data access object
     */
    @Mapping(target = "mid", source = "metricId")
    @Mapping(target = "dids", expression = "java(implode(dto.getDimensionIdList()))")
    @Mapping(target = "ct", ignore = true)
    MetricDimensionGroupRelDAO dtoToDao(MetricDimensionGroupRelDTO dto);

    /**
     * Convert YGroup DAO to YGroup model.
     *
     * @param dao yGroup DAO
     * @return yGroup model
     */
    @Mapping(target = "metricId", source = "mid")
    @Mapping(target = "dimensionIdList", expression = "java(explode(dao.getDids()))")
    @Mapping(target = "createTime", expression = "java(mapTimestampToDate(dao.getCt()))")
    @Mapping(target = "updateTime", ignore = true)
    MetricDimensionGroupRel daoToModel(MetricDimensionGroupRelDAO dao);

    /**
     * Convert YGroup list DAO to YGroup list model.
     *
     * @param daoList yGroup DAO list
     * @return yGroup model list
     */
    List<MetricDimensionGroupRel> daoToModelBatch(List<MetricDimensionGroupRelDAO> daoList);

    /**
     * Convert YGroup model to YGroup VO.
     *
     * @param model yGroup model
     * @return yGroup VO
     */
    @Mapping(target = "dimensionIds", expression = "java(implode(model.getDimensionIdList()))")
    @Mapping(target = "createTime", expression = "java(mapDateToString(model.getCreateTime()))")
    @Mapping(target = "updateTime", ignore = true)
    MetricDimensionGroupRelVO modelToVo(MetricDimensionGroupRel model);

    /**
     * Convert YGroup model list to YGroup VO list.
     *
     * @param modelList yGroup model list
     * @return yGroup VO list
     */
    List<MetricDimensionGroupRelVO> modelToVoBatch(List<MetricDimensionGroupRel> modelList);
}

