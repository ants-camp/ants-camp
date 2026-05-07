package io.antcamp.notificationservice.domain.exception;

import common.exception.BusinessException;
import common.exception.ErrorCode;

public class PrometheusMetricInvalidException extends BusinessException {
    public PrometheusMetricInvalidException() {
        super(ErrorCode.PROMETHEUS_METRIC_INVALID);
    }
}
