package lab.zhang.data_science.metrics_mall.controller

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.handler.GlobalExceptionHandler
import lab.zhang.data_science.metrics_mall.model.Entity.EntityMeta
import lab.zhang.data_science.metrics_mall.model.metric.PrimeMetric
import lab.zhang.data_science.metrics_mall.pojo.dao.EntityMetricRelDAO
import lab.zhang.data_science.metrics_mall.pojo.qo.EntityMetricRelQO
import lab.zhang.data_science.metrics_mall.pojo.vo.EntityMetricRelVO
import lab.zhang.data_science.metrics_mall.service.EntityMetricRelService
import lab.zhang.data_science.metrics_mall.service.EntityService
import lab.zhang.data_science.metrics_mall.service.MetricService
import lab.zhang.data_science.metrics_mall.struct_mapper.EntityMetricRelStructMapper
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import spock.lang.Specification

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Functional test for EntityMetricRelController.
 *
 * @author Rongjin Zhang
 */
class EntityMetricRelControllerTest extends Specification {

    MockMvc mockMvc
    EntityMetricRelService entityMetricRelService = Mock()
    EntityMetricRelStructMapper entityMetricRelStructMapper = Mock()
    EntityService entityService = Mock()
    MetricService metricService = Mock()
    EntityMetricRelController controller
    ObjectMapper objectMapper = new ObjectMapper()

    private static final Long ENTITY_META_ID = 1L
    private static final String ENTITY_CODE = "customer"
    private static final Long METRIC_META_ID = 2L
    private static final String METRIC_CODE = "consume_amount"


    def setup() {
        controller = new EntityMetricRelController()
        controller.entityMetricRelService = entityMetricRelService
        controller.entityMetricRelStructMapper = entityMetricRelStructMapper
        controller.entityService = entityService
        controller.metricService = metricService
        def validator = new LocalValidatorFactoryBean()
        validator.afterPropertiesSet()
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build()
    }

    def "test create success"() {
        given:
        def qo = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("test_alias")
                .dataUri("test://data/uri")
                .build()
        def entityMeta = EntityMeta.builder()
                .id(ENTITY_META_ID.intValue())
                .code(ENTITY_CODE)
                .build()
        def primeMetric = PrimeMetric.builder()
                .id(METRIC_META_ID)
                .code(METRIC_CODE)
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityService.getEntityMetaByCode(ENTITY_CODE) >> entityMeta
        1 * metricService.getPrimeMetricByCode(METRIC_CODE) >> primeMetric
        1 * entityMetricRelService.create(1L, 2L, "test_alias", "test://data/uri") >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test create validation failure"() {
        given:
        def qo = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
                .andExpect(jsonPath('$.errmsg').exists())
    }

    def "test get success"() {
        given:
        def entityMetaId = 1L
        def metricMetaId = 2L
        def dao = new EntityMetricRelDAO()
        dao.setEntityMetaId(entityMetaId)
        dao.setMetricMetaId(metricMetaId)
        dao.setAlias("test_alias")
        dao.setDataUri("test://data/uri")
        def vo = EntityMetricRelVO.builder()
                .entityMetaId(entityMetaId)
                .metricMetaId(metricMetaId)
                .alias("test_alias")
                .dataUri("test://data/uri")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metric_rels")
                .param("entityMetaId", entityMetaId.toString())
                .param("metricMetaId", metricMetaId.toString()))

