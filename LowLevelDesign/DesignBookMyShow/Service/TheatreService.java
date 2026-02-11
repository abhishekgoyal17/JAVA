package LowLevelDesign.DesignBookMyShow.Service;

import LowLevelDesign.DesignBookMyShow.entities.Movie;
import LowLevelDesign.DesignBookMyShow.entities.Screen;
import LowLevelDesign.DesignBookMyShow.entities.Show;
import LowLevelDesign.DesignBookMyShow.entities.Theatre;
import LowLevelDesign.DesignBookMyShow.Enums.City;

import java.time.LocalDate;
import java.util.*;

public class TheatreService {

    private final Map<City, List<Theatre>> cityTheatres = new HashMap<>();

    public void addTheatre(Theatre theatre) {
        cityTheatres
                .computeIfAbsent(theatre.getCity(), c -> new ArrayList<>())
                .add(theatre);
    }

    public Set<Movie> getMovies(City city, LocalDate date) {
        Set<Movie> movies = new HashSet<>();
        List<Theatre> theatres = cityTheatres.getOrDefault(city, List.of());

        for (Theatre theatre : theatres) {
            for (Screen screen : theatre.getScreens()) {
                for (Show show : screen.getShows(date)) {
                    movies.add(show.getMovie());
                }
            }
        }
        return movies;
    }

    public List<Theatre> getTheatres(City city, Movie movie, LocalDate date) {
        List<Theatre> theatres = cityTheatres.getOrDefault(city, List.of());

        return theatres.stream()
                .filter(t -> t.getScreens().stream()
                        .anyMatch(s -> s.getShows(date).stream()
                                .anyMatch(show -> show.getMovie().equals(movie))))
                .toList();
    }

    public List<Show> getShows(Movie movie, LocalDate date, Theatre theatre) {
        List<Show> result = new ArrayList<>();

        for (Screen screen : theatre.getScreens()) {
            for (Show show : screen.getShows(date)) {
                if (show.getMovie().equals(movie)) {
                    result.add(show);
                }
            }
        }
        return result;
    }
}
