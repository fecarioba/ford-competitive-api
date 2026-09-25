package com.fordchallenge.ford_competitive_api.searches.repository;

import com.fordchallenge.ford_competitive_api.searches.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    List<SearchHistory> findAllByOrderByCreatedAtDesc();

    List<SearchHistory> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}