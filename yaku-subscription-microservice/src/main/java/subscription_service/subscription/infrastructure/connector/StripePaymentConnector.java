package subscription_service.subscription.infrastructure.connector;

import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.domain.ports.ExternalPaymentGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class StripePaymentConnector implements ExternalPaymentGateway {

    @Value("sk_test_51TaSZKEyn9JTtfs5vSxxdR8xCTvPhqpsO26dNHk9C30Bejv8PNjHERtKy0Rh5s0Oe0MpOvg51bx9TV2SDPctQkQs00fUAIB4Nq")
    private String secretKey;

    @Value("https://front-opensource.web.app/payment-success")
    private String successUrl;

    @Value("https://front-opensource.web.app/payment-cancel")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    @Override
    public String generatePaymentIntent(Long amountInCents, String currency) {
        try {
            // Configura los parámetros requeridos por Stripe
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents) // Stripe procesa en centavos (ej: $20.00 = 2000)
                    .setCurrency(currency.toLowerCase()) // ej: "usd" o "pen"
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();

            // Llama a la API externa de Stripe
            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Retorna el client_secret que necesita tu Frontend (React/Flutter)
            return paymentIntent.getClientSecret();

        } catch (Exception e) {
            throw new RuntimeException("Error en la pasarela de pagos externa al generar el intento de cobro", e);
        }
    }

    @Override
    public String createCheckoutSession(Long userId, Plan plan) {
        try {
            SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                    .setSuccessUrl(successUrl)
                    .setCancelUrl(cancelUrl)
                    .setClientReferenceId(userId.toString())
                    .putMetadata("planId", plan.getId().toString());

            if (plan.getStripePriceId() != null && !plan.getStripePriceId().isEmpty()) {
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(plan.getStripePriceId())
                                .setQuantity(1L)
                                .build()
                );
            } else {
                // Fallback para precios dinámicos si no existe un Price ID en Stripe
                paramsBuilder.addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(plan.getCurrency().name().toLowerCase())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(plan.getName())
                                                                .build()
                                                )
                                                .setUnitAmount((long) (plan.getPrice() * 100))
                                                .setRecurring(
                                                        SessionCreateParams.LineItem.PriceData.Recurring.builder()
                                                                .setInterval(SessionCreateParams.LineItem.PriceData.Recurring.Interval.MONTH)
                                                                .build()
                                                )
                                                .build()
                                )
                                .setQuantity(1L)
                                .build()
                );
            }

            Session session = Session.create(paramsBuilder.build());
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Error al crear Checkout Session en Stripe", e);
        }
    }
}
