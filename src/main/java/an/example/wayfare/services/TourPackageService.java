package an.example.wayfare.services;

import an.example.wayfare.dtos.request.TourPackageRequest;
import an.example.wayfare.dtos.response.TourImageResponse;
import an.example.wayfare.dtos.response.TourItineraryResponse;
import an.example.wayfare.dtos.response.TourPackageResponse;
import an.example.wayfare.exceptions.AppException;
import an.example.wayfare.models.TourImage;
import an.example.wayfare.models.TourItinerary;
import an.example.wayfare.models.TourPackage;
import an.example.wayfare.repositories.TourPackageRepo;
import an.example.wayfare.specifications.TourPackageSpecification;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TourPackageService {

    @Autowired
    private TourPackageRepo tourPackageRepo;

    @Autowired
    private CloudinaryService cloudinaryService;


    @Transactional
    public TourPackageResponse createTour(@Valid TourPackageRequest request) {

        if (request.getDurationDays() != request.getItineraries().size()) {
            throw new AppException(
                    "Số lịch trình phải bằng số ngày của tour",
                    400
            );
        }

        TourPackage tourPackage = new TourPackage();

        tourPackage.setName(request.getName());
        tourPackage.setDescription(request.getDescription());
        tourPackage.setDurationDays(request.getDurationDays());
        tourPackage.setDurationNights(request.getDurationNights());
        tourPackage.setPrice(request.getPrice());
        tourPackage.setLocation(request.getLocation());

        if(!request.getItineraries().isEmpty()){
            List<TourItinerary> tourItineraryList = request.getItineraries().stream().map(item ->{
                TourItinerary tourItinerary = new TourItinerary();
                tourItinerary.setDayNumber(item.getDayNumber());
                tourItinerary.setTitle(item.getTitle());
                tourItinerary.setDescription(item.getDescription());
                tourItinerary.setTourPackage(tourPackage);
                return tourItinerary;
            }).toList();
            tourPackage.setItineraries(tourItineraryList);
        }

        List<MultipartFile> files = request.getImages();

        if (files == null || files.isEmpty()
                || files.stream().allMatch(
                file -> file == null || file.isEmpty())) {
            throw new AppException(
                    "Vui lòng chọn ít nhất một ảnh",
                    400
            );
        }

        List<TourImage> tourImages = files.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(file -> {
                    String imageUrl = cloudinaryService.uploadAvatar(file);

                    TourImage image = new TourImage();
                    image.setImageUrl(imageUrl);
                    image.setTourPackage(tourPackage);
                    image.setIsThumbnail(false);
                    image.setIsActive(true);

                    return image;
                })
                .toList();

        // Ảnh đầu tiên làm ảnh đại diện
        tourImages.get(0).setIsThumbnail(true);
        tourPackage.setImages(tourImages);

        // Lưu tour và các entity con nhờ cascade
        TourPackage savedTour = tourPackageRepo.save(tourPackage);


        List<TourItineraryResponse> tourItinerariesResponse =
                savedTour.getItineraries()
                        .stream()
                        .map(item -> {
                            TourItineraryResponse response =
                                    new TourItineraryResponse();

                            response.setId(item.getId());
                            response.setDayNumber(item.getDayNumber());
                            response.setTitle(item.getTitle());
                            response.setDescription(item.getDescription());
                            response.setIsActive(item.getIsActive());

                            return response;
                        })
                        .toList();

        List<TourImageResponse> tourImagesResponse =
                savedTour.getImages()
                        .stream()
                        .map(item -> {
                            TourImageResponse response =
                                    new TourImageResponse();

                            response.setId(item.getId());
                            response.setImageUrl(item.getImageUrl());
                            response.setIsThumbnail(item.getIsThumbnail());
                            response.setIsActive(item.getIsActive());

                            return response;
                        })
                        .toList();

        return TourPackageResponse.builder()
                .id(savedTour.getId())
                .name(savedTour.getName())
                .description(savedTour.getDescription())
                .durationDays(savedTour.getDurationDays())
                .durationNights(savedTour.getDurationNights())
                .price(savedTour.getPrice())
                .location(savedTour.getLocation())
                .isActive(savedTour.getIsActive())
                .createdAt(savedTour.getCreatedAt())
                .updatedAt(savedTour.getUpdatedAt())
                .deactivatedAt(savedTour.getDeactivatedAt())
                .images(tourImagesResponse)
                .itineraries(tourItinerariesResponse)
                .build();

    }

    public Page<TourPackageResponse> filterTours(
            String keyword,
            String location,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Integer minDays,
            Integer maxDays,
            Boolean isActive,
            int page,
            int size
    ) {
        Specification<TourPackage> spec =
                (root, query, cb) -> cb.conjunction();

        spec = spec
                .and(TourPackageSpecification.hasKeyword(keyword))
                .and(TourPackageSpecification.hasLocation(location))
                .and(TourPackageSpecification.minPrice(minPrice))
                .and(TourPackageSpecification.maxPrice(maxPrice))
                .and(TourPackageSpecification.minDays(minDays))
                .and(TourPackageSpecification.maxDays(maxDays));

        if (isActive != null) {
            spec = spec.and(
                    TourPackageSpecification.hasActiveStatus(isActive)
            );
        }

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<TourPackage> tours =
                tourPackageRepo.findAll(spec, pageable);

        List<TourPackageResponse> responses = new ArrayList<>();

        for (TourPackage tour : tours.getContent()) {
            List<TourImageResponse> images = tour.getImages()
                    .stream()
                    .map(image -> TourImageResponse.builder()
                            .id(image.getId())
                            .imageUrl(image.getImageUrl())
                            .build())
                    .toList();
            TourPackageResponse response = TourPackageResponse.builder()
                    .id(tour.getId())
                    .name(tour.getName())
                    .description(tour.getDescription())
                    .durationDays(tour.getDurationDays())
                    .durationNights(tour.getDurationNights())
                    .price(tour.getPrice())
                    .location(tour.getLocation())
                    .isActive(tour.getIsActive())
                    .createdAt(tour.getCreatedAt())
                    .updatedAt(tour.getUpdatedAt())
                    .images(images)
                    .build();

            responses.add(response);
        }

        return new PageImpl<>(
                responses,
                pageable,
                tours.getTotalElements()
        );
    }
}
