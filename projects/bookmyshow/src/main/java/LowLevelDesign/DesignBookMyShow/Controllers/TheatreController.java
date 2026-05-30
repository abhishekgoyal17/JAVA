/**
 * TheatreController manages all user operations related to theatres for BookMyShow.
 * <p>
 * This controller exposes methods to add theatres, list movies, theatres, and shows based
 * on various filters such as city, movie, and date. Serves as the facade for TheatreService.
 * </p>
 */
package LowLevelDesign.DesignBookMyShow.Controllers;

import LowLevelDesign.DesignBookMyShow.entities.Movie;
import LowLevelDesign.DesignBookMyShow.entities.Screen;
import LowLevelDesign.DesignBookMyShow.entities.Show;
import LowLevelDesign.DesignBookMyShow.entities.Theatre;
import LowLevelDesign.DesignBookMyShow.Enums.City;
import LowLevelDesign.DesignBookMyShow.Service.TheatreService;

import java.time.LocalDate;
import java.util.*;

/**
 * Controller for theatre-related operations interfacing with TheatreService.
 */
public class TheatreController {

    private final TheatreService theatreService;

    public TheatreController() {
        this.theatreService = new TheatreService();
    }

    /**
     * Adds a new theatre to the system.
     *
     * @param theatre the Theatre object to add
     */
    public void addTheatre(Theatre theatre) {
        theatreService.addTheatre(theatre);
    }

    /**
     * Returns all movies available in a city on a specific date.
     *
     * @param city city to filter movies by
     * @param date date to filter movies by
     * @return a set of Movie objects
     */
    public Set<Movie> getMovies(City city, LocalDate date) {
        return theatreService.getMovies(city, date);
    }

    /**
     * Returns all theatres running a specified movie on a given date in a city.
     *
     * @param city city to filter theatres by
     * @param movie movie to filter theatres by
     * @param date date to filter theatres by
     * @return a list of Theatre objects
     */
    public List<Theatre> getTheatres(City city, Movie movie, LocalDate date) {
        return theatreService.getTheatres(city, movie, date);
    }

    /**
     * Returns all shows for a movie on a given date in a specified theatre.
     *
     * @param movie movie for which shows are queried
     * @param date show date
     * @param theatre theatre in which shows are running
     * @return a list of Show objects
     */
    public List<Show> getShows(Movie movie, LocalDate date, Theatre theatre) {
        return theatreService.getShows(movie, date, theatre);
    }
}
