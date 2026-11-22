package com.fruittrade.service;

import com.fruittrade.common.PageResult;
import com.fruittrade.domain.dto.ProductSaveDTO;
import com.fruittrade.domain.vo.ProductDetailVO;
import com.fruittrade.domain.vo.ProductVO;
import com.fruittrade.domain.vo.RecommendationVO;
import java.util.List;

public interface ProductService {
    PageResult<ProductVO> list(Long categoryId, String keyword, Long merchantId,
                               java.math.BigDecimal minPrice, java.math.BigDecimal maxPrice,
                               String sortBy, Long pageNum, Long pageSize);
    ProductDetailVO detail(Long id);
    List<RecommendationVO> related(Long id, int limit);
    PageResult<ProductVO> merchantList(Long merchantId, String keyword, Long pageNum, Long pageSize);
    Long create(Long merchantId, ProductSaveDTO dto);
    void update(Long merchantId, Long id, ProductSaveDTO dto);
    void offShelf(Long merchantId, Long id);
    List<ProductVO> expiring(Long merchantId, int days);
}
