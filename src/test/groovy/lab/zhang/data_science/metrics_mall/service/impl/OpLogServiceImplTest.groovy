package lab.zhang.data_science.metrics_mall.service.impl

import lab.zhang.data_science.metrics_mall.enums.OpEventEnum
import lab.zhang.data_science.metrics_mall.mapper.OpLogMapper
import lab.zhang.data_science.metrics_mall.model.OpLog
import lab.zhang.data_science.metrics_mall.pojo.dao.OpLogDAO
import lab.zhang.data_science.metrics_mall.struct_mapper.OpLogStructMapper
import spock.lang.Specification

import java.math.BigInteger
import java.util.Date

/**
 * Test for OpLogServiceImpl.
 *
 * @author Rongjin Zhang
 */
class OpLogServiceImplTest extends Specification {

    OpLogMapper opLogMapper = Mock()
    OpLogStructMapper opLogStructMapper = Mock()
    OpLogServiceImpl service

    private static final BigInteger LOG_ID = BigInteger.valueOf(10000000L)
    private static final Long OPERATOR_ID = 1001L
    private static final Long OPERATE_TS = System.currentTimeMillis()
    private static final Long CREATE_TS = System.currentTimeMillis()

    def setup() {
        service = new OpLogServiceImpl(opLogMapper, opLogStructMapper)
    }

    def "test get success"() {
        given:
        def idLong = LOG_ID.longValue()
        def dao = OpLogDAO.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(CREATE_TS)
                .build()
        def model = OpLog.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()

        when:
        def result = service.get(idLong)

        then:
        1 * opLogMapper.selectById(idLong) >> dao
        1 * opLogStructMapper.daoToModel(dao) >> model
        result == model
    }

    def "test get with null id"() {
        when:
        def result = service.get(null)

        then:
        0 * opLogMapper.selectById(_)
        0 * opLogStructMapper.daoToModel(_)
        result == null
    }

    def "test get with not found"() {
        given:
        def idLong = LOG_ID.longValue()

        when:
        def result = service.get(idLong)

        then:
        1 * opLogMapper.selectById(idLong) >> null
        1 * opLogStructMapper.daoToModel(null) >> null
        result == null
    }

    def "test list success"() {
        given:
        def daoList = [
                OpLogDAO.builder()
                        .id(LOG_ID)
                        .event(OpEventEnum.CREATE_METRIC_META.getId())
                        .operatorId(OPERATOR_ID)
                        .operateTs(OPERATE_TS)
                        .ct(CREATE_TS)
                        .build(),
                OpLogDAO.builder()
                        .id(BigInteger.valueOf(10000001L))
                        .event(OpEventEnum.UPDATE_METRIC_META.getId())
                        .operatorId(OPERATOR_ID)
                        .operateTs(OPERATE_TS)
                        .ct(CREATE_TS)
                        .build()
        ]
        def modelList = [
                OpLog.builder()
                        .id(LOG_ID)
                        .event(OpEventEnum.CREATE_METRIC_META)
                        .operatorId(OPERATOR_ID)
                        .operateTime(new Date(OPERATE_TS))
                        .build(),
                OpLog.builder()
                        .id(BigInteger.valueOf(10000001L))
                        .event(OpEventEnum.UPDATE_METRIC_META)
                        .operatorId(OPERATOR_ID)
                        .operateTime(new Date(OPERATE_TS))
                        .build()
        ]

        when:
        def result = service.list()

        then:
        1 * opLogMapper.selectList(null) >> daoList
        1 * opLogStructMapper.daoToModelBatch(daoList) >> modelList
        result == modelList
        result.size() == 2
    }

    def "test list empty"() {
        when:
        def result = service.list()

        then:
        1 * opLogMapper.selectList(null) >> []
        1 * opLogStructMapper.daoToModelBatch([]) >> []
        result == []
        result.isEmpty()
    }

    def "test count success"() {
        when:
        def result = service.count()

        then:
        1 * opLogMapper.selectCount(null) >> 10L
        result == 10L
    }

    def "test count zero"() {
        when:
        def result = service.count()

        then:
        1 * opLogMapper.selectCount(null) >> 0L
        result == 0L
    }

