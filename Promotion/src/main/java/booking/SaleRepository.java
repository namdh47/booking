package booking;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource(collectionResourceRel="sales", path="sales")
public interface SaleRepository extends PagingAndSortingRepository<Sale, Long>{


}
