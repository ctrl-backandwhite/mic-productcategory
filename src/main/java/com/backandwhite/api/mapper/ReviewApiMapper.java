package com.backandwhite.api.mapper;

import com.backandwhite.api.dto.in.ReviewDtoIn;
import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.domain.model.Review;
import com.backandwhite.domain.model.ReviewStats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewApiMapper {

    ReviewDtoOut toDto(Review review);

    List<ReviewDtoOut> toDtoList(List<Review> reviews);

    ReviewStatsDtoOut toStatsDto(ReviewStats stats);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "verified", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "helpfulCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Review toDomain(ReviewDtoIn dto);
}
