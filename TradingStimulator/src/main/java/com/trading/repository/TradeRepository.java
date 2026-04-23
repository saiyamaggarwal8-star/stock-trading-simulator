package com.trading.repository;

import com.trading.model.Trade;
import org.springframework.data.jpa.repository.JpaRepository;

import com.trading.model.User;
import java.util.List;

public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByBuyOrderUserOrSellOrderUserOrderByCreatedAtDesc(User user1, User user2);
}
