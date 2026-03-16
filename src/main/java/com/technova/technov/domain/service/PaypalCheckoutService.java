package com.technova.technov.domain.service;

import java.math.BigDecimal;

public interface PaypalCheckoutService {

    boolean isConfigured();

    ApprovalData createOrder(String referenceCode, BigDecimal amount, String customerEmail);

    CaptureResult captureOrder(String orderId);

    record ApprovalData(String orderId, String approvalUrl) {
    }

    record CaptureResult(boolean completed, String status, String orderId, String referenceCode) {
    }
}

