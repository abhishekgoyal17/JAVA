/**
 * Main application class for the BookMyShow low-level design simulation.
 * <p>
 * This file initializes movies, theatres, screens, shows, and users
 * to simulate the booking experience of a movie ticket system.
 * <br>
 * All interactions related to a sample user booking are demonstrated in the userFlow method.
 * </p>
 * 
 * @author (Auto-generated JavaDoc)
 */
package LowLevelDesign.DesignBookMyShow;

import LowLevelDesign.DesignBookMyShow.Controllers.BookingController;
import LowLevelDesign.DesignBookMyShow.Controllers.TheatreController;
import LowLevelDesign.DesignBookMyShow.entities.*;
import LowLevelDesign.DesignBookMyShow.Enums.City;
import LowLevelDesign.DesignBookMyShow.Enums.SeatCategory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

/**
 * BookMyShowApp is responsible for system startup, initialization,
 * and orchestrating the user booking flow for the sample "BookMyShow" low-level design.
 */
public class BookMyShowApp {

    private TheatreController theatreController;
    private BookingController bookingController;

    /**
     * Main entry point for the BookMyShow application.
     * <p>
     * Initializes the application and performs a sample user booking flow end-to-end.
     *
     * @param args Command-line arguments (not used)
     */
    public static void main(String[] args) {
        BookMyShowApp app = new BookMyShowApp();
        app.initialize();
        app.userFlow();
    }


    /**
     * Initializes controllers and populates initial data for movies, theatres, screens, and shows.
     */
    private void initialize() {
        theatreController = new TheatreController();
        bookingController = new BookingController();


        /*
         * 1. Create Movies
         */
        Movie baahubali = new Movie("BAAHUBALI");
        Movie avengers = new Movie("AVENGERS");


        /*
         * 2. Create Theatre -> Screen -> Seats
         */
        Screen inoxScreen1 = new Screen(1, createSeats());
        Theatre inoxTheatreBangalore = new Theatre(
                "INOX",
                City.BANGALORE,
                List.of(inoxScreen1)
        );

        Screen pvrScreen1 = new Screen(1, createSeats());
        Theatre pvrTheatreDelhi = new Theatre(
                "PVR",
                City.DELHI,
                List.of(pvrScreen1)
        );

        theatreController.addTheatre(inoxTheatreBangalore);
        theatreController.addTheatre(pvrTheatreDelhi);


        /*
         * 3. Create Shows
         */
        Show inoxMorningShowToday = new Show(
                baahubali,
                inoxScreen1,
                LocalDate.now(),
                LocalTime.of(8, 0)
        );

        Show inoxAfternoonShowToday = new Show(
                baahubali,
                inoxScreen1,
                LocalDate.now(),
                LocalTime.of(15, 0)
        );

        Show inoxEveningShowToday = new Show(
                avengers,
                inoxScreen1,
                LocalDate.now(),
                LocalTime.of(18, 0)
        );


        Show pvrMorningShowTomorrow = new Show(
                baahubali,
                pvrScreen1,
                LocalDate.now().plusDays(1),
                LocalTime.of(9, 0)
        );


        // Attach shows to screens
        inoxScreen1.addShow(inoxMorningShowToday);
        inoxScreen1.addShow(inoxAfternoonShowToday);
        inoxScreen1.addShow(inoxEveningShowToday);
        pvrScreen1.addShow(pvrMorningShowTomorrow);
    }

    /**
     * Simulates the complete user journey of booking a ticket in the system.
     * <p>
     * The flow covers login, city selection, movie selection, theatre and time selection,
     * seat selection, and booking confirmation with payment.
     */
    private void userFlow() {

        // User enters system
        User user = new User("U1", "Shrayansh");

        System.out.println("User logged in: Shrayansh");

        // 1. User selects city
        City selectedCity = City.BANGALORE;
        System.out.println("Selected City: " + selectedCity);

        // 2. for specific date, Show movies running in city
        LocalDate selectedDate = LocalDate.now();
        System.out.println("Selected Date: " + selectedDate);

        Set<Movie> movies = theatreController.getMovies(selectedCity, selectedDate);
        System.out.println("Movies available:");
        movies.forEach(m -> System.out.println(" - " + m.getName()));

        // 3. User selects movie
        Movie selectedMovie = movies.iterator().next(); //selecting first movie
        System.out.println("Selected Movie: " + selectedMovie.getName());


        // 4. Show theatres and show times in city
        List<Theatre> theatres = theatreController.getTheatres(selectedCity, selectedMovie, selectedDate);
        System.out.println("Theatres available:");
        theatres.forEach(t -> System.out.println(" - " + t.getName()));

        // 6. User selects theatre
        Theatre selectedTheatre = theatres.get(0);
        System.out.println("Selected Theatre: " + selectedTheatre.getName());

        // 7. Show running shows for movie + date + theatre
        List<Show> shows =
                theatreController.getShows(
                        selectedMovie,
                        selectedDate,
                        selectedTheatre
                );

        System.out.println("Shows available:");
        shows.forEach(s ->
                System.out.println(" - " + s.getStartTime())
        );

        // 8. User selects show
        Show selectedShow = shows.get(0);
        System.out.println("Selected Show Time: " + selectedShow.getStartTime());

        // 9. User selects seats
        List<Integer> selectedSeats = List.of(1, 2, 3);
        System.out.println("Selected Seats: " + selectedSeats);

        // 10. Booking + Payment
        Booking booking =
                bookingController.createBooking(
                        user,
                        selectedShow,
                        selectedSeats
                );

        System.out.println("BOOKING SUCCESSFUL");
        System.out.println("Booking ID: " + booking.getBookingId());
    }

    /**
     * Creates and returns a list of seats for a screen.
     *
     * @return List of Seat objects with SILVER category
     */
    private List<Seat> createSeats() {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            seats.add(new Seat(i, SeatCategory.SILVER));
        }
        return seats;
    }
}
