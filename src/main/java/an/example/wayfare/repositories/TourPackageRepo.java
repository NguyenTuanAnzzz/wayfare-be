
package an.example.wayfare.repositories;

import an.example.wayfare.models.TourPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface TourPackageRepo extends JpaRepository<TourPackage, Long>, JpaSpecificationExecutor<TourPackage> {

    List<TourPackage> findByIsActiveTrue();


    Optional<TourPackage> findByIdAndIsActiveTrue(Long id);
}
