package LowLevelDesign.DesignBookMyShow.entities;

import LowLevelDesign.DesignBookMyShow.Enums.PaymentStatus;

import java.util.UUID;

public class Payment {

    private final UUID paymentId;
    private final PaymentStatus status;

    public Payment(PaymentStatus status) {
        this.paymentId = UUID.randomUUID();
        this.status = status;
    }

    public UUID getPaymentId() {
        return paymentId;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
