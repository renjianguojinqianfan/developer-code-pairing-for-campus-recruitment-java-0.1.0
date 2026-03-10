# Skill: 规范编码流程 (Spec-Coding Workflow)

## 概述

本技能定义从需求澄清到代码实现的完整工作流程，确保需求理解准确、代码质量达标。

## 四阶段流程

```
┌─────────────────────────────────────────────────────────────┐
│  Stage 1: Clarify (需求澄清)                                │
│  - 理解需求背景与业务目标                                    │
│  - 识别模糊点并提出澄清问题                                   │
│  - 输出 AC 定义（Given-When-Then）                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Stage 2: Design (领域设计)                                  │
│  - 设计领域模型（值对象/实体/聚合）                           │
│  - 确定包结构和依赖关系                                       │
│  - 输出 Domain_Model_Design.md                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Stage 3: Red (红灯 - 失败测试)                              │
│  - 编写失败的测试用例                                        │
│  - 测试必须反映 AC 定义                                      │
│  - 运行测试确认失败                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│  Stage 4: Green (绿灯 - 实现通过)                            │
│  - 实现功能让测试通过                                        │
│  - 保持最小改动原则                                          │
│  - 运行测试确认通过                                          │
└─────────────────────────────────────────────────────────────┘
```

---

## Stage 1: Clarify - 需求澄清

### 输入
- `docs/Requirements.md` 中的需求描述
- 业务背景信息

### 输出
- `docs/AC_Definition.md`（Given-When-Then 格式）

### 操作步骤

#### 1.1 阅读需求
- 理解用户故事（User Story）
- 识别关键业务实体
- 标记不确定的点

#### 1.2 识别模糊点
提出澄清问题，例如：
- 折扣的计算顺序是什么？
- 订单项数和折扣有上限吗？
- 折扣信息需要持久化吗？

#### 1.3 编写 AC 定义
使用 Given-When-Then 格式：

```markdown
### AC: 满减优惠计算

#### 场景 1: 订单金额满足满减条件
- **Given**: 订单菜品总金额为 60.00 元，满减规则为满 50 减 5
- **When**: 计算订单最终金额
- **Then**: 最终金额 = 60.00 - 5 = 55.00 元

#### 场景 2: 订单金额不满足满减条件
- **Given**: 订单菜品总金额为 40.00 元，满减规则为满 50 减 5
- **When**: 计算订单最终金额
- **Then**: 最终金额 = 40.00 元（不享受折扣）

#### 场景 3: 多档位满减取最优
- **Given**: 订单菜品总金额为 120.00 元，满减规则为满 50 减 5 / 满 100 减 15
- **When**: 计算订单最终金额
- **Then**: 最终金额 = 120.00 - 15 = 105.00 元
```

#### 1.4 确认 AC
- 与需求方确认 AC 定义
- 确保没有遗漏边界情况
- **只有 AC 确认后才能进入下一阶段**

---

## Stage 2: Design - 领域设计

### 输入
- 确认的 AC 定义
- 项目现有领域模型（参考 `Order.java`, `Pricing.java`）

### 输出
- `docs/Domain_Model_Design.md`

### 操作步骤

#### 2.1 确定对象类型
决策：值对象 vs 实体

| 特征 | 值对象 | 实体 |
|------|--------|------|
| 标识 | 无唯一标识 | 有唯一标识 |
| 不可变 | 不可变 | 可变 |
| 例子 | `OrderDiscount`, `Money` | `Order`, `User` |

#### 2.2 设计包结构
```
domain/order/
├── Order.java           # 订单聚合根（已有）
├── OrderItem.java       # 订单项（已有，禁止修改）
├── Pricing.java         # 价格计算（已有，需扩展）
└── OrderDiscount.java   # 新增：折扣值对象（单一实现，禁止策略模式）
```

#### 2.3 定义类
为每个新类编写设计说明：

```markdown
### OrderDiscount (值对象)

**职责**: 表示订单可享受的折扣

**字段**:
- `discountAmount: BigDecimal` - 折扣金额
- `discountReason: String` - 折扣原因（如"满50减5"）

**构造约束**:
- 折扣金额必须 >= 0
- 折扣原因不能为空
```

