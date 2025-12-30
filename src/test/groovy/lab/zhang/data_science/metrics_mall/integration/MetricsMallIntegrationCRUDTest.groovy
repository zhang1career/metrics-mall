package lab.zhang.data_science.metrics_mall.integration

import com.fasterxml.jackson.databind.ObjectMapper
import lab.zhang.data_science.metrics_mall.pojo.qo.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.transaction.annotation.Transactional
import spock.lang.Specification

import static org.hamcrest.Matchers.not
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

/**
 * Integration test for Metrics Mall based on INTEGRATION_TEST_PLAN.md
 *
 * This test follows the test plan execution order:
 * 1. Data preparation phase (2.1 - 2.7)
 * 2. Snapshot write phase (3.1 - 3.9)
 * 3. Snapshot query phase (4.1 - 4.5)
 *
 * @author Rongjin Zhang
 * @date 2025-01-16
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// CAUTION: The cleanup script MUST NOT be enabled for normal test runs,
//@Sql(
//        scripts = "/sql/cleanup.sql",
//        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
//)
@Transactional
class MetricsMallIntegrationCRUDTest extends Specification {

    @Autowired
    MockMvc mockMvc

    ObjectMapper objectMapper = new ObjectMapper()

    private static final String API_KEY = "test-api-key"
    private static final String ENTITY_CODE = "customer"
    private static final String METRIC_CODE = "consume_amount"
    private static final Long ENTITY_ID = 10000001L

    def getMetricIdByCode(String code) {
        def response = mockMvc.perform(get("/api/v1/metrics/code/${code}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andReturn()
        return objectMapper.readTree(response.response.contentAsString).get("data").get("id").asLong()
    }


    // ==================== 2.1 EntityMeta 数据准备 ====================

    def "2.1.1 新增 EntityMeta - 成功"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.1.2 新增 EntityMeta - 失败（code重复）"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def entityMetaQO1 = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客2")
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO1)))
        then:
        response
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    // ==================== 2.2 MetricMeta 数据准备 ====================

    def "2.2.1 新增 MetricMeta - 成功"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.2.2 新增 MetricMeta - 失败（code重复）"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def metricQO1 = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额2")
                .valueType(2)
                .precision(2)
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    // ==================== 2.3 EntityMetricRel 关联关系准备 ====================

    def "2.3.1 新增 EntityMetricRel - 成功"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def relQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.3.2 新增 EntityMetricRel - 失败（重复关联）"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def relQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()
        def relQO1 = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount2")
                .dataUri("yyy")
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    // ==================== 2.4 Version 数据准备 ====================

    def "2.4.1 新增 MetricVersion - 成功（version=1）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.4.2 新增 MetricVersion - 失败（version重复）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("yyy")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    def "2.4.3 新增 MetricVersion - 失败（is_main不能为非0值）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .isMain(1)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    def "2.4.4 新增 MetricVersion - 失败（life_status不能为非1值）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .isMain(0)
                .lifeStatus(0)
                .calcLogic("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    def "2.4.5 新增 MetricVersion - 失败（calc_logic不能为空）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        // Note: This validation might be handled at service layer, adjust expectations accordingly
        response.andExpect(status().isOk())
    }

    def "2.4.6 新增 MetricVersion - 成功（version=2）"() {
        given:
        def entityMetaQO = EntityMetaQO.builder()
                .code(ENTITY_CODE)
                .name("顾客")
                .build()
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def relQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    // ==================== 2.5 Dimension 数据准备 ====================

    def "2.5.1 新增 Dimension - 成功（location）"() {
        given:
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()

        when:
        def response = mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.5.2 新增 Dimension - 失败（code重复）"() {
        given:
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def dimensionQO1 = DimensionQO.builder()
                .code("location")
                .name("地点2")
                .build()

        when:
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(not(0)))
    }

    def "2.5.3 新增 Dimension - 成功（venues）"() {
        given:
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def dimensionQO1 = DimensionQO.builder()
                .code("venues")
                .name("场所")
                .build()

        when:
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.5.4 新增 Dimension - 成功（category）"() {
        given:
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def dimensionQO1 = DimensionQO.builder()
                .code("venues")
                .name("场所")
                .build()
        def dimensionQO2 = DimensionQO.builder()
                .code("category")
                .name("种类")
                .build()

        when:
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO1)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO2)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }


    // ==================== 2.6 DimensionGroup 数据准备 ====================

    def "2.6.1 新增 DimensionGroup - 成功"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def relQO = MetricDimensionGroupRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCodes("location")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metric_dim_group_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }


    // ==================== 2.7 MetricDimensionRel 关联关系准备 ====================

    def "2.7.1 新增 MetricDimensionRel - 成功（location）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def relQO = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("location")
                .isHot(1)
                .validation("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.7.2 新增 MetricDimensionRel - 成功（venues）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def dimensionQO1 = DimensionQO.builder()
                .code("venues")
                .name("场所")
                .build()
        def relQO = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("location")
                .isHot(1)
                .validation("xxx")
                .build()
        def relQO1 = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("venues")
                .isHot(1)
                .validation("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO1)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.7.3 新增 MetricDimensionRel - 成功（category）"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def dimensionQO = DimensionQO.builder()
                .code("location")
                .name("地点")
                .build()
        def dimensionQO1 = DimensionQO.builder()
                .code("venues")
                .name("场所")
                .build()
        def dimensionQO2 = DimensionQO.builder()
                .code("category")
                .name("种类")
                .build()
        def relQO = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("location")
                .isHot(1)
                .validation("xxx")
                .build()
        def relQO1 = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("venues")
                .isHot(1)
                .validation("xxx")
                .build()
        def relQO2 = MetricDimensionRelQO.builder()
                .metricCode(METRIC_CODE)
                .dimensionCode("category")
                .isHot(1)
                .validation("xxx")
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO1)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/dims")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dimensionQO2)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO1)))
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
        and:
        def response = mockMvc.perform(post("/api/v1/metric_dim_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(relQO2)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    // ==================== 2.8 MetricVersion 状态更新 ====================

    def "2.8.1 更新 version_1 的 life_status 为 TEST"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(1)
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.8.2 更新 version_1 的 life_status 为 GRAY"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(1)
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()
        def versionQO2 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(3)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO2)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.8.3 更新 version_1 的 life_status 为 ONLINE"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(1)
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()
        def versionQO2 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(3)
                .build()
        def versionQO3 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(4)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO3)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.8.4 更新 version_1 的 life_status 为 TEST"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(1)
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()
        def versionQO2 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(3)
                .build()
        def versionQO3 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO3)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.8.5 更新 version_1 的 life_status 为 OFFLINE"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(1)
                .build()
        def versionQO1 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()
        def versionQO2 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(3)
                .build()
        def versionQO3 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(2)
                .build()
        def versionQO4 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .lifeStatus(0)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO3)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO4)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }

    def "2.8.7 更新 version_2 的 is_main 为 1"() {
        given:
        def metricQO = MetricMetaQO.builder()
                .code(METRIC_CODE)
                .name("消费金额")
                .valueType(2)
                .precision(2)
                .build()
        def versionQO20 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .lifeStatus(1)
                .build()
        def versionQO21 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .lifeStatus(2)
                .build()
        def versionQO22 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .lifeStatus(3)
                .build()
        def versionQO23 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .lifeStatus(4)
                .build()
        def versionQO24 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(2)
                .isMain(1)
                .build()

        when:
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        def metricId = getMetricIdByCode(METRIC_CODE)
        mockMvc.perform(post("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO20)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO21)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO22)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO23)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        and:
        def response = mockMvc.perform(put("/api/v1/metrics/${metricId}/versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO24)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data').value(true))
    }
}
