package com.example.demo.domain.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * OrderDiscount 单元测试。
 *
 * <p>测试满减折扣的计算逻辑。
 */
@DisplayName("OrderDiscount 值对象测试")
class OrderDiscountTest {

    @Test
    @DisplayName("场景1：订单金额满足满减条件时，应享受折扣")
    void should_apply_discount_when_threshold_met() {
        throw new UnsupportedOperationException("Red 阶段：测试尚未实现");
    }

    @Test
    @DisplayName("场景2：订单金额不满足满减条件时，不应享受折扣")
    void should_not_apply_discount_when_threshold_not_met() {
        throw new UnsupportedOperationException("Red 阶段：测试尚未实现");
    }

    @Test
    @DisplayName("场景3：订单金额刚好等于门槛时，应享受折扣（边界）")
    void should_apply_discount_when_equals_threshold() {
        throw new UnsupportedOperationException("Red 阶段：测试尚未实现");
    }

    @Test
    @DisplayName("场景4：非法折扣构造（折扣金额大于门槛）时，应抛出异常")
    void should_throw_exception_when_invalid_discount() {
        throw new UnsupportedOperationException("Red 阶段：测试尚未实现");
    }
}
