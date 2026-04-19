package com.trading.repository;

import com.trading.model.Portfolio;
import com.trading.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByUser(User user);
}
