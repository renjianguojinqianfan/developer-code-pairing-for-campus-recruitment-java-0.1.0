# HoCATLing 🐾

HoCATLing（Hands-on Clean Architecture Template Ling）是一个基于整洁架构和 DDD 的订餐系统订单管理模块，用于校招技术考核。

## 🎯 作业说明

本项目已实现基础的订单创建和查询功能，你的任务是：**为订单添加折扣功能**

详细需求请查看：📄 [需求文档](docs/Requirements.md)

### 你需要提交什么？

1. **需求分析文档**：你对折扣功能的理解和验收标准（AC）定义
2. **领域模型设计**：折扣相关的类设计和包结构
3. **测试用例**：先写测试，再写实现（TDD）
4. **实现代码**：通过所有测试的完整实现
5. **Prompt History**：请使用 AI 辅助开发，需提交完整对话记录

---

## 🏗️ 架构理解

本项目采用简化版 Clean Architecture，核心思想是**依赖方向从外向内**：

```
Web层 → Application层 → Domain层 ← Infrastructure层
```

### 各层职责

- **Web 层**（`web/`）：处理 HTTP 请求，参数校验，返回响应
- **Application 层**（`application/`）：编排业务流程，调用领域对象
- **Domain 层**（`domain/`）：核心业务逻辑，领域模型和规则
- **Infrastructure 层**（`infrastructure/`）：技术实现，如数据库访问

### 关键设计点

1. **Repository 接口在 Domain 层定义**，由 Infrastructure 层实现（依赖倒置）
2. **领域对象是充血模型**，包含业务逻辑（如 `Order` 的价格计算）
3. **使用值对象**（如 `OrderId`、`UserId`）保证类型安全

---

## 🚀 快速开始

### 环境要求

- Java 21+
- Gradle 8.x+（项目自带 Gradle Wrapper，无需单独安装）

### 启动应用

```bash
# 使用 H2 内存数据库（默认）
./gradlew bootRun

# 使用 MySQL（需先启动 Docker Compose）
docker compose up -d
./gradlew bootRun --args='--spring.profiles.active=mysql'
```

应用启动后访问：`http://localhost:8080`

### 测试 API

```bash
# 创建订单（需要认证，默认用户：user/password）
curl -X POST http://localhost:8080/api/v1/orders \
  -u user:password \
  -H "Content-Type: application/json" \
  -d '{
    "merchantId": "merchant-001",
    "items": [
      {
        "dishId": "dish-001",
        "dishName": "宫保鸡丁",
        "quantity": 2,
        "price": 28.00
      }
    ],
    "deliveryInfo": {
      "recipientName": "张三",
      "recipientPhone": "13800138000",
      "address": "北京市朝阳区xxx路xxx号"
    },
    "remark": "少辣"
  }'

# 查询订单
curl http://localhost:8080/api/v1/orders/{orderId} -u user:password
```

---

## 🧪 运行测试

```bash
# 单元测试（快速验证核心逻辑）
./gradlew test

# 集成测试（验证各层协作）
./gradlew integrationTest

# 契约测试（验证 API 契约）
./gradlew contractTest

# 完整构建（包含代码格式检查和覆盖率验证，要求 70%+）
./gradlew build
```

测试覆盖率报告：`app/build/reports/jacoco/test/html/index.html`

---

## 📂 代码导航

### 核心文件位置

```
app/src/main/java/com/example/demo/
├── domain/order/
│   ├── Order.java              # 订单聚合根（核心业务逻辑）
│   ├── OrderItem.java          # 订单项
│   ├── Pricing.java            # 价格计算（你需要修改这里）
│   └── OrderRepository.java    # 仓储接口
├── application/service/
│   └── CreateOrderService.java # 创建订单服务
└── web/order/
    └── CreateOrderController.java  # 订单 API 端点
```

### 建议的学习路径

1. **先看测试**：`app/src/test/java/` 和 `app/src/integrationTest/java/`
2. **理解领域模型**：从 `Order.java` 开始，看懂价格如何计算
3. **追踪数据流**：Controller → Service → Domain → Repository
4. **查看数据库映射**：`infrastructure/persistence/order/entity/`

---


## � 实现折扣功能的提示

### 思考题

1. 折扣应该是一个什么样的对象？（值对象？实体？）
2. 折扣逻辑应该放在哪一层？（Domain 层的 `Pricing`？）
3. 如何设计才能支持未来扩展其他折扣类型？
4. 折扣信息需要持久化吗？如果需要，如何设计数据库表？

### 推荐步骤

1. **定义 AC**：明确"满减"的计算规则和边界条件
2. **设计领域模型**：创建 `Discount` 相关类
3. **编写测试**：先写失败的测试用例
4. **实现功能**：让测试通过
5. **集成到 API**：修改 Controller 和 Service
6. **验证**：运行所有测试，确保覆盖率达标

---

## �🛠️ 技术栈

- **框架**：Spring Boot 3.4.2、Spring Data JPA、Spring Security
- **数据库**：MySQL 8.0 / H2（内存数据库）
- **测试**：JUnit 5、AssertJ、Mockito、Spring Cloud Contract
- **工具**：Spotless（代码格式化）、JaCoCo（覆盖率）、Flyway（数据库迁移）、Lombok

---

## 📚 参考资料

### 架构与设计

- [Clean Architecture 原文](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html) - Uncle Bob 的经典文章
- [DDD 领域驱动设计](https://www.domainlanguage.com/ddd/) - Eric Evans 的 DDD 官方资源
- [原始模板项目 HoCAT](https://github.com/macdao/hands-on-clean-architecture-template) - 本项目的完整版模板

### 测试框架与工具

- [JUnit 5 用户指南](https://junit.org/junit5/docs/current/user-guide/) - 单元测试框架
- [AssertJ 文档](https://assertj.github.io/doc/) - 流式断言库（比 JUnit 自带的更好用）
- [Mockito 文档](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html) - Mock 框架，用于隔离依赖
- [Spring Boot Testing](https://docs.spring.io/spring-boot/reference/testing/index.html) - Spring Boot 测试指南
- [Spring Cloud Contract](https://spring.io/projects/spring-cloud-contract) - 契约测试框架

### Java 新特性

- [Java Record](https://docs.oracle.com/en/java/javase/21/language/records.html) - 本项目大量使用 Record 作为 DTO
- [Java 21 新特性](https://openjdk.org/projects/jdk/21/) - 了解项目使用的 Java 版本

### Spring 生态

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot) - 框架核心文档
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa) - 数据持久化
- [Spring Security](https://spring.io/projects/spring-security) - 安全认证
- [Bean Validation](https://beanvalidation.org/2.0/spec/) - 参数校验规范（`@NotNull`、`@Valid` 等）

### 工具与规范

- [Lombok](https://projectlombok.org/) - 减少样板代码（`@Getter`、`@RequiredArgsConstructor` 等）
- [Spotless](https://github.com/diffplug/spotless) - 代码格式化工具
- [JaCoCo](https://www.jacoco.org/jacoco/) - 测试覆盖率工具
- [Flyway](https://flywaydb.org/) - 数据库版本管理

### 推荐阅读顺序（应届生）

1. **先跑通项目**：按照"快速开始"运行起来，测试 API
2. **看懂测试**：从 `CreateOrderServiceTest` 开始，理解 Mockito 和 AssertJ 的用法
3. **理解领域模型**：阅读 `Order.java` 和 `Pricing.java`，看懂价格计算逻辑
4. **学习分层架构**：追踪一个请求从 Controller → Service → Domain → Repository 的完整流程
5. **TDD 实践**：先写测试，再实现折扣功能
