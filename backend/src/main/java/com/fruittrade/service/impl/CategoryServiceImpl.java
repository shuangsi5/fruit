package com.fruittrade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fruittrade.domain.entity.Category;
import com.fruittrade.domain.vo.CategoryVO;
import com.fruittrade.mapper.CategoryMapper;
import com.fruittrade.service.CategoryService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分类服务实现（支持树形结构）。
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public List<CategoryVO> tree(Long parentId) {
        long root = parentId == null ? 0L : parentId;
        List<Category> all = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .eq(Category::getDeleted, 0)
                        .orderByAsc(Category::getSort));
        List<CategoryVO> roots = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(root))
                .map(this::toVO)
                .collect(Collectors.toList());
        for (CategoryVO vo : roots) {
            vo.setChildren(buildChildren(vo.getId(), all));
        }
        return roots;
    }

    private List<CategoryVO> buildChildren(Long parentId, List<Category> all) {
        List<CategoryVO> children = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parentId))
                .map(this::toVO)
                .collect(Collectors.toList());
        if (children.isEmpty()) {
            return new ArrayList<>();
        }
        for (CategoryVO vo : children) {
            vo.setChildren(buildChildren(vo.getId(), all));
        }
        return children;
    }

    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setParentId(c.getParentId());
        vo.setSort(c.getSort());
        vo.setIcon(c.getIcon());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
