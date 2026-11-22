package com.fruittrade.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果封装：list / total / pageNum / pageSize / pages
 */
@Data
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<T> list;
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;

    public PageResult() {
        this.list = new ArrayList<>();
    }

    public PageResult(List<T> list, long total, long pageNum, long pageSize, long pages) {
        this.list = list;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = pages;
    }

    /** 将 MyBatis-Plus 分页对象转换为统一分页结果 */
    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(page.getRecords(), page.getTotal(),
                page.getCurrent(), page.getSize(), page.getPages());
    }

    public static <T> PageResult<T> of(List<T> list, long total, long pageNum, long pageSize) {
        long pages = pageSize <= 0 ? 0 : (total + pageSize - 1) / pageSize;
        return new PageResult<>(list, total, pageNum, pageSize, pages);
    }
}
