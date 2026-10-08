package com.badminton.inspection.dto.request;

import com.badminton.common.enums.*;
import jakarta.validation.constraints.*;
import java.time.*;

public record InspectionCreateRequest(@NotNull Long listingId, @Size(max = 2000) String note) {}
