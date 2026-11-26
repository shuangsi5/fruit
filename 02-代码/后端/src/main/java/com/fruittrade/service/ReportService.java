package com.fruittrade.service;

import com.fruittrade.domain.vo.OverviewVO;
import com.fruittrade.domain.vo.SalesReportVO;

public interface ReportService {
    SalesReportVO sales(Long merchantId);
    OverviewVO overview();
}
