package com.group6.clothingstore.product.service;

import com.group6.clothingstore.order.repository.OrderDetailRepository;
import com.group6.clothingstore.product.dto.ProductFormDto;
import com.group6.clothingstore.product.entity.Category;
import com.group6.clothingstore.product.entity.Product;
import com.group6.clothingstore.product.repository.CategoryRepository;
import com.group6.clothingstore.product.repository.ProductRepository;
import com.group6.clothingstore.shared.enums.ProductStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductManageService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(Integer categoryId, ProductStatus status, String keyword) {
        Specification<Product> spec = (root, query, cb) -> cb.conjunction();

        if (categoryId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("category").get("categoryId"), categoryId));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }

        if (keyword != null && !keyword.isBlank()) {
            String kw = "%" + keyword.strip().toLowerCase(Locale.ROOT) + "%";
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("productName")), kw));
        }

        return productRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "productId"));
    }

    @Transactional(readOnly = true)
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm có ID: " + id));
    }

    @Transactional
    public Product createProduct(ProductFormDto dto) {
        // ERR-10: Giá bán phải lớn hơn 0 và số lượng tồn kho không được là số âm
        validatePriceAndStock(dto.getPrice(), dto.getStockQuantity());

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục được chọn không hợp lệ!"));

        String sizes = (dto.getSizes() != null && !dto.getSizes().isEmpty())
                ? String.join(",", dto.getSizes())
                : "S,M,L,XL";

        ProductStatus initialStatus = dto.getStatus();
        if (initialStatus == null) {
            initialStatus = (dto.getStockQuantity() == 0) ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE;
        }

        Product product = Product.builder()
                .productName(dto.getProductName().trim())
                .category(category)
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .availableSizes(sizes)
                .color(dto.getColor().trim())
                .material(dto.getMaterial() != null ? dto.getMaterial().trim() : null)
                .gender(dto.getGender())
                .description(dto.getDescription() != null ? dto.getDescription().trim() : null)
                .imageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null)
                .status(initialStatus)
                .build();

        Product saved = productRepository.save(product);
        log.info("Admin tạo sản phẩm mới: id={}, name={}", saved.getProductId(), saved.getProductName());
        return saved;
    }

    @Transactional
    public Product updateProduct(Long id, ProductFormDto dto) {
        validatePriceAndStock(dto.getPrice(), dto.getStockQuantity());

        Product product = getProductById(id);

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục được chọn không hợp lệ!"));

        String sizes = (dto.getSizes() != null && !dto.getSizes().isEmpty())
                ? String.join(",", dto.getSizes())
                : product.getAvailableSizes();

        product.setProductName(dto.getProductName().trim());
        product.setCategory(category);
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setAvailableSizes(sizes);
        product.setColor(dto.getColor().trim());
        product.setMaterial(dto.getMaterial() != null ? dto.getMaterial().trim() : null);
        product.setGender(dto.getGender());
        product.setDescription(dto.getDescription() != null ? dto.getDescription().trim() : null);
        product.setImageUrl(dto.getImageUrl() != null ? dto.getImageUrl().trim() : null);

        if (dto.getStatus() != null) {
            product.setStatus(dto.getStatus());
        } else if (dto.getStockQuantity() == 0 && product.getStatus() == ProductStatus.ACTIVE) {
            product.setStatus(ProductStatus.OUT_OF_STOCK);
        } else if (dto.getStockQuantity() > 0 && product.getStatus() == ProductStatus.OUT_OF_STOCK) {
            product.setStatus(ProductStatus.ACTIVE);
        }

        log.info("Admin cập nhật sản phẩm: id={}, name={}", id, product.getProductName());
        return productRepository.save(product);
    }

    /**
     * FR09: Ẩn / Ngừng kinh doanh mẫu (ACTIVE -> INACTIVE và ngược lại)
     */
    @Transactional
    public void toggleProductStatus(Long id) {
        Product product = getProductById(id);
        if (product.getStatus() == ProductStatus.INACTIVE) {
            product.setStatus(product.getStockQuantity() == 0 ? ProductStatus.OUT_OF_STOCK : ProductStatus.ACTIVE);
        } else {
            product.setStatus(ProductStatus.INACTIVE);
        }
        productRepository.save(product);
        log.info("Admin thay đổi trạng thái sản phẩm: id={}, status={}", id, product.getStatus());
    }

    /**
     * BR05: Không được xóa cứng sản phẩm đã từng phát sinh trong đơn hàng; chỉ chuyển sang INACTIVE.
     * @return true nếu đã xóa cứng, false nếu đã chuyển sang INACTIVE do đã có đơn hàng
     */
    @Transactional
    public boolean deleteOrDeactivateProduct(Long id) {
        Product product = getProductById(id);

        if (orderDetailRepository.existsByProductProductId(id)) {
            product.setStatus(ProductStatus.INACTIVE);
            productRepository.save(product);
            log.info("Sản phẩm id={} đã có trong đơn hàng, chuyển trạng thái sang INACTIVE thay vì xóa cứng (BR05)", id);
            return false;
        }

        productRepository.delete(product);
        log.info("Admin xóa cứng sản phẩm id={}", id);
        return true;
    }

    private void validatePriceAndStock(BigDecimal price, Integer stockQuantity) {
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0 và số lượng tồn kho không được là số âm!");
        }
        if (stockQuantity == null || stockQuantity < 0) {
            throw new IllegalArgumentException("Giá bán phải lớn hơn 0 và số lượng tồn kho không được là số âm!");
        }
    }
}
