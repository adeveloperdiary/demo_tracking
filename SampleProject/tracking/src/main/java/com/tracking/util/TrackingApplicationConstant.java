package com.tracking.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TrackingApplicationConstant {
    @Value("${app.error.db.save}")
    public String DB_SAVE_ERROR;

    @Value("${app.error.validation.input}")
    public String REQUEST_VALIDATION_ERROR = "Error validating input request";
}
