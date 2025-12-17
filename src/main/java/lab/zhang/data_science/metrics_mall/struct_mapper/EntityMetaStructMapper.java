package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.Entity.EntityMeta;
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetaDAO;
import org.mapstruct.Mapper;

/**
 * Mapper for converting EntityMeta related objects.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface EntityMetaStructMapper {

    /**
     * Convert EntityMetaDAO to EntityMeta.
     *
     * @param dao entity meta dao
     * @return EntityMeta
     */
    EntityMeta daoToModel(EntityMetaDAO dao);
}

