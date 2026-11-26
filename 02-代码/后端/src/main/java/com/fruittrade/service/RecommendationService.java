package com.fruittrade.service;

import com.fruittrade.domain.vo.RecommendationVO;
import java.util.List;

public interface RecommendationService {
    List<RecommendationVO> recommend(Long userId, String scene, int limit);
}
