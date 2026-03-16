package com.technova.technov.domain.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.technova.technov.domain.service.PaypalCheckoutService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class PaypalCheckoutServiceImpl implements PaypalCheckoutService {

    @Value("${technova.paypal.client-id:}")
    private String clientId;

    @Value("${technova.paypal.client-secret:}")
    private String clientSecret;

    @Value("${technova.paypal.base-url:https://api-m.sandbox.paypal.com}")
    private String baseUrl;

    @Value("${technova.paypal.currency:USD}")
    private String currency;

    @Value("${technova.paypal.return-url:http://localhost:8080/checkout/paypal/retorno}")
    private String returnUrl;

    @Value("${technova.paypal.cancel-url:http://localhost:8080/checkout/paypal/retorno?cancel=true}")
    private String cancelUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    public boolean isConfigured() {
        return notBlank(clientId) && notBlank(clientSecret);
    }

    @Override
    public ApprovalData createOrder(String referenceCode, BigDecimal amount, String customerEmail) {
        if (!isConfigured()) {
            throw new IllegalStateException("PayPal no está configurado. Faltan client-id/client-secret.");
        }
        try {
            String accessToken = fetchAccessToken();
            BigDecimal normalized = amount.setScale(2, RoundingMode.HALF_UP);

            String currencyToUse = resolveCurrencyForSandbox();
            ObjectNode amountNode = objectMapper.createObjectNode()
                    .put("currency_code", currencyToUse)
                    .put("value", normalized.toPlainString());
            ObjectNode purchaseUnit = objectMapper.createObjectNode()
                    .put("reference_id", referenceCode)
                    .set("amount", amountNode);
            ArrayNode purchaseUnits = objectMapper.createArrayNode().add(purchaseUnit);
            ObjectNode appContext = objectMapper.createObjectNode()
                    .put("brand_name", "TechNova")
                    .put("user_action", "PAY_NOW")
                    .put("return_url", returnUrl)
                    .put("cancel_url", cancelUrl);
            ObjectNode payloadNode = objectMapper.createObjectNode();
            payloadNode.put("intent", "CAPTURE");
            payloadNode.set("purchase_units", purchaseUnits);
            payloadNode.set("application_context", appContext);

            String payload = payloadNode.toString();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Prefer", "return=representation")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Error creando orden PayPal: HTTP " + response.statusCode() + " - " + response.body());
            }

            JsonNode json = objectMapper.readTree(response.body());
            String orderId = asText(json, "id");
            String approvalUrl = "";
            JsonNode links = json.get("links");
            if (links != null && links.isArray()) {
                for (JsonNode link : links) {
                    if ("approve".equalsIgnoreCase(asText(link, "rel"))) {
                        approvalUrl = asText(link, "href");
                        break;
                    }
                }
            }

            if (!notBlank(orderId) || !notBlank(approvalUrl)) {
                throw new IllegalStateException("PayPal no devolvió orderId o approveUrl.");
            }
            return new ApprovalData(orderId, approvalUrl);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo crear orden en PayPal: " + e.getMessage(), e);
        }
    }

    @Override
    public CaptureResult captureOrder(String orderId) {
        if (!notBlank(orderId)) {
            return new CaptureResult(false, "INVALID_ORDER_ID", orderId, null);
        }
        if (!isConfigured()) {
            return new CaptureResult(false, "NOT_CONFIGURED", orderId, null);
        }
        try {
            String accessToken = fetchAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/v2/checkout/orders/" + URLEncoder.encode(orderId, StandardCharsets.UTF_8) + "/capture"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + accessToken)
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new CaptureResult(false, buildPaypalErrorStatus(response), orderId, null);
            }

            JsonNode json = objectMapper.readTree(response.body());
            String status = asText(json, "status");
            boolean completed = "COMPLETED".equalsIgnoreCase(status);

            String referenceCode = null;
            JsonNode units = json.get("purchase_units");
            if (units != null && units.isArray() && !units.isEmpty()) {
                referenceCode = asText(units.get(0), "reference_id");
            }

            return new CaptureResult(completed, status, orderId, referenceCode);
        } catch (Exception e) {
            return new CaptureResult(false, "ERROR", orderId, null);
        }
    }

    private String fetchAccessToken() throws Exception {
        String basic = java.util.Base64.getEncoder()
                .encodeToString((clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/oauth2/token"))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Basic " + basic)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("HTTP " + response.statusCode() + " al pedir token PayPal");
        }

        JsonNode json = objectMapper.readTree(response.body());
        String token = asText(json, "access_token");
        if (!notBlank(token)) {
            throw new IllegalStateException("PayPal no devolvió access_token");
        }
        return token;
    }

    private static String asText(JsonNode node, String field) {
        if (node == null || node.get(field) == null || node.get(field).isNull()) {
            return "";
        }
        return node.get(field).asText("");
    }

    private static boolean notBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveCurrencyForSandbox() {
        if (!notBlank(currency)) {
            return "USD";
        }
        String normalized = currency.trim().toUpperCase();
        // Algunos entornos sandbox no aceptan COP para órdenes REST; usar USD evita el 422.
        if ("COP".equals(normalized)) {
            return "USD";
        }
        return normalized;
    }

    private String buildPaypalErrorStatus(HttpResponse<String> response) {
        String status = "HTTP_" + response.statusCode();
        String body = response.body();
        if (!notBlank(body)) {
            return status;
        }
        try {
            JsonNode json = objectMapper.readTree(body);
            String issue = "";
            String description = "";
            JsonNode details = json.get("details");
            if (details != null && details.isArray() && !details.isEmpty()) {
                JsonNode first = details.get(0);
                issue = asText(first, "issue");
                description = asText(first, "description");
            }
            String name = asText(json, "name");

            StringBuilder sb = new StringBuilder(status);
            if (notBlank(name)) {
                sb.append(" - ").append(name);
            }
            if (notBlank(issue)) {
                sb.append(" - ").append(issue);
            }
            if (notBlank(description)) {
                sb.append(": ").append(description);
            }
            return sb.toString();
        } catch (Exception e) {
            return status;
        }
    }
}

