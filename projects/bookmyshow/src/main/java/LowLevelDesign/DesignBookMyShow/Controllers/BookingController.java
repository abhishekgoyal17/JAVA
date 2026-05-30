/**
 * BookingController handles all HTTP-like operations for bookings in the BookMyShow system.
 * <p>
 * This controller acts as a facade to the BookingService, exposing methods to create and fetch bookings.
 * </p>
 */
package LowLevelDesign.DesignBookMyShow.Controllers;

import LowLevelDesign.DesignBookMyShow.entities.Booking;
import LowLevelDesign.DesignBookMyShow.entities.Show;
import LowLevelDesign.DesignBookMyShow.entities.User;
import LowLevelDesign.DesignBookMyShow.Service.BookingService;

import java.util.List;
import java.util.UUID;

/**
 * Controller for orchestrating booking-related actions.
 * Acts as the entry point for creating and retrieving bookings.
 */
public class BookingController {

    private final BookingService bookingService;

    public BookingController() {
        this.bookingService = new BookingService();
    }

    /**
     * Creates a booking for the given user, show, and selected seat numbers.
     *
     * @param user  the user making the booking
     * @param show  the show to be booked
     * @param seats the seat numbers to reserve
     * @return a Booking object representing the successful booking
     */
    public Booking createBooking(User user, Show show, List<Integer> seats) {
        Booking booking = bookingService.book(user, show, seats);
        return booking;
    }

    /**
     * Retrieves a booking by its unique booking ID.
     *
     * @param bookingId the unique identifier for a booking
     * @return the Booking with the given ID, or null if not found
     */
    public Booking getBooking(UUID bookingId) {
        return bookingService.getBooking(bookingId);
    }

    /**
     * Lists all bookings for the given user.
     *
     * @param user the user whose bookings are to be retrieved
     * @return list of Bookings for the user
     */
    public List<Booking> getBookingsForUser(User user) {
        return bookingService.getBookingsForUser(user);
    }
}
