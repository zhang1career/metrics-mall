package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetaQO;
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetaVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting EntityMeta related objects.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface EntityMetaStructMapper {

    /**
     * Convert EntityMetaDAO to Entity.EntityMeta (for backward compatibility).
     *
     * @param dao entity meta dao
     * @return Entity.EntityMeta
     */
    Entity.EntityMeta daoToModel(EntityMetaDAO dao);

    /**
     * Convert EntityMetaQO to EntityMetaModel.
     *
     * @param qo entity meta query object
     * @return entity meta model
     */
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    EntityMeta qoToModel(EntityMetaQO qo);

    /**
     * Convert EntityMetaDAO to EntityMetaModel.
     *
     * @param dao entity meta DAO
     * @return entity meta model
     */
    @Mapping(target = "createTime", expression = "java(dao.getCt() != null ? new java.util.Date(dao.getCt()) : null)")
    @Mapping(target = "updateTime", expression = "java(dao.getUt() != null ? new java.util.Date(dao.getUt()) : null)")
    EntityMeta daoToModelNew(EntityMetaDAO dao);

    /**
     * Convert EntityMetaDAO list to EntityMetaModel list.
     *
     * @param daoList entity meta DAO list
     * @return entity meta model list
     */
    List<EntityMeta> daoToModelBatch(List<EntityMetaDAO> daoList);

    /**
     * Convert EntityMetaModel to EntityMetaDAO.
     *
     * @param model entity meta model
     * @return entity meta DAO
     */
    @Mapping(target = "ct", expression = "java(model.getCreateTime() != null ? model.getCreateTime().getTime() : null)")
    @Mapping(target = "ut", expression = "java(model.getUpdateTime() != null ? model.getUpdateTime().getTime() : null)")
    EntityMetaDAO modelToDao(EntityMeta model);

    /**
     * Convert EntityMetaModel to EntityMetaVO.
     *
     * @param model entity meta model
     * @return entity meta VO
     */
    @Mapping(target = "ct", expression = "java(model.getCreateTimeInTimestamp())")
    @Mapping(target = "ut", expression = "java(model.getUpdateTimeInTimestamp())")
    EntityMetaVO modelToVo(EntityMeta model);

    /**
     * Convert EntityMetaModel list to EntityMetaVO list.
     *
     * @param modelList entity meta model list
     * @return entity meta VO list
     */
    List<EntityMetaVO> modelToVoBatch(List<EntityMeta> modelList);
}

