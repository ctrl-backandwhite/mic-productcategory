package com.backandwhite.infrastructure.db.postgres.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.backandwhite.domain.model.ProductDetail;
import com.backandwhite.domain.model.ProductDetailTranslation;
import com.backandwhite.domain.model.ProductDetailVariant;
import com.backandwhite.domain.model.ProductDetailVariantInventory;
import com.backandwhite.domain.model.ProductDetailVariantTranslation;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailTranslationId;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantTranslationEntity;
import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantTranslationId;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductDetailInfraMapperTest {

    private final ProductDetailInfraMapper mapper = Mappers.getMapper(ProductDetailInfraMapper.class);

    @Test
    void toDomain_nullInput_returnsNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    void toDomain_mapsFields() {
        ProductDetailEntity entity = ProductDetailEntity.builder().pid("pid-1").build();
        ProductDetail d = mapper.toDomain(entity);
        assertThat(d.getPid()).isEqualTo("pid-1");
    }

    @Test
    void toDomainList_nullInput_returnsNull() {
        assertThat(mapper.toDomainList(null)).isNull();
    }

    @Test
    void toDomainList_mapsList() {
        ProductDetailEntity entity = ProductDetailEntity.builder().pid("pid-1").build();
        assertThat(mapper.toDomainList(List.of(entity))).hasSize(1);
    }

    @Test
    void toTranslationDomain_mapsFields() {
        ProductDetailTranslationEntity entity = ProductDetailTranslationEntity.builder()
                .id(new ProductDetailTranslationId("pid", "es")).build();
        ProductDetailTranslation d = mapper.toTranslationDomain(entity);
        assertThat(d.getLocale()).isEqualTo("es");
    }

    @Test
    void toTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toTranslationDomain(null)).isNull();
    }

    @Test
    void toVariantDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantDomain(null)).isNull();
    }

    @Test
    void toVariantDomain_mapsFields() {
        ProductDetailVariantEntity entity = ProductDetailVariantEntity.builder().vid("vid").pid("pid").build();
        ProductDetailVariant v = mapper.toVariantDomain(entity);
        assertThat(v.getVid()).isEqualTo("vid");
    }

    @Test
    void toVariantTranslationDomain_nullInput_returnsNull() {
        assertThat(mapper.toVariantTranslationDomain(null)).isNull();
    }

    @Test
    void toVariantTranslationDomain_mapsFields() {
        ProductDetailVariantTranslationEntity entity = ProductDetailVariantTranslationEntity.builder()
                .id(new ProductDetailVariantTranslationId("vid", "es")).build();
        ProductDetailVariantTranslation t = mapper.toVariantTranslationDomain(entity);
        assertThat(t.getLocale()).isEqualTo("es");
    }

    @Test
    void toInventoryDomain_nullInput_returnsNull() {
        assertThat(mapper.toInventoryDomain(null)).isNull();
    }

    @Test
    void toInventoryDomain_mapsFields() {
        ProductDetailVariantInventoryEntity entity = ProductDetailVariantInventoryEntity.builder().vid("vid")
                .countryCode("US").totalInventory(10).build();
        ProductDetailVariantInventory i = mapper.toInventoryDomain(entity);
        assertThat(i.getCountryCode()).isEqualTo("US");
    }

    @Test
    void toEntity_nullInput_returnsNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    void toEntity_mapsFields() {
        ProductDetail d = ProductDetail.builder().pid("pid-1").build();
        ProductDetailEntity e = mapper.toEntity(d);
        assertThat(e.getPid()).isEqualTo("pid-1");
    }

    @Test
    void toTranslationEntity_mapsFields() {
        ProductDetailTranslation t = ProductDetailTranslation.builder().locale("es").build();
        ProductDetailTranslationEntity e = mapper.toTranslationEntity(t, "pid-1");
        assertThat(e.getId().getPid()).isEqualTo("pid-1");
        assertThat(e.getId().getLocale()).isEqualTo("es");
    }

    @Test
    void toVariantEntity_nullInput_returnsNull() {
        assertThat(mapper.toVariantEntity(null)).isNull();
    }

    @Test
    void toVariantEntity_mapsFields() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").pid("pid").variantSku("SKU-1").build();
        ProductDetailVariantEntity e = mapper.toVariantEntity(v);
        assertThat(e.getVariantSku()).isEqualTo("SKU-1");
    }

    @Test
    void toVariantTranslationEntity_mapsFields() {
        ProductDetailVariantTranslation t = ProductDetailVariantTranslation.builder().locale("es").build();
        ProductDetailVariantTranslationEntity e = mapper.toVariantTranslationEntity(t, "vid");
        assertThat(e.getId().getVid()).isEqualTo("vid");
        assertThat(e.getId().getLocale()).isEqualTo("es");
    }

    @Test
    void toInventoryEntity_nullInput_returnsNull() {
        assertThat(mapper.toInventoryEntity(null)).isNull();
    }

    @Test
    void toInventoryEntity_mapsFields() {
        ProductDetailVariantInventory i = ProductDetailVariantInventory.builder().vid("vid").countryCode("US")
                .totalInventory(10).build();
        ProductDetailVariantInventoryEntity e = mapper.toInventoryEntity(i);
        assertThat(e.getCountryCode()).isEqualTo("US");
    }

    @Test
    void toInventoryEntityList_nullInput_returnsNull() {
        assertThat(mapper.toInventoryEntityList(null)).isNull();
    }

    @Test
    void toInventoryEntityList_mapsList() {
        ProductDetailVariantInventory i = ProductDetailVariantInventory.builder().vid("vid").countryCode("US").build();
        assertThat(mapper.toInventoryEntityList(List.of(i))).hasSize(1);
    }

    @Test
    void toEntityWithChildren_fullMapping() {
        ProductDetailVariantInventory inv = ProductDetailVariantInventory.builder().vid("vid").countryCode("US")
                .totalInventory(10).build();
        ProductDetailVariantTranslation vt = ProductDetailVariantTranslation.builder().locale("es").build();
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").pid("pid").variantSku("SKU-1")
                .translations(List.of(vt)).inventories(List.of(inv)).build();
        ProductDetailTranslation pt = ProductDetailTranslation.builder().locale("es").productName("X").build();
        ProductDetail d = ProductDetail.builder().pid("pid-1").productNameEn("EN").translations(List.of(pt))
                .variants(List.of(v)).build();

        ProductDetailEntity e = mapper.toEntityWithChildren(d);
        assertThat(e.getPid()).isEqualTo("pid-1");
        assertThat(e.getTranslations()).hasSize(1);
        assertThat(e.getVariants()).hasSize(1);
        assertThat(e.getVariants().getFirst().getTranslations()).hasSize(1);
        assertThat(e.getVariants().getFirst().getInventories()).hasSize(1);
    }

    @Test
    void toEntityWithChildren_nullLists_noChildren() {
        ProductDetail d = ProductDetail.builder().pid("pid-1").translations(null).variants(null).build();
        ProductDetailEntity e = mapper.toEntityWithChildren(d);
        assertThat(e.getTranslations()).isEmpty();
        assertThat(e.getVariants()).isEmpty();
    }

    @Test
    void toEntityWithChildren_variantNullSublists() {
        ProductDetailVariant v = ProductDetailVariant.builder().vid("vid").pid("pid-1").translations(null)
                .inventories(null).build();
        ProductDetail d = ProductDetail.builder().pid("pid-1").variants(List.of(v)).build();
        ProductDetailEntity e = mapper.toEntityWithChildren(d);
        assertThat(e.getVariants()).hasSize(1);
        assertThat(e.getVariants().getFirst().getTranslations()).isEmpty();
        assertThat(e.getVariants().getFirst().getInventories()).isEmpty();
    }
}
