package lab.zhang.data_science.metrics_mall.struct_mapper;

import lab.zhang.data_science.metrics_mall.model.OpLog;
import lab.zhang.data_science.metrics_mall.pojo.dao.OpLogDAO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper for converting between OpLogDAO and OpLogModel.
 *
 * @author Rongjin Zhang
 */
@Mapper(componentModel = "spring")
public interface OpLogStructMapper extends BaseStructMapper {
    /**
     * Convert OpLogDAO to OpLogModel.
     *
     * @param dao operation log entity
     * @return operation log model
     */
    @Mapping(target = "event", expression = "java(mapEvent(dao.getEvent()))")
    @Mapping(target = "operateTime", expression = "java(mapTimestampToDate(dao.getOperateTs()))")
    @Mapping(target = "createTime", expression = "java(mapTimestampToDate(dao.getCt()))")
    @Mapping(target = "updateTime", ignore = true)
    OpLog daoToModel(OpLogDAO dao);


    /**
     * Convert list of OpLogDAO to list of OpLogModel.
     *
     * @param daoList operation log entity list
     * @return operation log model list
     */
    List<OpLog> daoToModelBatch(List<OpLogDAO> daoList);

    /**
     * Convert OpLogModel to OpLogDAO.
     *
     * @param model operation log model
     * @return operation log entity
     */
    @Mapping(target = "event", expression = "java(mapEventToInt(model.getEvent()))")
    @Mapping(target = "operateTs", expression = "java(mapDateToTimestamp(model.getOperateTime()))")
    @Mapping(target = "ct", expression = "java(mapDateToTimestamp(model.getCreateTime()))")
    OpLogDAO modelToDao(OpLog model);
}

