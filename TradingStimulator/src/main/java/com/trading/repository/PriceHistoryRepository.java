package com.trading.repository;

import com.trading.model.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    List<PriceHistory> findTop20BySymbolOrderByCreatedAtDesc(String symbol);
    List<PriceHistory> findTop100BySymbolOrderByCreatedAtDesc(String symbol);
    List<PriceHistory> findAllBySymbolOrderByCreatedAtAsc(String symbol);
    void deleteBySymbol(String symbol);
}
