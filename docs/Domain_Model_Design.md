# Domain_Model_Design.md - 领域模型设计

> **需求来源**：`docs/Requirements.md` (XCB-H01)
> **AC 定义**：`docs/AC_Definition.md`
> **阶段**：Stage 2 - Design
> **标记**：[AWAITING_CONFIRMATION]

---

## 设计决策

### D1: Pricing 值对象扩展

**决策**：在 `Pricing` 中添加折扣相关字段，而非创建新方法

**理由**：
- `Pricing` 是不可变 `record`，需要添加 `discountAmount` 字段
- 保持 API 一致性，现有 getter 方法可直接复用
- 符合单一职责：价格计算逻辑内聚在 `Pricing` 中

### D2: OrderDiscount 值对象设计

**决策**：创建 `OrderDiscount` record，包含折扣金额和原因

**理由**：
- 值对象特征：无标识、不可变
- 单一实现（AGENTS.md 约束）：非策略模式
- 构造时校验：折扣金额不能大于门槛（防御性编程）

### D3: Order 聚合根修改

**决策**：在 `Order` 中添加 `discount` 字段，修改构造函数

**理由**：
- 折扣是订单的属性，需要持久化
- 通过构造函数注入，保持对象创建的一致性

---

## 1. OrderDiscount 值对象

### 文件位置
`app/src/main/java/com/example/demo/domain/order/OrderDiscount.java`

### 设计

```java
package com.example.demo.domain.order;

import java.math.BigDecimal;

/**
 * 订单折扣值对象。
 *
 * <p>表示订单可享受的折扣，包含折扣金额和折扣原因。
 * 折扣金额必须大于等于 0，且不能大于满减门槛。
 */
public record OrderDiscount(BigDecimal discountAmount, String discountReason) {
    /** 满减门槛 */
    public static final BigDecimal THRESHOLD = new BigDecimal("50.00");
    /** 满减金额 */
    public static final BigDecimal DISCOUNT_AMOUNT = new BigDecimal("5.00");

    public OrderDiscount {
        if (discountAmount == null) {
            throw new IllegalArgumentException("折扣金额不能为空");
        }
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("折扣金额不能为负");
        }
        if (discountAmount.compareTo(THRESHOLD) > 0) {
            throw new IllegalArgumentException("折扣金额不能大于满减门槛");
        }
        if (discountReason == null || discountReason.isBlank()) {
            throw new IllegalArgumentException("折扣原因不能为空");
        }
    }

    /**
     * 判断是否满足满减条件。
     *
     * @param itemsTotal 商品小计
     * @return 是否满足满减
     */
    public static boolean isEligible(BigDecimal itemsTotal) {
        return itemsTotal.compareTo(THRESHOLD) >= 0;
    }

    /**
     * 计算折扣金额。
     *
     * @param itemsTotal 商品小计
     * @return 折扣金额（如果不满足条件返回 0）
     */
    public static BigDecimal calculateDiscount(BigDecimal itemsTotal) {
        if (isEligible(itemsTotal)) {
            return DISCOUNT_AMOUNT;
        }
        return BigDecimal.ZERO;
    }
}
```

### 字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| discountAmount | BigDecimal | 折扣金额 |
| discountReason | String | 折扣原因（如"满50减5"） |

### 常量

| 常量 | 值 | 说明 |
|------|-----|------|
| THRESHOLD | 50.00 | 满减门槛 |
| DISCOUNT_AMOUNT | 5.00 | 满减金额 |

---

## 2. Pricing 值对象扩展

### 文件位置
`app/src/main/java/com/example/demo/domain/order/Pricing.java`（修改）

### 修改内容

