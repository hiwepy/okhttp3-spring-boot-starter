# okhttp3-spring-boot-starter

Spring Boot 自动装配层，只负责属性绑定、Bean 装配、Actuator/metrics 接入；纯 Java 能力下沉到 `okhttp3-extension` 与 `okhttp3-metrics-prometheus`。

## Maven

```xml
<dependency>
  <groupId>io.github.hiwepy</groupId>
  <artifactId>okhttp3-spring-boot-starter</artifactId>
  <version>2.7.x.20260630-SNAPSHOT</version>
</dependency>
```

## 依赖关系

- `okhttp3-extension`
  - SSL、CookieJar、Interceptor、纯 Java Response/工具
- `okhttp3-metrics-prometheus`
  - OkHttp 指标采集与 Micrometer 绑定
- `okhttp3-spring-boot-starter`
  - `@ConfigurationProperties`
  - `@Configuration`
  - Actuator / metrics 装配

## 版本矩阵

| Starter 分支 | Spring Boot Parent | JDK | `okhttp3-extension` | `okhttp3-metrics-prometheus` |
|--------------|--------------------|-----|---------------------|------------------------------|
| `2.3.x` | `2.3.12.RELEASE` | 8 | `1.0.x.*` | `1.0.x.*` |
| `2.4.x` | `2.4.13` | 8 | `1.0.x.*` | `1.0.x.*` |
| `2.5.x` | `2.5.15` | 8 | `1.0.x.*` | `1.0.x.*` |
| `2.6.x` | `2.6.15` | 8 | `1.0.x.*` | `1.0.x.*` |
| `2.7.x` | `2.7.18` | 8 | `1.0.x.*` | `1.0.x.*` |
| `3.0.x` | `3.0.13` | 17 | `2.0.x.*` | `2.0.x.*` |
| `3.1.x` | `3.1.12` | 17 | `2.0.x.*` | `2.0.x.*` |
| `3.2.x` | `3.2.12` | 17 | `2.0.x.*` | `2.0.x.*` |
| `3.3.x` | `3.3.13` | 17 | `2.0.x.*` | `2.0.x.*` |
| `3.4.x` | `3.4.13` | 17 | `2.0.x.*` | `2.0.x.*` |
| `3.5.x` | `3.5.16` | 17 | `2.0.x.*` | `2.0.x.*` |
| `4.0.x` | `4.0.7` | 21 | `3.0.x.*` | `3.0.x.*` |
| `4.1.x` | `4.1.0` | 21 | `3.0.x.*` | `3.0.x.*` |

## 分支 POM 生成

```bash
python3 scripts/render-branch-pom.py 2.7.x
```

## 配置示例

```yaml
okhttp3:
  connect-timeout: 5s
  read-timeout: 30s
  write-timeout: 30s
  retry-on-connection-failure: false
  log-level: HEADERS
  pool:
    max-idle-connections: 48
    max-requests: 128
    max-requests-per-host: 24
```

## License

Apache License 2.0
