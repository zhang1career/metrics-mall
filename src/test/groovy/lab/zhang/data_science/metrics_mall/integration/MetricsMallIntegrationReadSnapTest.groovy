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

import static org.hamcrest.Matchers.lessThan
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
@Transactional
class MetricsMallIntegrationReadSnapTest extends Specification {

    @Autowired
    MockMvc mockMvc

    ObjectMapper objectMapper = new ObjectMapper()

    private static final String API_KEY = "test-api-key"
    private static final String ENTITY_CODE = "customer"
    private static final String METRIC_CODE = "consume_amount"
    private static final Long ENTITY_ID = 10000001L
    private static final Long REQUEST_SNAPSHOT_TS = 1766507545000L
    private static final Long ACTUAL_SNAPSHOT_TS = 1766505929573L

    // ==================== 4. 快照查询测试阶段 ====================

    def "4.1 查询快照 - 成功（指定版本）"() {
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
        def entityMetricRelQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()

        def versionQO11 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO12 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(2)
                .calcLogic("xxx")
                .build()
        def versionQO13 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(3)
                .calcLogic("xxx")
                .build()
        def versionQO14 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(4)
                .calcLogic("xxx")
                .build()

        def snapshotQO = MetricSnapshotQO.builder()
                .ec(ENTITY_CODE)
                .eid(ENTITY_ID)
                .metrics([EchoMetricQO.builder()
                                  .code(METRIC_CODE)
                                  .alias("amount")
                                  .v(1)
                                  .build()])
                .build()

        when:
        // create metric
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetricRelQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        // online version 1
        mockMvc.perform(post("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO11)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO13)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO14)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))

        and:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values.consume_amount').exists())
    }

    def "4.2 查询快照 - 成功（指定时间戳，返回邻近的旧值）"() {
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
        def entityMetricRelQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()

        def versionQO11 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO12 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(2)
                .calcLogic("xxx")
                .build()
        def versionQO13 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(3)
                .calcLogic("xxx")
                .build()
        def versionQO14 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(4)
                .calcLogic("xxx")
                .build()


        def snapshotQO = MetricSnapshotQO.builder()
                .ec(ENTITY_CODE)
                .eid(ENTITY_ID)
                .snapshotTs(REQUEST_SNAPSHOT_TS)
                .metrics([EchoMetricQO.builder()
                                  .code(METRIC_CODE)
                                  .alias("amount")
                                  .v(1)
                                  .build()])
                .build()

        when:
        // create metric
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetricRelQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        // online version 1
        mockMvc.perform(post("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO11)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO13)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO14)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))

        and:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values.consume_amount').exists())
                .andExpect(jsonPath('$.data._ts.consume_amount', lessThan(REQUEST_SNAPSHOT_TS)))
    }

    def "4.3 查询快照 - 成功（指定时间戳，精确匹配）"() {
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
        def entityMetricRelQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()

        def versionQO11 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO12 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(2)
                .calcLogic("xxx")
                .build()
        def versionQO13 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(3)
                .calcLogic("xxx")
                .build()
        def versionQO14 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(4)
                .calcLogic("xxx")
                .build()


        def snapshotQO = MetricSnapshotQO.builder()
                .ec(ENTITY_CODE)
                .eid(ENTITY_ID)
                .snapshotTs(REQUEST_SNAPSHOT_TS)
                .metrics([EchoMetricQO.builder()
                                  .code(METRIC_CODE)
                                  .alias("amount")
                                  .v(1)
                                  .build()])
                .build()

        when:
        // create metric
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetricRelQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        // online version 1
        mockMvc.perform(post("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO11)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO13)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO14)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))

        and:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values.consume_amount').exists())
                .andExpect(jsonPath('$.data._ts.consume_amount').value(ACTUAL_SNAPSHOT_TS))
    }

    def "4.4 查询快照 - 成功（指定时间戳，查询结果为空）"() {
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
        def entityMetricRelQO = EntityMetricRelQO.builder()
                .entityCode(ENTITY_CODE)
                .metricCode(METRIC_CODE)
                .alias("amount")
                .dataUri("xxx")
                .build()

        def versionQO11 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(1)
                .calcLogic("xxx")
                .build()
        def versionQO12 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(2)
                .calcLogic("xxx")
                .build()
        def versionQO13 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(3)
                .calcLogic("xxx")
                .build()
        def versionQO14 = MetricVersionQO.builder()
                .metricCode(METRIC_CODE)
                .version(1)
                .isMain(0)
                .lifeStatus(4)
                .calcLogic("xxx")
                .build()


        def snapshotQO = MetricSnapshotQO.builder()
                .ec(ENTITY_CODE)
                .eid(ENTITY_ID)
                .snapshotTs(123)
                .metrics([EchoMetricQO.builder()
                                  .code(METRIC_CODE)
                                  .alias("amount")
                                  .v(1)
                                  .build()])
                .build()

        when:
        // create metric
        mockMvc.perform(post("/api/v1/entity_metas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetaQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/metrics")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(metricQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(post("/api/v1/entity_metric_rels")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(entityMetricRelQO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        // online version 1
        mockMvc.perform(post("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO11)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO12)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO13)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
        mockMvc.perform(put("/api/v1/metric_versions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(versionQO14)))
                .andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))

        and:
        def response = mockMvc.perform(post("/api/v1/m_snap")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(snapshotQO)))

        then:
        response.andExpect(status().isOk())
                .andExpect(jsonPath('$.code').value(0))
                .andExpect(jsonPath('$.data.values').isEmpty())
    }
}