    def "test insert success with create time"() {
        given:
        def model = OpLog.builder()
                .event(OpEventEnum.CREATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()
        def daoWithoutCt = OpLogDAO.builder()
                .event(OpEventEnum.CREATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(null)
                .build()
        def daoWithCt = OpLogDAO.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(CREATE_TS)
                .build()
        def resultModel = OpLog.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()

        when:
        def result = service.insert(model)

        then:
        1 * opLogStructMapper.modelToDao(model) >> daoWithoutCt
        1 * opLogMapper.insert(_ as OpLogDAO) >> { OpLogDAO dao ->
            assert dao.getCt() != null
            assert dao.getCt() > 0
            1
        }
        1 * opLogStructMapper.daoToModel(_) >> { OpLogDAO dao ->
            assert dao.getCt() != null
            resultModel
        }
        result != null
        result.getId() == LOG_ID
    }

    def "test insert success with existing create time"() {
        given:
        def model = OpLog.builder()
                .event(OpEventEnum.CREATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()
        def daoWithCt = OpLogDAO.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(CREATE_TS)
                .build()
        def resultModel = OpLog.builder()
                .id(LOG_ID)
                .event(OpEventEnum.CREATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()

        when:
        def result = service.insert(model)

        then:
        1 * opLogStructMapper.modelToDao(model) >> daoWithCt
        1 * opLogMapper.insert(daoWithCt) >> 1
        1 * opLogStructMapper.daoToModel(daoWithCt) >> resultModel
        result == resultModel
        result.getId() == LOG_ID
    }

    def "test insert with null model"() {
        when:
        def result = service.insert(null)

        then:
        0 * opLogStructMapper.modelToDao(_)
        0 * opLogMapper.insert(_)
        0 * opLogStructMapper.daoToModel(_)
        result == null
    }

    def "test update success"() {
        given:
        def model = OpLog.builder()
                .id(LOG_ID)
                .event(OpEventEnum.UPDATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()
        def dao = OpLogDAO.builder()
                .id(LOG_ID)
                .event(OpEventEnum.UPDATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(CREATE_TS)
                .build()

        when:
        def result = service.update(model)

        then:
        1 * opLogStructMapper.modelToDao(model) >> dao
        1 * opLogMapper.updateById(dao) >> 1
        result == true
    }

    def "test update with null model"() {
        when:
        def result = service.update(null)

        then:
        0 * opLogStructMapper.modelToDao(_)
        0 * opLogMapper.updateById(_)
        result == false
    }

    def "test update with null id"() {
        given:
        def model = OpLog.builder()
                .id(null)
                .event(OpEventEnum.UPDATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()

        when:
        def result = service.update(model)

        then:
        0 * opLogStructMapper.modelToDao(_)
        0 * opLogMapper.updateById(_)
        result == false
    }

    def "test update with not found"() {
        given:
        def model = OpLog.builder()
                .id(LOG_ID)
                .event(OpEventEnum.UPDATE_METRIC_META)
                .operatorId(OPERATOR_ID)
                .operateTime(new Date(OPERATE_TS))
                .build()
        def dao = OpLogDAO.builder()
                .id(LOG_ID)
                .event(OpEventEnum.UPDATE_METRIC_META.getId())
                .operatorId(OPERATOR_ID)
                .operateTs(OPERATE_TS)
                .ct(CREATE_TS)
                .build()

        when:
        def result = service.update(model)

        then:
        1 * opLogStructMapper.modelToDao(model) >> dao
        1 * opLogMapper.updateById(dao) >> 0
        result == false
    }

    def "test delete success"() {
        given:
        def idLong = LOG_ID.longValue()

        when:
        def result = service.delete(idLong)

        then:
        1 * opLogMapper.deleteById(idLong) >> 1
        result == true
    }

    def "test delete with null id"() {
        when:
        def result = service.delete(null)

        then:
        0 * opLogMapper.deleteById(_)
        result == false
    }

    def "test delete with not found"() {
        given:
        def idLong = LOG_ID.longValue()

        when:
        def result = service.delete(idLong)

        then:
        1 * opLogMapper.deleteById(idLong) >> 0
        result == false
    }
}