```java
package com.example.demo.domain.order;

import java.math.BigDecimal;
import java.util.List;

/**
 * 订单价格计算结果。
 *
 * <p>包含商品小计、包装费、配送费、折扣金额和最终金额。
 */
public record Pricing(
        BigDecimal itemsTotal,
        BigDecimal packagingFee,
        BigDecimal deliveryFee,
        BigDecimal discountAmount,  // 新增：折扣金额
        BigDecimal finalAmount      // 新增：最终金额（含折扣）
) {
    public static final BigDecimal PACKAGING_FEE = new BigDecimal("1.00");
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

    /**
     * 计算无折扣的价格。
     *
     * @param items 订单项列表
     * @return 价格计算结果
     */
    public static Pricing calculate(List<OrderItem> items) {
        return calculateWithDiscount(items, BigDecimal.ZERO);
    }

    /**
     * 计算带折扣的价格。
     *
     * <p>计算公式：
     * 最终金额 = 商品小计 - 折扣金额 + 包装费 + 配送费
     *
     * @param items 订单项列表
     * @param discountAmount 折扣金额
     * @return 价格计算结果
     */
    public static Pricing calculateWithDiscount(List<OrderItem> items, BigDecimal discountAmount) {
        BigDecimal itemsTotal = items.stream()
                .map(OrderItem::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 折扣金额不能为负
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("折扣金额不能为负");
        }

        // 折扣金额不能超过商品小计
        if (discountAmount.compareTo(itemsTotal) > 0) {
            throw new IllegalArgumentException("折扣金额不能超过商品小计");
        }

        BigDecimal finalAmount = itemsTotal
                .subtract(discountAmount)
                .add(PACKAGING_FEE)
                .add(DELIVERY_FEE);

        return new Pricing(itemsTotal, PACKAGING_FEE, DELIVERY_FEE, discountAmount, finalAmount);
    }
}
```

### 新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| discountAmount | BigDecimal | 折扣金额 |
| finalAmount | BigDecimal | 最终金额（含折扣） |

### 方法签名变更

| 方法 | 输入 | 输出 | 说明 |
|------|------|------|------|
| `calculate(List<OrderItem>)` | items | Pricing | 现有方法，调用 `calculateWithDiscount(items, BigDecimal.ZERO)` |
| `calculateWithDiscount(List, BigDecimal)` | items, discountAmount | Pricing | 新增：带折扣计算 |

---

## 3. Order 聚合根修改

### 文件位置
`app/src/main/java/com/example/demo/domain/order/Order.java`（修改）

### 修改内容

```java
package com.example.demo.domain.order;

import com.example.demo.domain.Identities;
import com.example.demo.domain.merchant.MerchantId;
import com.example.demo.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

/**
 * 订单聚合根。
 *
 * <p>包含订单的所有业务逻辑和校验规则。
 */
@Getter
public class Order {
    private final OrderId id;
    private final OrderNumber orderNumber;
    private final UserId userId;
    private final MerchantId merchantId;
    private final List<OrderItem> items;
    private final DeliveryInfo deliveryInfo;
    private final String remark;
    private OrderStatus status;
    private final Pricing pricing;
    private final OrderDiscount discount;  // 新增：折扣信息
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * 创建新订单（无折扣）。
     */
    public Order(
            UserId userId,
            MerchantId merchantId,
            List<OrderItem> items,
            DeliveryInfo deliveryInfo,
            String remark) {
        this(userId, merchantId, items, deliveryInfo, remark, null);
    }

    /**
     * 创建新订单（带折扣）。
     *
     * @param userId 用户ID
     * @param merchantId 商家ID
     * @param items 订单项列表
     * @param deliveryInfo 配送信息
     * @param remark 备注
     * @param discount 折扣信息（可为空）
     */
    public Order(
            UserId userId,
            MerchantId merchantId,
            List<OrderItem> items,
            DeliveryInfo deliveryInfo,
            String remark,
            OrderDiscount discount) {
        // 现有校验逻辑...

        this.id = new OrderId(Identities.generateId());
        this.orderNumber = new OrderNumber();
        this.userId = userId;
        this.merchantId = merchantId;
        this.items = List.copyOf(items);
        this.deliveryInfo = deliveryInfo;
        this.remark = remark;
        this.status = OrderStatus.PENDING_PAYMENT;

        // 计算价格（带折扣）
        BigDecimal discountAmount = (discount != null)
                ? OrderDiscount.calculateDiscount(
                        items.stream()
                                .map(OrderItem::subtotal)
                                .reduce(BigDecimal.ZERO, BigDecimal::add))
                : BigDecimal.ZERO;
        this.pricing = Pricing.calculateWithDiscount(items, discountAmount);

        this.discount = discount;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // 现有重构构造函数保持不变，添加 discount 参数...
}
```

