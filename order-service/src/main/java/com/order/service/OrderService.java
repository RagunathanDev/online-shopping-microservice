package com.order.service;

import com.order.bean.InventoryResponse;
import com.order.bean.OrderLineItemRequest;
import com.order.bean.OrderRequest;
import com.order.model.Order;
import com.order.model.OrderLineItems;
import com.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final WebClient.Builder webClientBuilder;

    public String placeOrder(OrderRequest orderRequest) {
        String response = "";
        try {
            Order order = new Order();
            order.setOrderNumber(UUID.randomUUID()
                    .toString());
            List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemRequest()
                    .stream()
                    .map(this::mapToModel)
                    .collect(Collectors.toList());
            order.setOrderLineItems(orderLineItemsList);
            log.info("final data  : {}", order);

            final var listOfSkuCodes = order.getOrderLineItems()
                    .stream()
                    .map(OrderLineItems::getSkuCode)
                    .collect(Collectors.toList());

            //Make Inventory-service API call to check product quantity
            InventoryResponse[] inventoryResponses = webClientBuilder.build()
                    .get()
                    .uri("http://inventory-service/api/inventory",
                            uriBuilder -> uriBuilder.queryParam("skuCode", listOfSkuCodes)
                                    .build())
                    .retrieve()
                    .bodyToMono(InventoryResponse[].class)
                    .block();

            if (inventoryResponses == null) throw new AssertionError();
            boolean allProductsInStock = Arrays.stream(inventoryResponses)
                    .allMatch(InventoryResponse::isInStock);

            if (allProductsInStock) {
                orderRepository.save(order);
                response = "Order placed successfully";
            } else response = "Product not in stock";

        } catch (Exception e) {
            log.error("{} : Exception while place order : ", orderRequest, e);
        }
        return response;
    }

    private OrderLineItems mapToModel(OrderLineItemRequest orderLineItemRequest) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemRequest.getSkuCode());
        orderLineItems.setPrice(orderLineItemRequest.getPrice());
        orderLineItems.setQuantity(orderLineItemRequest.getQuantity());
        return orderLineItems;
    }
}
