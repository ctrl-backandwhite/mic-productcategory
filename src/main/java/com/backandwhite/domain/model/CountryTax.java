package com.backandwhite.domain.model;

import com.backandwhite.domain.valueobject.TaxType;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.*;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CountryTax {

    private String id;
    private String countryCode;
    private String region;
    private BigDecimal rate;
    private TaxType type;
    private String appliesTo;
    private Boolean includesShipping;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}
