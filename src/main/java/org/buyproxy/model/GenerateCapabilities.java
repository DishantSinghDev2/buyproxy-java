package org.buyproxy.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;

/** What a plan's generate call accepts. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GenerateCapabilities(
        int maxCount,
        int maxStickyMinutes,
        List<String> protocols,
        Targeting targeting) {}
