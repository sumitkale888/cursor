package com.cursor.backend.service;

import com.cursor.backend.dto.InventoryTransactionResponse;
import com.cursor.backend.dto.InventoryUpdateRequest;
import com.cursor.backend.dto.PagedResponse;
import com.cursor.backend.entity.InventoryTransaction;
import com.cursor.backend.entity.InventoryTransactionType;
import com.cursor.backend.entity.Product;
import com.cursor.backend.exception.BadRequestException;
import com.cursor.backend.exception.ResourceNotFoundException;
import com.cursor.backend.mapper.EntityMapper;
import com.cursor.backend.repository.InventoryTransactionRepository;
import com.cursor.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Transactional
    public InventoryTransactionResponse updateStock(Long productId, InventoryUpdateRequest request, String performedBy) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        int previousStock = product.getStock();
        int newStock = previousStock + request.getQuantityChange();

        if (newStock < 0) {
            throw new BadRequestException("Stock cannot be negative");
        }

        if (request.getType() == InventoryTransactionType.RESTOCK && request.getQuantityChange() <= 0) {
            throw new BadRequestException("Restock quantity must be positive");
        }

        product.setStock(newStock);
        productRepository.save(product);

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .type(request.getType())
                .quantityChange(request.getQuantityChange())
                .previousStock(previousStock)
                .newStock(newStock)
                .reason(request.getReason())
                .performedBy(performedBy)
                .build();

        InventoryTransaction saved = inventoryTransactionRepository.save(transaction);
        log.info("Inventory updated for product id={}: {} -> {}", productId, previousStock, newStock);
        return entityMapper.toInventoryTransactionResponse(saved);
    }

    @Transactional
    public void recordSale(Product product, int quantitySold, int previousStock, String performedBy) {
        int newStock = previousStock - quantitySold;

        InventoryTransaction transaction = InventoryTransaction.builder()
                .product(product)
                .type(InventoryTransactionType.SALE)
                .quantityChange(-quantitySold)
                .previousStock(previousStock)
                .newStock(newStock)
                .reason("Order placement")
                .performedBy(performedBy)
                .build();

        inventoryTransactionRepository.save(transaction);
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionResponse> getProductTransactions(Long productId, Pageable pageable) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }
        Page<InventoryTransaction> page = inventoryTransactionRepository
                .findByProductIdOrderByCreatedAtDesc(productId, pageable);
        return PagedResponse.from(page.map(entityMapper::toInventoryTransactionResponse));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionResponse> getAllTransactions(Pageable pageable) {
        Page<InventoryTransaction> page = inventoryTransactionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return PagedResponse.from(page.map(entityMapper::toInventoryTransactionResponse));
    }
}
