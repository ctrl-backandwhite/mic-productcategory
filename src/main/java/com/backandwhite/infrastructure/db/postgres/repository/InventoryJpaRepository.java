package com.backandwhite.infrastructure.db.postgres.repository;

import com.backandwhite.infrastructure.db.postgres.entity.ProductDetailVariantInventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryJpaRepository extends JpaRepository<ProductDetailVariantInventoryEntity, Long> {

    List<ProductDetailVariantInventoryEntity> findByVid(String vid);

    Optional<ProductDetailVariantInventoryEntity> findByVidAndCountryCode(String vid, String countryCode);

    @Modifying
    @Query("UPDATE ProductDetailVariantInventoryEntity i SET i.totalInventory = i.totalInventory - :qty WHERE i.vid = :vid AND i.countryCode = :country AND i.totalInventory >= :qty")
    int decrementStock(@Param("vid") String vid, @Param("country") String countryCode, @Param("qty") int quantity);

    @Modifying
    @Query("UPDATE ProductDetailVariantInventoryEntity i SET i.totalInventory = i.totalInventory + :qty WHERE i.vid = :vid AND i.countryCode = :country")
    int incrementStock(@Param("vid") String vid, @Param("country") String countryCode, @Param("qty") int quantity);

    @Query("SELECT COALESCE(SUM(i.totalInventory), 0) FROM ProductDetailVariantInventoryEntity i WHERE i.vid = :vid")
    int getTotalStockByVid(@Param("vid") String vid);
}
