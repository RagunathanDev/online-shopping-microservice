package com.inventory.service;

import com.inventory.bean.InventoryResponse;
import com.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {
    @Autowired
    private final InventoryRepository inventoryRepository;

    @Transactional(readOnly = true)
    public boolean isInStock(String skuCode) {
        log.info("check the skuCode : {}", skuCode);
        return inventoryRepository.findBySkuCode(skuCode)
                .isPresent();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> isInStockAll(List<String> skuCode) {
        log.info("isInStockAll() : Checking order availability..");
        return inventoryRepository.findBySkuCodeIn(skuCode)
                .stream()
                .map(inventory -> InventoryResponse.builder()
                        .skuCode(inventory.getSkuCode())
                        .isInStock(inventory.getQuantity() > 0)
                        .build())
                .collect(Collectors.toList());
    }
}
