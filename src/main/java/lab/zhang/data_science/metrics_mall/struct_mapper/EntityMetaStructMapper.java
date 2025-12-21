package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.Entity;
import lab.zhang.data_science.metrics_mall.model.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import lab.zhang.data_science.metrics_mall.pojo.dto.EntityMetaDTO;
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
public interface EntityMetaStructMapper extends BaseStructMapper {

    /**
     * Convert EntityMetaQO to EntityMetaDTO.
     *
     * @param qo entity meta query object
     * @return entity meta data transfer object
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    EntityMetaDTO qoToDto(EntityMetaQO qo);

    /**
     * Convert EntityMetaDAO to Entity.EntityMeta (for backward compatibility).
     *
     * @param dao entity meta dao
     * @return Entity.EntityMeta
     */
    Entity.EntityMeta daoToModel(EntityMetaDAO dao);

    /**
     * Convert EntityMetaDAO to EntityMetaModel.
     *
     * @param dao entity meta DAO
     * @return entity meta model
     */
    @Mapping(target = "createTime", expression = "java(mapTimestampToDate(dao.getCt()))")
    @Mapping(target = "updateTime", expression = "java(mapTimestampToDate(dao.getUt()))")
    EntityMeta daoToModelNew(EntityMetaDAO dao);

    /**
     * Convert EntityMetaDAO list to EntityMetaModel list.
     *
     * @param daoList entity meta DAO list
     * @return entity meta model list
     */
    List<EntityMeta> daoToModelBatch(List<EntityMetaDAO> daoList);

    /**
     * Convert EntityMetaDTO to EntityMetaDAO.
     *
     * @param dto entity meta data transfer object
     * @return entity meta DAO
     */
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(dto.getCreateTime()))")
    @Mapping(target = "ut", expression = "java(mapDateToTimestamp(dto.getUpdateTime()))")
    EntityMetaDAO dtoToDao(EntityMetaDTO dto);

    /**
     * Convert EntityMetaModel to EntityMetaVO.
     *
     * @param model entity meta model
     * @return entity meta VO
     */
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(model.getCreateTime()))")
    @Mapping(target = "ut", expression = "java(mapDateToTimestamp(model.getUpdateTime()))")
    EntityMetaVO modelToVo(EntityMeta model);

    /**
     * Convert EntityMetaModel list to EntityMetaVO list.
     *
     * @param modelList entity meta model list
     * @return entity meta VO list
     */
    List<EntityMetaVO> modelToVoBatch(List<EntityMeta> modelList);
}

