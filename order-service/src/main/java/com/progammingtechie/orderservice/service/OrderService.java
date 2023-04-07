package com.progammingtechie.orderservice.service;

import com.progammingtechie.orderservice.dto.OrderLineItemsDto;
import com.progammingtechie.orderservice.dto.OrderRequest;
import com.progammingtechie.orderservice.model.Order;
import com.progammingtechie.orderservice.model.OrderLineItems;
import com.progammingtechie.orderservice.repository.OrderRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient webClient;
    public void placeOrder(OrderRequest orderRequest) {
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList()
            .stream()
            .map(this::mapToDto)
            .toList();

        order.setOrderLineItemsList(orderLineItems);
//        Call Inventory Service, and place order if product is in
//        stock
        Boolean result = webClient.get()
            .uri("http://localhost:8082/api/inventory")
            .retrieve()
//            return
            .bodyToMono(Boolean.class)
//            return type
            .block();
//            make sync

        if (result) {
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException(
                "Product is not in stock, please try it again later");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setId(orderLineItemsDto.getId());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        return orderLineItems;
    }
}
