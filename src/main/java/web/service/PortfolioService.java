package web.service;

import java.util.List;

import org.springframework.data.domain.Example;

import web.model.Portfolio;

public interface PortfolioService {
	
	List<Portfolio> findAll();
	void save(Portfolio art);
	void update(Portfolio art);
	Portfolio findById(Integer id);
	void delete(Integer[] id);
	List<Portfolio> findByExample(Example<Portfolio> example);
}
