package com.example.cowmjucraft.domain.order.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.cowmjucraft.domain.order.repository.OrderViewTokenRepository;
import com.example.cowmjucraft.global.config.AppProperties;
import com.example.cowmjucraft.global.config.AppProperties.OrderViewTokenDelivery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderViewTokenUrlTest {

    @Mock
    private OrderViewTokenRepository orderViewTokenRepository;

    private OrderViewTokenService service(OrderViewTokenDelivery delivery, String baseUrl, String path) {
        AppProperties props = new AppProperties();
        props.setPublicBaseUrl(baseUrl);
        props.setOrderViewPath(path);
        props.setOrderViewTokenTtlMinutes(30);
        props.setOrderViewTokenDelivery(delivery);
        return new OrderViewTokenService(orderViewTokenRepository, props);
    }

    @Test
    void buildOrderViewUrl_기본설정_쿼리파라미터로토큰을싣는다() {
        // given
        OrderViewTokenService service = service(OrderViewTokenDelivery.QUERY, "https://mju-craft.shop", "/orders/view");

        // when
        String url = service.buildOrderViewUrl("raw-token");

        // then
        assertThat(url).isEqualTo("https://mju-craft.shop/orders/view?token=raw-token");
    }

    @Test
    void buildOrderViewUrl_FRAGMENT설정_해시프래그먼트로토큰을싣는다() {
        // given
        OrderViewTokenService service = service(OrderViewTokenDelivery.FRAGMENT, "https://mju-craft.shop", "/orders/view");

        // when
        String url = service.buildOrderViewUrl("raw-token");

        // then
        assertThat(url).isEqualTo("https://mju-craft.shop/orders/view#token=raw-token");
    }

    @Test
    void buildOrderViewUrl_기본값_설정을생략하면QUERY로동작한다() {
        // given
        AppProperties props = new AppProperties();
        props.setPublicBaseUrl("https://mju-craft.shop");
        props.setOrderViewPath("/orders/view");
        props.setOrderViewTokenTtlMinutes(30);

        // when
        String url = new OrderViewTokenService(orderViewTokenRepository, props).buildOrderViewUrl("raw-token");

        // then
        assertThat(props.getOrderViewTokenDelivery()).isEqualTo(OrderViewTokenDelivery.QUERY);
        assertThat(url).isEqualTo("https://mju-craft.shop/orders/view?token=raw-token");
    }

    @Test
    void buildOrderViewUrl_base끝슬래시와path앞슬래시누락_정규화한다() {
        // given
        OrderViewTokenService service = service(OrderViewTokenDelivery.FRAGMENT, "https://mju-craft.shop/", "orders/view");

        // when
        String url = service.buildOrderViewUrl("raw-token");

        // then
        assertThat(url).isEqualTo("https://mju-craft.shop/orders/view#token=raw-token");
    }
}
