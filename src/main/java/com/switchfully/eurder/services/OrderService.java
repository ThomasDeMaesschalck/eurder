package com.switchfully.eurder.services;

import com.switchfully.eurder.api.dto.orders.CreateOrderDTO;
import com.switchfully.eurder.api.dto.orders.CreateOrderlineDTO;
import com.switchfully.eurder.api.dto.orders.OrderDTO;
import com.switchfully.eurder.api.mappers.OrderMapper;
import com.switchfully.eurder.api.mappers.OrderlineMapper;
import com.switchfully.eurder.domain.entities.Item;
import com.switchfully.eurder.domain.entities.Order;
import com.switchfully.eurder.domain.entities.Orderline;
import com.switchfully.eurder.repositories.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final int IN_STOCK_SHIPPING_DAYS = 1;
    private static final int BACKORDERED_SHIPPING_DAYS = 7;

    private final OrderMapper orderMapper;
    private final OrderlineMapper orderlineMapper;
    private final UserService userService;
    private final ItemService itemService;
    private final OrderRepository orderRepository;

    @Autowired
    public OrderService(OrderMapper orderMapper, OrderlineMapper orderlineMapper, UserService userService, ItemService itemService, OrderRepository orderRepository) {
        this.orderMapper = orderMapper;
        this.orderlineMapper = orderlineMapper;
        this.userService = userService;
        this.itemService = itemService;
        this.orderRepository = orderRepository;
    }

    public OrderDTO save(UUID userId, CreateOrderDTO createOrderDTO) {
        userService.assertUserId(userId);

        Order order = processOrder(createOrderDTO, userId);

        return orderMapper.toDTO(orderRepository.save(order));
    }

    private Order processOrder(CreateOrderDTO createOrderDTO, UUID userId) {

        createOrderDTO.getOrderlines().forEach(this::assertOrderlineIdAndOrderedAmount);

        Order order = orderMapper.toEntity(userId);

        createOrderDTO.getOrderlines().forEach(orderline -> order.addOrderline(processOrderline(orderline)));

        return order;
    }

    private Orderline processOrderline(CreateOrderlineDTO orderline) {

        Item item = itemService.getById(orderline.getItemId());

        item.takeItemsFromStock(orderline.getAmount());

        LocalDate shippingDate = calculateShippingDate(orderline.getAmount(), item.getAmountInStock());

        return orderlineMapper.toEntity(item, orderline.getAmount(), shippingDate);
    }

    private LocalDate calculateShippingDate(int amountOrdered, int amountInStock) {
        if (amountInStock >= amountOrdered) {
            return LocalDate.now().plusDays(IN_STOCK_SHIPPING_DAYS);
        } else {
            return LocalDate.now().plusDays(BACKORDERED_SHIPPING_DAYS);
        }
    }

    private void assertOrderlineIdAndOrderedAmount(CreateOrderlineDTO orderline) {
        itemService.assertItemId(orderline.getItemId());
        assertOrderlineAmountGreaterThanZero(orderline.getAmount());
    }

    private void assertOrderlineAmountGreaterThanZero(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("You can't order 0 or less of an item.");
        }
    }

    protected List<Order> getAllOrdersOfCustomer(UUID customerId) {
        return orderRepository.getOrders().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .collect(Collectors.toList());
    }

}
