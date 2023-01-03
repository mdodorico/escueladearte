package web.service.imp;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import web.model.Portfolio;
import web.repository.PortfolioRepository;
import web.service.PortfolioService;

@Service
public class PortfolioServiceImpl implements PortfolioService{
	
	@Autowired
    private PortfolioRepository portfolioRepository;

	@Override
	public List<Portfolio> findAll() {
		return portfolioRepository.findAll();
	}
	
	@Override
	public Portfolio findById(Integer id) {
		Optional<Portfolio> optional = portfolioRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	@Override
	public void save(Portfolio art) {
		portfolioRepository.save(art);
	}
	
	@Override
	public void update(Portfolio art) {
		deleteImage(art.getId());
		portfolioRepository.save(art);
	}

	@Override
	public void delete(Integer[] id) {
		for(Integer idArt: id) {
			deleteImage(idArt);
			portfolioRepository.deleteById(idArt);
		}
	}
	
	@Transactional
    private void deleteImage(Integer id) {
		Portfolio art = findById(id);
		
		try { 
			Files.deleteIfExists(Paths.get("c:/arte/images/portfolio/" + art.getImage()));
		} catch (NoSuchFileException e) {
		    System.out.println("No existe el directorio o archivo");
		} catch (DirectoryNotEmptyException e) {
		    System.out.println("El directorio no está vacío");
		} catch (IOException e) {
		    System.out.println("No tiene permisos para realizar esta operación");
		}
		 
		System.out.println("La imagen fue eliminada");
	 }

	@Override
	public List<Portfolio> findByExample(Example<Portfolio> example) {
		return portfolioRepository.findAll(example);
	}
}
