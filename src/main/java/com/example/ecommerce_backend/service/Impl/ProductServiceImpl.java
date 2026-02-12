package com.example.ecommerce_backend.service.Impl;

import com.example.ecommerce_backend.dto.request.ProductPatchRequest;
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

import java.util.*;
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

        // Smart update of sizes
        updateProductSizes(product, request.getSizes());

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated);
    }

    @Override
    @Transactional
    public ProductResponse patch(Long id, ProductPatchRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Update only fields that are provided (not null)
        
        if (request.getName() != null) {
            product.setName(request.getName());
        }

        if (request.getSlug() != null) {
            // Check slug uniqueness if changed
            if (!product.getSlug().equals(request.getSlug()) && 
                productRepository.existsBySlug(request.getSlug())) {
                throw new RuntimeException("Product with slug '" + request.getSlug() + "' already exists");
            }
            product.setSlug(request.getSlug());
        }

        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }

        if (request.getOriginalPrice() != null) {
            product.setOriginalPrice(request.getOriginalPrice());
        }

        if (request.getDiscount() != null) {
            product.setDiscount(request.getDiscount());
        }

        if (request.getImage() != null) {
            product.setImage(request.getImage());
        }

        if (request.getAdditionalPhotos() != null) {
            product.setAdditionalPhotos(request.getAdditionalPhotos());
        }

        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            product.setCategory(category);
        }

        // Update sizes if provided
        if (request.getSizes() != null) {
            updateProductSizes(product, convertPatchSizesToRequestSizes(request.getSizes()));
        }

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated);
    }

    /**
     * Convert PatchRequest sizes to ProductRequest sizes for reuse
     */
    private List<ProductRequest.SizeRequest> convertPatchSizesToRequestSizes(
            List<ProductPatchRequest.SizeRequest> patchSizes) {
        return patchSizes.stream()
                .map(ps -> ProductRequest.SizeRequest.builder()
                        .size(ps.getSize())
                        .stock(ps.getStock())
                        .priceOverride(ps.getPriceOverride())
                        .sku(ps.getSku())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Smart update of product sizes:
     * - Updates existing sizes (by size name)
     * - Adds new sizes
     * - Removes sizes that are no longer in the request
     */
    private void updateProductSizes(Product product, List<ProductRequest.SizeRequest> newSizes) {
        // Get existing sizes
        List<ProductSize> existingSizes = new ArrayList<>(product.getSizes());
        
        // Create a map of existing sizes by size name for quick lookup
        Map<String, ProductSize> existingSizeMap = existingSizes.stream()
                .collect(Collectors.toMap(
                    ProductSize::getSize,
                    size -> size
                ));
        
        // Track which sizes are in the new request
        Set<String> newSizeNames = newSizes.stream()
                .map(ProductRequest.SizeRequest::getSize)
                .collect(Collectors.toSet());
        
        // Remove sizes that are no longer in the request
        Iterator<ProductSize> iterator = product.getSizes().iterator();
        while (iterator.hasNext()) {
            ProductSize existingSize = iterator.next();
            if (!newSizeNames.contains(existingSize.getSize())) {
                iterator.remove();
                existingSize.setProduct(null);
            }
        }
        
        // Update existing sizes or add new ones
        for (ProductRequest.SizeRequest sizeReq : newSizes) {
            ProductSize existingSize = existingSizeMap.get(sizeReq.getSize());
            
            if (existingSize != null) {
                // Update existing size
                existingSize.setStock(sizeReq.getStock());
                existingSize.setPriceOverride(sizeReq.getPriceOverride());
                existingSize.setSku(sizeReq.getSku());
            } else {
                // Add new size
                ProductSize newSize = ProductSize.builder()
                        .size(sizeReq.getSize())
                        .stock(sizeReq.getStock())
                        .priceOverride(sizeReq.getPriceOverride())
                        .sku(sizeReq.getSku())
                        .build();
                product.addSize(newSize);
            }
        }
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