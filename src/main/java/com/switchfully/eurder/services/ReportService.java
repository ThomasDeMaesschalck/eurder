package com.switchfully.eurder.services;

import com.switchfully.eurder.api.dto.reports.OrderReportDTO;
import com.switchfully.eurder.api.dto.reports.OrderlineReportDTO;
import com.switchfully.eurder.api.dto.reports.OrdersReportForCustomerDTO;
import com.switchfully.eurder.api.mappers.ReportMapper;
import com.switchfully.eurder.domain.entities.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final UserService userService;
    private final ReportMapper reportMapper;
    private final OrderService orderService;

    @Autowired
    public ReportService(UserService userService, ReportMapper reportMapper, OrderService orderService) {
        this.userService = userService;
        this.reportMapper = reportMapper;
        this.orderService = orderService;
    }

    public OrdersReportForCustomerDTO getReportOfOrdersForCustomer(UUID userId, UUID customerId) {
        assertIdOfRetrieverIsSameAsCustomerIdOfOrders(userId, customerId);
        userService.assertUserId(customerId);

        List<OrderReportDTO> listOfAllOrdersFromCustomer = new ArrayList<>();

        orderService.getAllOrdersOfCustomer(customerId).forEach(order -> listOfAllOrdersFromCustomer.addAll(processIndividualOrder(order)));

        return reportMapper.toOrdersReportForCustomerDTO(listOfAllOrdersFromCustomer, calculateTotalOfAllCustomerOrders(listOfAllOrdersFromCustomer));
    }

    private List<OrderReportDTO> processIndividualOrder(Order order) {
        return new ArrayList<>(List.of(reportMapper.toOrderReportDTO(order.getId(), processOrderlinesOfIndividualOrder(order))));
    }

    private List<OrderlineReportDTO> processOrderlinesOfIndividualOrder(Order order) {
        return order.getOrderlines().stream()
                .map(reportMapper::toOrderlineReportDTO)
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalOfAllCustomerOrders(List<OrderReportDTO> orderReportDTOList) {
        return orderReportDTOList.stream()
                .map(OrderReportDTO::getOrderlineReportDTOList)
                .flatMap(List::stream)
                .map(OrderlineReportDTO::getOrderlineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void assertIdOfRetrieverIsSameAsCustomerIdOfOrders(UUID userId, UUID customerId) {
        if (!userId.equals(customerId)) {
            throw new IllegalArgumentException("You can't view report from another customer");
        }
    }

}
