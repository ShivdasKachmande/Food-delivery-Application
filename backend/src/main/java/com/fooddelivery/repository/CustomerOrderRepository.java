package com.fooddelivery.repository;

import com.fooddelivery.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findByCustomerEmailIgnoreCaseOrderByOrderDateDesc(
            String customerEmail);
}
