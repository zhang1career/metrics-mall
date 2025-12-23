package lab.zhang.data_science.metrics_mall.struct_mapper;

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
     * Convert EntityMetaDAO to Entity.EntityMeta.
     *
     * @param dao entity meta DAO
     * @return Entity.EntityMeta
     */
    EntityMeta daoToModel(EntityMetaDAO dao);

    /**
     * Convert EntityMetaDAO list to Entity.EntityMeta list.
     *
     * @param daoList entity meta DAO list
     * @return Entity.EntityMeta list
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
     * Convert EntityMetaDAO to EntityMetaVO.
     *
     * @param dao entity meta DAO
     * @return entity meta VO
     */
    EntityMetaVO daoToVo(EntityMetaDAO dao);

    /**
     * Convert EntityMetaDAO list to EntityMetaVO list.
     *
     * @param daoList entity meta DAO list
     * @return entity meta VO list
     */
    List<EntityMetaVO> daoToVoBatch(List<EntityMetaDAO> daoList);

    /**
     * Convert Entity.EntityMeta and EntityMetaDAO to EntityMetaVO.
     * Note: Entity.EntityMeta doesn't have time fields, so we need DAO for time information.
     *
     * @param model entity meta model
     * @param dao   entity meta DAO (for time fields)
     * @return entity meta VO
     */
    @Mapping(target = "ct", source = "dao.ct")
    @Mapping(target = "ut", source = "dao.ut")
    @Mapping(target = "id", source = "model.id")
    @Mapping(target = "code", source = "model.code")
    @Mapping(target = "name", source = "model.name")
    @Mapping(target = "description", source = "model.description")
    EntityMetaVO modelToVo(EntityMeta model, EntityMetaDAO dao);
}

