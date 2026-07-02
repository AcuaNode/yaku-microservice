package subscription_service.subscription.domain.model.valueobjects;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public record SubscriptionPeriod(LocalDate startDate, LocalDate endDate) {
    public SubscriptionPeriod {
        if (startDate == null)
            throw new IllegalArgumentException("Start date cannot be null");
        if (endDate == null)
            throw new IllegalArgumentException("End date cannot be null");
        if (endDate.isBefore(startDate))
            throw new IllegalArgumentException("End date cannot be before start date");
    }

    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
}
