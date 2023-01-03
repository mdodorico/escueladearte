package web.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import web.model.Portfolio;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer>{

}
