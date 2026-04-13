package com.backandwhite.infrastructure.client.cj.mapper;

import com.backandwhite.domain.model.Review;
import com.backandwhite.infrastructure.client.cj.dto.CjReviewItemDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface CjReviewMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", source = "pid")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "authorName", source = "reviewerName", qualifiedByName = "resolveAuthorName")
    @Mapping(target = "rating", source = "score")
    @Mapping(target = "title", ignore = true)
    @Mapping(target = "body", source = "reviewContent")
    @Mapping(target = "verified", source = "verifiedPurchase", qualifiedByName = "toVerified")
    @Mapping(target = "status", constant = "APPROVED")
    @Mapping(target = "helpfulCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", source = "reviewImages", qualifiedByName = "parseImages")
    @Mapping(target = "externalReviewId", source = "id")
    @Mapping(target = "source", constant = "CJ")
    @Mapping(target = "countryCode", source = "countryCode")
    Review toDomain(CjReviewItemDto dto);

    @Named("resolveAuthorName")
    default String resolveAuthorName(String name) {
        return (name != null && !name.isBlank()) ? name : "Anonymous";
    }

    @Named("toVerified")
    default Boolean toVerified(Boolean verified) {
        return Boolean.TRUE.equals(verified);
    }

    @Named("parseImages")
    default List<String> parseImages(String reviewImages) {
        if (reviewImages == null || reviewImages.isBlank())
            return new ArrayList<>();
        return Arrays.asList(reviewImages.split(","));
    }
}
