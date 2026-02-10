package com.example.ecommerce_backend.service.Impl;

import com.example.ecommerce_backend.dto.request.ProductRequest;
import com.example.ecommerce_backend.dto.response.ProductResponse;
import com.example.ecommerce_backend.entity.Category;
import com.example.ecommerce_backend.entity.Product;
import com.example.ecommerce_backend.entity.ProductSize;
import com.example.ecommerce_backend.repository.CategoryRepository;
import com.example.ecommerce_backend.repository.ProductRepository;
import com.example.ecommerce_backend.repository.ProductSizeRepository;
import com.example.ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        // Check if slug already exists
        if (productRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Product with slug '" + request.getSlug() + "' already exists");
        }

        // Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Create product
        Product product = Product.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .price(request.getPrice())
                .originalPrice(request.getOriginalPrice())
                .discount(request.getDiscount())
                .image(request.getImage())
                .additionalPhotos(request.getAdditionalPhotos())
                .description(request.getDescription())
                .category(category)
                .build();

        // Add sizes with stock
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            for (ProductRequest.SizeRequest sizeReq : request.getSizes()) {
                ProductSize size = ProductSize.builder()
                        .size(sizeReq.getSize())
                        .stock(sizeReq.getStock())
                        .priceOverride(sizeReq.getPriceOverride())
                        .sku(sizeReq.getSku())
                        .build();
                product.addSize(size);
            }
        }

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check slug uniqueness (if changed)
        if (!product.getSlug().equals(request.getSlug()) && 
            productRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Product with slug '" + request.getSlug() + "' already exists");
        }

        // Find category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Update basic fields
        product.setName(request.getName());
        product.setSlug(request.getSlug());
        product.setPrice(request.getPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setDiscount(request.getDiscount());
        product.setImage(request.getImage());
        product.setAdditionalPhotos(request.getAdditionalPhotos());
        product.setDescription(request.getDescription());
        product.setCategory(category);

        // Update sizes
        product.clearSizes();
        if (request.getSizes() != null && !request.getSizes().isEmpty()) {
            for (ProductRequest.SizeRequest sizeReq : request.getSizes()) {
                ProductSize size = ProductSize.builder()
                        .size(sizeReq.getSize())
                        .stock(sizeReq.getStock())
                        .priceOverride(sizeReq.getPriceOverride())
                        .sku(sizeReq.getSku())
                        .build();
                product.addSize(size);
            }
        }

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return ProductResponse.fromEntity(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable)
                .map(ProductResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(String categoryId, int page, int size) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategory(category, pageable)
                .map(ProductResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProductsByCategory(String categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        
        return productRepository.findByCategory(category).stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }
}