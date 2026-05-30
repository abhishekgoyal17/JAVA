package LowLevelDesign.DesignBookMyShow.entities;

import LowLevelDesign.DesignBookMyShow.Enums.City;
import LowLevelDesign.DesignBookMyShow.entities.Screen;

import java.util.List;

public class Theatre {

    private final String name;
    private final City city;
    private final List<Screen> screens;
    //address info etc.

    public Theatre(String name, City city, List<Screen> screens) {
        this.name = name;
        this.city = city;
        this.screens = screens;
    }

    public City getCity() {
        return city;
    }

    public String getName() {
        return name;
    }

    public List<Screen> getScreens() {
        return screens;
    }
}
