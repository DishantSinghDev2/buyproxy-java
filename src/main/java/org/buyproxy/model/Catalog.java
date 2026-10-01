package org.buyproxy.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** The public product catalog with live prices. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Catalog(
        List<Product> products,
        VolumeBonus volumeBonus,
        List<PlanPrice> plans) {}