#### 2.4 确认设计
- 检查是否符合 Clean Architecture
- 确认 Domain 层无外部依赖
- **只有设计确认后才能进入下一阶段**

---

## Stage 3: Red - 编写失败测试

### 输入
- 确认的 AC 定义
- 确认的领域设计

### 输出
- 失败的单元测试

### 操作步骤

#### 3.1 创建测试文件
```java
// src/test/java/com/example/demo/domain/order/PricingWithDiscountTest.java
@ExtendWith(MockitoExtension.class)
class PricingWithDiscountTest {
    // ...
}
```

#### 3.2 编写测试用例
每个 AC 场景对应一个测试方法：

```java
@Test
void calculate_final_amount_with_discount_should_apply_when_threshold_met() {
    // Given: 订单菜品总金额为 60.00 元
    List<OrderItem> items = List.of(
        new OrderItem(new DishId("dish-001"), "宫保鸡丁", 2, new BigDecimal("30.00"))
    );
    
    // When: 使用满 50 减 5 的折扣
    OrderDiscount discount = new OrderDiscount(new BigDecimal("5.00"), "满50减5");
    Pricing pricing = Pricing.calculateWithDiscount(items, discount);
    
    // Then: 最终金额 = 60.00 - 5 + 包装费 + 配送费
    assertThat(pricing.finalAmount()).isEqualByComparingTo(new BigDecimal("59.00"));
}
```

#### 3.3 运行测试确认失败
```bash
./gradlew test --tests "PricingWithDiscountTest"
```

**预期结果**: 测试失败（红色），因为功能尚未实现

#### 3.4 检查清单
- [ ] 每个 AC 场景都有对应测试
- [ ] 测试使用 BigDecimal 字符串构造
- [ ] 测试命名反映业务场景
- [ ] 测试确实失败

---

## Stage 4: Green - 实现功能

### 输入
- 失败的测试用例
- 确认的领域设计

### 输出
- 通过的测试用例

### 操作步骤

#### 4.1 实现最小改动
按照以下优先级实现：

1. **Domain 层** - 核心业务逻辑
   - 在 `Pricing.java` 中添加折扣计算逻辑
   - 创建 `OrderDiscount` 值对象（单一 record）
   - 使用 `IllegalArgumentException`（中文消息）进行校验
   - **禁止使用策略模式/工厂模式**

2. **Application 层** - 服务编排
   - 修改 `CreateOrderService` 传递折扣信息

3. **Web 层** - API 适配
   - 修改 Controller 的 Request/Response

4. **Infrastructure 层** - 持久化（如需要）
   - 添加 JPA 实体映射

#### 4.2 运行测试确认通过
```bash
./gradlew test --tests "PricingWithDiscountTest"
```

**预期结果**: 测试通过（绿色）

#### 4.3 检查清单
- [ ] 所有测试通过
- [ ] 格式化检查通过：`./gradlew spotlessCheck`
- [ ] 无类型错误（无 `as any`、`@ts-ignore`）
- [ ] 注释使用中文 Javadoc
- [ ] `OrderDiscount` 是 `record`（不是 `@Entity`）
- [ ] 未修改 `OrderItem.java`

---

## 质量检查点

| 阶段 | 检查项 | 工具 |
|------|--------|------|
| Clarify | AC 使用 Given-When-Then | 人工审查 |
| Design | Domain 层无 @Entity/@Autowired | 代码审查 |
| Red | 测试反映 AC | 测试覆盖率 |
| Green | 测试通过 | `./gradlew test` |
| 任何阶段 | 格式化 | `./gradlew spotlessCheck` |
| 任何阶段 | 构建 | `./gradlew build` |

## 回滚规则

如果在任何阶段发现问题：
1. 停止当前阶段
2. 回滚到上一个确认点
3. 修复问题后重新该阶段

---

## 触发条件

本技能在以下情况触发：
- 开始新的功能开发
- 需求存在模糊点需要澄清
- 需要编写 AC 定义文档
