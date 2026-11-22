package com.fruittrade.service;

import com.fruittrade.domain.vo.CategoryVO;
import java.util.List;

public interface CategoryService {
    List<CategoryVO> tree(Long parentId);
}
