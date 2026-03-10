# AGENTS.md - HoCATLing Project Guidelines

> **[PROJECT_RULES_GENERATED]**
> 本文件整合了个人开发规范 (rule.txt) 与项目技术要求 (Clean Architecture)。

---

## 语言约束（置顶）

- **所有代码注释**：中文 Javadoc
- **异常信息**：中文（`IllegalArgumentException` 消息）
- **测试用例名称**：英文 snake_case
- **提交物文档**：中文 Markdown

---

## 1. 需求澄清优先

- 需求存在歧义时，先提出澄清问题，确认后再编码
- 每阶段完成需显式确认后再进行下一步
- **业务需求文档**：`docs/Requirements.md`
- **AC 定义输出**：`docs/AC_Definition.md`（Given-When-Then 格式）

## 2. 测试先行（TDD）

- 实现代码前，先编写失败的测试用例
- 发现 Bug 时，先写测试重现，再修复
- 测试位置：`src/test/java/`（单元测试）、`src/integrationTest/java/`（集成测试）
- 运行命令：`./gradlew test --tests "ClassName.methodName"`

## 3. 小步提交（3文件原则）

- 同时修改超过 3 个文件时，拆分为独立子任务
- 每阶段完成即提交版本控制
- 使用功能分支：`feature/discount`（当前任务）

## 4. 核心业务独立

- **Domain 层禁止使用**：`@Entity`、`@Autowired`
- **值对象**：使用 `record`（如 `OrderDiscount`）
- **依赖倒置**：Repository 接口在 Domain 层定义，Infrastructure 层实现
- 简单场景避免过度设计，保护现有代码
- **【订单折扣约束】**：
  - 禁止修改 `OrderItem.java`（README 红线）
  - 禁止策略模式/工厂模式（单一 `OrderDiscount` 实现）
  - `OrderDiscount` 必须是 `record`（禁止 `@Entity`）

## 5. 防御式编程

- 校验输入合法性（使用 `@Valid`、`ConstraintViolationException`）
- 异常信息明确说明原因（中文）
- **数值计算**：使用 `BigDecimal`，字符串构造（如 `new BigDecimal("50.00")`），禁止 `double`/`float`

## 6. 代码可读性

- **函数级注释**：中文 Javadoc 说明用途与约束
- 命名见名知意
- 禁止魔术数字（使用常量，如 `PACKAGING_FEE`）

## 7. 规则迭代

- 被纠正时，确认是否固化为新规则，并更新本文档

## 8. 失败停止原则

- 构建失败、测试未通过时，立即修复，不得继续下一步
- 运行验证：`./gradlew build`

## 9. 质量门禁

- 测试覆盖率不低于 70%（JaCoCo）
- 代码风格检查通过（Spotless）
- 所有测试套件通过：unit、integration、contract

---

## 项目技术规范

### 构建命令

```bash
./gradlew bootRun                              # 运行应用（H2）
./gradlew bootRun --args='--spring.profiles.active=mysql'  # MySQL
./gradlew spotlessApply                        # 格式化代码
./gradlew spotlessCheck                        # 检查格式
./gradlew test                                 # 单元测试
./gradlew test --tests "ClassName.methodName"   # 单个测试
./gradlew integrationTest                      # 集成测试
./gradlew contractTest                         # 契约测试
./gradlew build                                 # 完整构建（70%+ 覆盖率）
```

### 架构（Clean Architecture）

```
web/           → Controllers, DTOs, input validation
application/   → Services, orchestration, transactions
domain/        → Entities, Value Objects, domain logic, repository interfaces
infrastructure/→ Repository implementations, persistence entities
```

### Domain 层规范

- **值对象**：`record`（如 `OrderId`, `Pricing`, `OrderDiscount`）
- **实体**：充血模型，业务逻辑在构造函数中（如 `Order` 校验）
- **校验**：`IllegalArgumentException`（中文消息）
- **禁止**：`@Entity`、`@Autowired`

### 类型规范

- 金额：`BigDecimal`（字符串构造 `"50.00"`，禁止 `double`）
- 时间：`Instant`
- 列表防御拷贝：`List.copyOf()`

### 测试规范

- 单元测试：`@ExtendWith(MockitoExtension.class)`
- 断言：AssertJ（`assertThat()`，`isEqualByComparingTo()` for BigDecimal）
- 异常测试：`assertThatThrownBy()`

### 关键约束

| 约束项 | 要求 |
|--------|------|
| Java 版本 | 21 |
| 格式化 | Palantir Java Format |
| 最低覆盖率 | 70% |
| 测试框架 | JUnit 5 + AssertJ + Mockito |

---

## 当前任务：订单折扣功能

- **需求**：`docs/Requirements.md` (XCB-H01)
- **AC 输出**：`docs/AC_Definition.md`
- **设计输出**：`docs/Domain_Model_Design.md`
- **分支**：`feature/discount`
