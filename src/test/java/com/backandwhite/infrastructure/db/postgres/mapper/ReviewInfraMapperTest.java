package com.backandwhite.infrastructure.db.postgres.mapper;

import static com.backandwhite.provider.ReviewProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.Review;
import com.backandwhite.infrastructure.db.postgres.entity.ReviewEntity;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ReviewInfraMapperTest {

    private final ReviewInfraMapper mapper = Mappers.getMapper(ReviewInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        Review d = mapper.toDomain(reviewEntity());
        assertThat(d.getId()).isEqualTo(REVIEW_ID);
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        assertThat(mapper.toDomainList(List.of(reviewEntity()))).hasSize(1);
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        ReviewEntity e = mapper.toEntity(review());
        assertThat(e.getId()).isEqualTo(REVIEW_ID);
    }
}
