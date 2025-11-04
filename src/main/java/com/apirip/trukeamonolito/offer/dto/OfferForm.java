package com.apirip.trukeamonolito.offer.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public record OfferForm(
        @NotNull Integer targetProductId,
        @NotEmpty List<@NotNull Integer> offeredProductIds
) {}
