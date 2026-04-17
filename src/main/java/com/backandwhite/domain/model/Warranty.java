package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.WarrantyType;
import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warranty {

    private String id;
    private String name;
    private WarrantyType type;
    private Integer durationMonths;
    private String coverage;
    private String conditions;
    private Boolean includesLabor;
    private Boolean includesParts;
    private Boolean includesPickup;
    private Integer repairLimit;
    private String contactPhone;
    private String contactEmail;
    private Boolean active;
    private Long productsCount;
    private Instant createdAt;
    private Instant updatedAt;
}
