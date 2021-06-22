package booking;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="lends", path="lends")
public interface LendRepository extends PagingAndSortingRepository<Lend, Long>{


}