### 新增字段

| 字段 | 类型 | 说明 |
|------|------|------|
| discount | OrderDiscount | 折扣信息（可为空） |

---

## 4. ADR-01：为什么不用策略模式？

### 背景

在订单折扣场景中，通常有多种折扣类型：
- 满减折扣（当前需求）
- 百分比折扣（未来需求）
- 固定金额折扣（未来需求）

常见的做法是使用**策略模式**（Strategy Pattern）：
- 定义 `DiscountStrategy` 接口
- 实现多种折扣策略类
- 在订单创建时注入策略

### 决策：不使用策略模式

**理由 1：AGENTS.md 约束**

项目规范明确规定：
> 禁止策略模式/工厂模式（单一 `OrderDiscount` 实现）

这是来自需求方的显式约束，必须遵守。

**理由 2：当前需求简单**

当前需求只有一种折扣类型（满 50 减 5），复杂度低：
- 策略模式会增加代码复杂度
- 需要额外维护接口和实现类
- 引入工厂模式进一步增加复杂度

**理由 3：YAGNI 原则**

You Aren't Gonna Need It（你不会需要它）：
- 未来可能有其他折扣类型，但当前不需要
- 可以用简单的 `if-else` 或配置表扩展
- 等需求明确后再重构为策略模式不迟

**理由 4：保持代码可读性**

对于单一折扣实现：
- 代码路径清晰
- 易于理解和调试
- 便于 TDD 开发

### 结论

当前采用**单一值对象**方案：
- `OrderDiscount` 是 `record`，直接包含折扣数据
- `Pricing.calculateWithDiscount()` 包含折扣计算逻辑
- 未来需求明确后，可重构为策略模式

---

## 5. 包结构图

### 修改后的目录结构

```
app/src/main/java/com/example/demo/domain/order/
├── Order.java              # 聚合根（修改：添加 discount 字段）
├── OrderId.java            # 订单ID值对象（已有）
├── OrderNumber.java        # 订单号值对象（已有）
├── OrderItem.java          # 订单项值对象（已有，禁止修改）
├── OrderStatus.java        # 订单状态枚举（已有）
├── DeliveryInfo.java       # 配送信息值对象（已有）
├── Pricing.java            # 价格计算（修改：添加折扣字段和方法）
├── OrderDiscount.java      # 【新增】折扣值对象
└── OrderRepository.java   # 仓储接口（已有）
```

### 新增文件

| 文件 | 类型 | 说明 |
|------|------|------|
| `OrderDiscount.java` | record | 折扣值对象 |

### 修改文件

| 文件 | 修改内容 |
|------|----------|
| `Pricing.java` | 添加 `discountAmount`、`finalAmount` 字段和 `calculateWithDiscount` 方法 |
| `Order.java` | 添加 `discount` 字段和带折扣的构造函数 |

### Infrastructure 层（如果需要持久化）

```
app/src/main/java/com/example/demo/infrastructure/persistence/order/entity/
├── OrderEntity.java              # 已有
├── OrderItemEntity.java         # 已有
├── DeliveryInfoEmbeddable.java  # 已有
└── PricingEmbeddable.java      # 修改：添加 discountAmount 字段
```

---

## 待确认事项

- [x] OrderDiscount 使用 record（确认）
- [x] Pricing 添加 discountAmount 和 finalAmount 字段（确认）
- [x] Order 添加 discount 字段（确认）
- [x] 不使用策略模式（确认：ADR-01）
- [x] 不修改 OrderItem.java（确认）

---

> **下一步**：确认设计后，进入 Stage 3 - Red 阶段（编写失败测试）
