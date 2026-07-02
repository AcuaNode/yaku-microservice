package subscription_service.subscription.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import subscription_service.subscription.domain.ports.ExternalPaymentGateway;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentIntentController {

    private final ExternalPaymentGateway externalPaymentGateway;

    public PaymentIntentController(ExternalPaymentGateway externalPaymentGateway) {
        this.externalPaymentGateway = externalPaymentGateway;
    }

    /**
     * Endpoint to test the Stripe Payment Intent generation via Swagger.
     */
    @PostMapping("/intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(
            @RequestParam Long amountInCents,
            @RequestParam(defaultValue = "usd") String currency) {

        try {
            // Llamamos a nuestro puerto, que en tiempo de ejecución será StripePaymentConnector
            String clientSecret = externalPaymentGateway.generatePaymentIntent(amountInCents, currency);
            
            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", clientSecret);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
