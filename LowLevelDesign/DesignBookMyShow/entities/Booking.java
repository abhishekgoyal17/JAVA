package LowLevelDesign.DesignBookMyShow.entities;

import LowLevelDesign.DesignBookMyShow.entities.User;
import LowLevelDesign.DesignBookMyShow.entities.Show;
import LowLevelDesign.DesignBookMyShow.entities.Payment;

import java.util.List;
import java.util.UUID;

public class Booking {

    private final UUID bookingId;
    private final User user;
    private final Show show;
    private final List<Integer> seats;
    private final Payment payment;


    public Booking(User user, Show show, List<Integer> seats, Payment payment) {
        this.bookingId = UUID.randomUUID();
        this.user = user;
        this.show = show;
        this.seats = seats;
        this.payment = payment;
    }

    public UUID getBookingId() {
        return bookingId;
    }

    public User getUser() {
        return user;
    }

    public Payment getPayment() {
        return payment;
    }
}
