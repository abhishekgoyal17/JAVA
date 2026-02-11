package LowLevelDesign.DesignBookMyShow.Service;

import LowLevelDesign.DesignBookMyShow.entities.Booking;
import LowLevelDesign.DesignBookMyShow.entities.Payment;
import LowLevelDesign.DesignBookMyShow.entities.Show;
import LowLevelDesign.DesignBookMyShow.entities.User;
import LowLevelDesign.DesignBookMyShow.Enums.PaymentStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BookingService {

    private final Map<UUID, Booking> bookings = new HashMap<>();


    public Booking book(User user, Show show, List<Integer> seats) {

        if (!show.lockSeats(seats)) {
            throw new RuntimeException("Seat unavailable");
        }

        //simulated payment flow here, we can invoke Pay method of Payment Controller
        Payment payment = new Payment(PaymentStatus.SUCCESS);

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            show.confirmSeats(seats);
            Booking booking = new Booking(user, show, seats, payment);
            bookings.put(booking.getBookingId(), booking);
            return booking;
        } else {
            show.releaseSeats(seats);
            throw new RuntimeException("Payment failed");
        }
    }

    public Booking getBooking(UUID bookingId) {
        return bookings.get(bookingId);
    }

    public List<Booking> getBookingsForUser(User user) {
        return bookings.values()
                .stream()
                .filter(b -> b.getUser().equals(user))
                .toList();
    }
}
