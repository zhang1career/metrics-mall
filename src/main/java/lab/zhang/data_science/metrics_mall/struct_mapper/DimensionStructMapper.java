package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.DimensionDTO;
import lab.zhang.data_science.metrics_mall.pojo.qo.DimensionQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.DimensionVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Dimension structure mapper.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface DimensionStructMapper extends BaseStructMapper {

    /**
     * Convert Dimension QO to Dimension DTO.
     *
     * @param qo dimension query object
     * @return dimension data transfer object
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    DimensionDTO qoToDto(DimensionQO qo);

    /**
     * Convert Dimension DTO to Dimension DAO.
     *
     * @param dto dimension data transfer object
     * @return dimension data access object
     */
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(dto.getCreateTime()))")
    @Mapping(target = "ut", expression = "java(mapDateToTimestamp(dto.getUpdateTime()))")
    DimensionDAO dtoToDao(DimensionDTO dto);

    /**
     * Convert Dimension DAO to Dimension model.
     *
     * @param dao dimension DAO
     * @return dimension model
     */
    @Mapping(target = "createTime", expression = "java(mapTimestampToDate(dao.getCt()))")
    @Mapping(target = "updateTime", expression = "java(mapTimestampToDate(dao.getUt()))")
    Dimension daoToModel(DimensionDAO dao);

    /**
     * Convert Dimension list DAO to Dimension list model.
     *
     * @param daoList dimension DAO list
     * @return dimension model list
     */
    List<Dimension> daoToModelBatch(List<DimensionDAO> daoList);

    /**
     * Convert Dimension model to Dimension VO.
     *
     * @param model dimension model
     * @return dimension VO
     */
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(model.getCreateTime()))")
    @Mapping(target = "ut", expression = "java(mapDateToTimestamp(model.getUpdateTime()))")
    DimensionVO modelToVo(Dimension model);

    /**
     * Convert Dimension model list to Dimension VO list.
     *
     * @param modelList dimension model list
     * @return dimension VO list
     */
    List<DimensionVO> modelToVoBatch(List<Dimension> modelList);
}

