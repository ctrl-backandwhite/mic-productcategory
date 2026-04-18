package com.backandwhite.api.mapper;

import static com.backandwhite.provider.ReviewProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.api.dto.out.ReviewDtoOut;
import com.backandwhite.api.dto.out.ReviewStatsDtoOut;
import com.backandwhite.domain.model.Review;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ReviewApiMapperTest {

    private final ReviewApiMapper mapper = Mappers.getMapper(ReviewApiMapper.class);

    @Test
    void toDto_nullInput_returnsNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    void toDto_mapsFields() {
        ReviewDtoOut dto = mapper.toDto(review());
        assertThat(dto.getId()).isEqualTo(REVIEW_ID);
        assertThat(dto.getRating()).isEqualTo(REVIEW_RATING);
    }

    @Test
    void toDtoList_nullInput_returnsNull() {
        assertThat(mapper.toDtoList(null)).isNull();
    }

    @Test
    void toDtoList_mapsList() {
        List<ReviewDtoOut> list = mapper.toDtoList(List.of(review()));
        assertThat(list).hasSize(1);
    }

    @Test
    void toStatsDto_nullInput_returnsNull() {
        assertThat(mapper.toStatsDto(null)).isNull();
    }

    @Test
    void toStatsDto_mapsFields() {
        ReviewStatsDtoOut dto = mapper.toStatsDto(reviewStats());
        assertThat(dto.getAvgRating()).isEqualTo(4.5);
        assertThat(dto.getTotalCount()).isEqualTo(100L);
    }

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Review domain = mapper.toDomain(reviewDtoIn());
        assertThat(domain.getRating()).isEqualTo(REVIEW_RATING);
        assertThat(domain.getTitle()).isEqualTo(REVIEW_TITLE);
    }
}
