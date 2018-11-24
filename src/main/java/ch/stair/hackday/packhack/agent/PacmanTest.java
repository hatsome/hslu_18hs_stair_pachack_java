package ch.stair.hackday.packhack.agent;

import static org.junit.Assert.*;

import ch.stair.hackday.packhack.agent.Pacman.GameMap;
import ch.stair.hackday.packhack.agent.Pacman.Location;
import java.util.Map;

public class PacmanTest {

    @org.junit.Test
    public void testSearch() {
        GameMap map = new GameMap(5,5);
        map.addWall(new Location(1, 3));
        map.addWall(new Location(2, 1));
        map.addWall(new Location(3, 1));
        map.addWall(new Location(3, 2));
        map.addWall(new Location(3, 3));

        map.addFood(new Location(1, 0));
        map.addFood(new Location(2, 0));
        map.addFood(new Location(3, 0));
        map.addFood(new Location(4, 0));
        map.addFood(new Location(4, 2));
        map.addFood(new Location(4, 3));

        Location start = new Location(0, 0);
        Location goal = new Location(4, 2);
        Map<Location, Location> origin = Pacman.search(map, start, goal);
        Location current = goal;
        while (current != null && !current.equals(start)) {
            System.out.println(current.toString());
            current = origin.get(current);
        }
    }
}