package booking;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="reserves", path="reserves")
public interface ReserveRepository extends PagingAndSortingRepository<Reserve, Long>{


}
