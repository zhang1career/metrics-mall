package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.Dimension;
import lab.zhang.data_science.metrics_mall.pojo.dao.DimensionDAO;
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
public interface DimensionStructMapper {

    /**
     * Convert DimensionQO to Dimension.
     *
     * @param qo dimension query object
     * @return dimension model
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Dimension qoToModel(DimensionQO qo);

    /**
     * Convert Dimension DAO to Dimension model.
     *
     * @param dao dimension DAO
     * @return dimension model
     */
    @Mapping(target = "createTime", expression = "java(dao.getCt() != null ? new java.util.Date(dao.getCt()) : null)")
    @Mapping(target = "updateTime", expression = "java(dao.getUt() != null ? new java.util.Date(dao.getUt()) : null)")
    Dimension daoToModel(DimensionDAO dao);

    /**
     * Convert Dimension list DAO to Dimension list model.
     *
     * @param daoList dimension DAO list
     * @return dimension model list
     */
    List<Dimension> daoToModelBatch(List<DimensionDAO> daoList);

    /**
     * Convert Dimension model to Dimension DAO.
     *
     * @param model dimension model
     * @return dimension DAO
     */
    @Mapping(target = "ct", expression = "java(model.getCreateTime() != null ? model.getCreateTime().getTime() : null)")
    @Mapping(target = "ut", expression = "java(model.getUpdateTime() != null ? model.getUpdateTime().getTime() : null)")
    DimensionDAO modelToDao(Dimension model);

    /**
     * Convert Dimension model to Dimension VO.
     *
     * @param model dimension model
     * @return dimension VO
     */
    @Mapping(target = "ct", expression = "java(model.getCreateTimeInTimestamp())")
    @Mapping(target = "ut", expression = "java(model.getUpdateTimeInTimestamp())")
    DimensionVO modelToVo(Dimension model);

    /**
     * Convert Dimension model list to Dimension VO list.
     *
     * @param modelList dimension model list
     * @return dimension VO list
     */
    List<DimensionVO> modelToVoBatch(List<Dimension> modelList);
}

