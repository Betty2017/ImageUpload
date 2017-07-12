package byAJ.repositories;

import byAJ.models.Photo;

import java.util.List;

import org.springframework.data.repository.CrudRepository;


public interface PhotoRepository extends CrudRepository<Photo, Long>{
	
	 List<Photo> findByUsername(String username);
}
