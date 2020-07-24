package com.tracking.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrackingErrorConstant {
    @Value("${app.error.db.save")
    public String DB_SAVE_ERROR = "Error Saving to Database";

    @Value("${app.error.validation.input}")
    public String REQUEST_VALIDATION_ERROR = "Error validating input request";

    @Value("${app.error.outbound.api}")
    public String INVOCATION_ERROR = "Error invoking outbound service";
}
