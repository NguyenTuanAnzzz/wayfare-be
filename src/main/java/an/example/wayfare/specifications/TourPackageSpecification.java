
package an.example.wayfare.specifications;

import an.example.wayfare.models.TourPackage;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public class TourPackageSpecification {

    public static Specification<TourPackage> maxPrice(
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            if (maxPrice == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("price"), maxPrice
            );
        };
    }

    public static Specification<TourPackage> minPrice(
            BigDecimal minPrice
    ) {
        return (root, query, cb) -> {
            if (minPrice == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("price"), minPrice
            );
        };
    }


    public static Specification<TourPackage> hasKeyword(
            String keyword
    ) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("name")),
                    "%" + keyword.trim().toLowerCase() + "%"
            );
        };
    }


    public static Specification<TourPackage> hasLocation(
            String location
    ) {
        return (root, query, cb) -> {
            if (location == null || location.isBlank()) {
                return cb.conjunction();
            }

            return cb.like(
                    cb.lower(root.get("location")),
                    "%" + location.trim().toLowerCase() + "%"
            );
        };
    }

    public static Specification<TourPackage> minDays(
            Integer minDays
    ) {
        return (root, query, cb) -> {
            if (minDays == null) {
                return cb.conjunction();
            }

            return cb.greaterThanOrEqualTo(
                    root.get("durationDays"), minDays
            );
        };
    }


    public static Specification<TourPackage> maxDays(
            Integer maxDays
    ) {
        return (root, query, cb) -> {
            if (maxDays == null) {
                return cb.conjunction();
            }

            return cb.lessThanOrEqualTo(
                    root.get("durationDays"), maxDays
            );
        };
    }


    public static Specification<TourPackage> hasActiveStatus(
            Boolean isActive
    ) {
        return (root, query, cb) -> {
            if (isActive == null) {
                return cb.conjunction();
            }

            return cb.equal(root.get("isActive"), isActive);
        };
    }
}