        then:
        1 * entityMetricRelService.get(entityMetaId, metricMetaId) >> dao
        1 * entityMetricRelStructMapper.daoToVo(dao) >> vo
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.entityMetaId').value(entityMetaId))
                .andExpect(jsonPath('$.data.metricMetaId').value(metricMetaId))
                .andExpect(jsonPath('$.data.alias').value("test_alias"))
    }

    def "test get not found"() {
        given:
        def entityMetaId = 1L
        def metricMetaId = 2L

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metric_rels")
                .param("entityMetaId", entityMetaId.toString())
                .param("metricMetaId", metricMetaId.toString()))

        then:
        1 * entityMetricRelService.get(entityMetaId, metricMetaId) >> null
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').isEmpty())
    }

    def "test listByEntityMetaId success"() {
        given:
        def entityMetaId = 1L
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(entityMetaId)
        dao1.setMetricMetaId(2L)
        dao1.setAlias("alias1")
        dao1.setDataUri("uri1")
        def dao2 = new EntityMetricRelDAO()
        dao2.setEntityMetaId(entityMetaId)
        dao2.setMetricMetaId(3L)
        dao2.setAlias("alias2")
        dao2.setDataUri("uri2")
        def daoList = [dao1, dao2]
        def vo1 = EntityMetricRelVO.builder()
                .entityMetaId(entityMetaId)
                .metricMetaId(2L)
                .alias("alias1")
                .dataUri("uri1")
                .build()
        def vo2 = EntityMetricRelVO.builder()
                .entityMetaId(entityMetaId)
                .metricMetaId(3L)
                .alias("alias2")
                .dataUri("uri2")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metric_rels/entity/${entityMetaId}"))

        then:
        1 * entityMetricRelService.listByEntityMetaId(entityMetaId) >> daoList
        1 * entityMetricRelStructMapper.daoToVo(dao1) >> vo1
        1 * entityMetricRelStructMapper.daoToVo(dao2) >> vo2
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].entityMetaId').value(entityMetaId))
                .andExpect(jsonPath('$.data[0].metricMetaId').value(2))
                .andExpect(jsonPath('$.data[1].metricMetaId').value(3))
    }

    def "test listByMetricMetaId success"() {
        given:
        def metricMetaId = 2L
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(1L)
        dao1.setMetricMetaId(metricMetaId)
        dao1.setAlias("alias1")
        dao1.setDataUri("uri1")
        def daoList = [dao1]
        def vo1 = EntityMetricRelVO.builder()
                .entityMetaId(1L)
                .metricMetaId(metricMetaId)
                .alias("alias1")
                .dataUri("uri1")
                .build()

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metric_rels/metric/${metricMetaId}"))

        then:
        1 * entityMetricRelService.listByMetricMetaId(metricMetaId) >> daoList
        1 * entityMetricRelStructMapper.daoToVo(dao1) >> vo1
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].metricMetaId').value(metricMetaId))
    }

    def "test list success"() {
        given:
        def dao1 = new EntityMetricRelDAO()
        dao1.setEntityMetaId(1L)
        dao1.setMetricMetaId(2L)
        dao1.setAlias("alias1")
        dao1.setDataUri("uri1")
        def daoList = [dao1]
        def vo1 = EntityMetricRelVO.builder()
                .entityMetaId(1L)
                .metricMetaId(2L)
                .alias("alias1")
                .dataUri("uri1")
                .build()
        def voList = [vo1]

        when:
        def response = mockMvc.perform(get("/api/v1/entity_metric_rels/list"))

        then:
        1 * entityMetricRelService.list() >> daoList
        1 * entityMetricRelStructMapper.daoToVoBatch(daoList) >> voList
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data[0].entityMetaId').value(1))
    }

    def "test update success"() {
        given:
        def qo = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("updated_alias")
                .dataUri("updated://data/uri")
                .build()
        def entityMeta = EntityMeta.builder()
                .id(ENTITY_META_ID.intValue())
                .code(ENTITY_CODE)
                .build()
        def primeMetric = PrimeMetric.builder()
                .id(METRIC_META_ID)
                .code(METRIC_CODE)
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        1 * entityService.getEntityMetaByCode(ENTITY_CODE) >> entityMeta
        1 * metricService.getPrimeMetricByCode(METRIC_CODE) >> primeMetric
        1 * entityMetricRelService.update(1L, 2L, "updated_alias", "updated://data/uri") >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "test update validation failure"() {
        given:
        def qo = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .build()

        when:
        def response = mockMvc.perform(put("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(qo)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(400))
                .andExpect(jsonPath('$.errmsg').exists())
    }

    def "test delete success"() {
        given:
        def entityMetaId = 1L
        def metricMetaId = 2L

        when:
        def response = mockMvc.perform(delete("/api/v1/entity_metric_rels")
                .param("entityMetaId", entityMetaId.toString())
                .param("metricMetaId", metricMetaId.toString()))

        then:
        1 * entityMetricRelService.delete(entityMetaId, metricMetaId) >> true
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}

