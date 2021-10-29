package com.switchfully.eurder.services;

import com.switchfully.eurder.api.dto.items.CreateItemDTO;
import com.switchfully.eurder.api.dto.items.ItemDTO;
import com.switchfully.eurder.api.dto.orders.CreateOrderDTO;
import com.switchfully.eurder.api.dto.orders.CreateOrderlineDTO;
import com.switchfully.eurder.api.dto.users.CreateUserDTO;
import com.switchfully.eurder.api.dto.users.UserDTO;
import com.switchfully.eurder.api.mappers.*;
import com.switchfully.eurder.repositories.ItemRepository;
import com.switchfully.eurder.repositories.OrderRepository;
import com.switchfully.eurder.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportServiceTest {

    private OrderService orderService;
    private UserDTO user;
    private UUID item1UUID;
    private UUID item2UUID;
    private CreateOrderlineDTO createOrderlineDTO1;
    private CreateOrderlineDTO createOrderlineDTO2;
    private ReportService reportService;

    @BeforeEach
    void setUp() {
        CreateUserDTO userDTO = new CreateUserDTO("firstname", "lastname", "email@email.com", "address", "123456");
        CreateUserDTO adminDTO = new CreateUserDTO("admin", "lastname", "email@email.com", "address", "123456");
        UserRepository userRepository = new UserRepository();
        UserService userService = new UserService(new UserMapper(), userRepository);
        OrderRepository orderRepository = new OrderRepository();
        ItemRepository itemRepository = new ItemRepository();

        ItemService itemService = new ItemService(new ItemMapper(), userService, itemRepository);
        orderService = new OrderService(new OrderMapper(), new OrderlineMapper(), userService, itemService, orderRepository);
        userService = new UserService(new UserMapper(), userRepository);
        UserDTO admin = userService.createAdmin(adminDTO);
        user = userService.createUser(userDTO);

        CreateItemDTO createItemDTO = new CreateItemDTO.CreateItemDTOBuilder()
                .withName("My Item")
                .withDescription("Fancy item")
                .withPrice(BigDecimal.valueOf(10))
                .withAmountInStock(5)
                .build();

        CreateItemDTO createItemDTO2 = new CreateItemDTO.CreateItemDTOBuilder()
                .withName("My Item2")
                .withDescription("Fancy item")
                .withPrice(BigDecimal.valueOf(500))
                .withAmountInStock(10)
                .build();

        itemService = new ItemService(new ItemMapper(), userService, itemRepository);

        itemService.save(admin.getId(), createItemDTO);
        itemService.save(admin.getId(), createItemDTO2);

        Optional<ItemDTO> itemDTO1 = itemService.getAllItems(admin.getId()).stream().filter(item -> item.getName().equals("My Item")).findFirst();
        Optional<ItemDTO> itemDTO2 = itemService.getAllItems(admin.getId()).stream().filter(item -> item.getName().equals("My Item2")).findFirst();

        itemDTO1.ifPresent(itemDTO -> item1UUID = itemDTO.getId());

        itemDTO2.ifPresent(itemDTO -> item2UUID = itemDTO.getId());

        createOrderlineDTO1 = new CreateOrderlineDTO(item1UUID, 5);
        createOrderlineDTO2 = new CreateOrderlineDTO(item2UUID, 10);

        ReportMapper reportMapper = new ReportMapper();
        reportService = new ReportService(userService, reportMapper, orderService);
    }

    @Test
    @DisplayName("Total of all orders is correct")
    void whenMakingValidOrder_thenItemStockIsLowered() {

        CreateOrderDTO createOrderDTO = new CreateOrderDTO(List.of(createOrderlineDTO1, createOrderlineDTO2));
        CreateOrderDTO createOrderDTO2 = new CreateOrderDTO(List.of(createOrderlineDTO1));
        orderService.save(user.getId(), createOrderDTO);
        orderService.save(user.getId(), createOrderDTO2);

        BigDecimal expected = BigDecimal.valueOf(5100);

        BigDecimal result = reportService.getReportOfOrdersForCustomer(user.getId(), user.getId()).getTotalOfAllOrders();

        assertEquals(expected, result);
    }

}
