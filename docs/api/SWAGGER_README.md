
## 使用方法
启动应用后，可通过以下地址访问：
Swagger UI：http://localhost:8080/swagger-ui.html
OpenAPI JSON：http://localhost:8080/v3/api-docs

## 下一步
在 Controller 类上使用注解来生成 API 文档：

```java
@RestController
@Tag(name = "示例控制器", description = "示例 API")
@RequestMapping("/api/example")
public class ExampleController {
    @GetMapping("/hello")
    @Operation(summary = "问候接口", description = "返回问候信息")
    public String hello() {
        return "Hello World";
    }
}
```


---


第三步：代码注解 (让文档更易读)
Springdoc 会自动扫描 Controller，但如果不加注解，文档里只有光秃秃的 URL。为了让“接口 A”和“接口 B”对调用者友好，我们需要加几个核心注解。

我们以你的 接口 A (实时查询) 为例：

Java

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/m_snap")
@Tag(name = "实时决策接口 (Interface A)", description = "提供基于 Redis 的毫秒级实时数据查询")
public class MetricRealtimeController {

    @GetMapping("/latest")
    @Operation(
        summary = "查询最新单点指标", 
        description = "直接穿透查询 Redis，适用于风控、推荐等高并发场景。"
    )
    public Result<Map<String, Object>> getLatestMetric(
        
        @Parameter(description = "实体类型", example = "user", required = true)
        @RequestParam String type,
        
        @Parameter(description = "实体ID (用户ID或设备ID)", example = "u123456", required = true)
        @RequestParam String id,
        
        @Parameter(description = "维度名称 (仅限元数据允许的通用维度)", example = "status")
        @RequestParam(required = false) String dim
    ) {
        // 你的业务逻辑...
        return Result.success(Map.of("status", "active"));
    }
}
核心注解说明：
@Tag: 给 Controller 分组。你可以把“接口 A”和“接口 B”分成两组，左侧菜单更清晰。

@Operation: 解释这个具体接口是干嘛的。

@Parameter: 解释每个入参的含义、是否必填，最重要的是提供 Example (示例值)。

这一点对 Playground 至关重要！ 有了 Example，用户打开页面，输入框里就已经填好了 u123456，直接点 "Execute" 就能跑通。

第四步：全局配置 (可选，为了显得专业)
新建一个配置类，配置文档的大标题、版本号等信息。

Java

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI metricPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("指标服务平台 API 文档")
                        .description("包含实时决策接口(A)与离线分析接口(B)的调试与说明")
                        .version("v1.0.0"));
    }
}
第五步：启动与验证 (API Playground)
启动 Spring Boot 应用。

浏览器访问：http://localhost:8080/swagger-ui.html (或者你在 yml 里配置的路径)。

你会看到什么？

一个漂亮的网页。

左侧分类明确（实时接口 / 分析接口）。

点开 /latest 接口，有一个 "Try it out" 按钮。

点击后，输入框里预填了你写的 Example。

点击 "Execute"，页面下方直接显示真实的 JSON 响应结果、HTTP 状态码、响应耗时。

这就是最经济、最高效的 Online Debugger。

⚠️ 一个常见的坑：Spring Security
如果你的项目集成了 Spring Security（用于鉴权），它默认会拦截 Swagger 的页面请求，导致你打不开文档。

你需要放行 Swagger 相关的路径：

Java

@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
http.authorizeHttpRequests(auth -> auth
// 放行 Swagger 相关路径
.requestMatchers(
"/swagger-ui/**",
"/v3/api-docs/**",
"/swagger-ui.html"
).permitAll()
.anyRequest().authenticated()
);
return http.build();
}
现在，你的指标服务不仅有了“骨架”（架构）和“血肉”（代码），还有了“脸面”（Swagger Playground），开发和对接体验会直接提升一个档次。