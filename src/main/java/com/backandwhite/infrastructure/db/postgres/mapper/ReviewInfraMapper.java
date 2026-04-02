package com.backandwhite.infrastructure.db.postgres.mapper;

import com.backandwhite.domain.model.Review;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewInfraMapper {

    Review toDomain(ReviewEntity entity);

    List<Review> toDomainList(List<ReviewEntity> entities);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ReviewEntity toEntity(Review domain);
}
