package an.example.wayfare.controllers;


import an.example.wayfare.dtos.request.TourPackageRequest;
import an.example.wayfare.dtos.response.TourPackageResponse;
import an.example.wayfare.models.TourPackage;
import an.example.wayfare.services.TourPackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourPackageController {

    @Autowired
    private TourPackageService tourPackageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public TourPackageResponse createTour(
            @Valid @ModelAttribute TourPackageRequest request
    ) {
        return tourPackageService.createTour(request);
    }

    // API lấy danh sách tour và filter
    @GetMapping
    public Page<TourPackageResponse> findAllTours(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer minDays,
            @RequestParam(required = false) Integer maxDays,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return tourPackageService.filterTours(
                keyword,
                location,
                minPrice,
                maxPrice,
                minDays,
                maxDays,
                isActive,
                page,
                size
        );
    }

}
