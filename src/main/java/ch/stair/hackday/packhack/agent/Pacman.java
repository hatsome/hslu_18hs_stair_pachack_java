package ch.stair.hackday.packhack.agent;

import ch.stair.hackday.packhack.dto.Direction;
import ch.stair.hackday.packhack.dto.FieldTypes;
import ch.stair.hackday.packhack.dto.GameState;
import ch.stair.hackday.packhack.dto.PublicPlayer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class Pacman implements Agent {
    private boolean leftSide = false;
    private GameState gameState = null;
    private Location goal = null;
    private Location homebase = null;
    private boolean getFood = true;
    private boolean printed = true;
    private boolean enemyWeak = false;

    @Override
    public String getAgentInformation() {
        return "RedDuck";
    }

    @Override
    public Direction chooseAction(GameState gameState) {
        this.gameState = gameState;

        int playerIndex = gameState.getAgentIndex();
        PublicPlayer player = gameState.getPublicPlayers()[playerIndex];
        PublicPlayer enemy = gameState.getPublicPlayers()[(playerIndex+1) %2];
        GameMap map = createGameMap(gameState.getGameField(), player, enemy);
        Location playerLoc = getLocation(player.getPosition());

        if (homebase == null) {
            homebase = playerLoc;
            goal = playerLoc;
            if (playerIndex == 0) {
                leftSide = true;
            }
        }

        enemyWeak = enemy.isWeakened();
        chooseGoal(map, playerLoc);
        Map<Location, Location> origin = search(map, playerLoc, goal);

        if (!printed) {
            map.print(playerLoc, goal);
            printed = true;
        }

        return getMove(origin, playerLoc, goal);
    }

    private void chooseGoal(GameMap map, Location playerLoc) {
        if (playerLoc.equals(goal) && getFood) {
            List<Location> neighbours = map.neighbours(playerLoc);
            boolean foodNearBy = false;
            for (Location neighbour : neighbours) {
                if (map.getFood().contains(neighbour)) {
                    goal = neighbour;
                    foodNearBy = true;
                    break;
                }
            }

            if (!foodNearBy) {
                getFood = false;
                goal = homebase;
            }
        }

        if (leftSide) {
            chooseGoalLeft(map, playerLoc);
        }
        else {
            chooseGoalRight(map, playerLoc);
        }
    }

    private void chooseGoalLeft(GameMap map, Location playerLoc) {
        if (playerLoc.x < map.width /2 && !getFood) {
            for (Location loc : map.getFood()) {
                if (loc.x >= map.width / 2 && (!getFood || getFood && loc.x < goal.x)) {
                    goal = loc;
                    getFood = true;
                }
                if (!enemyWeak && loc.x >= map.width / 2 && map.getSpecial().contains(loc)) {
                    goal = loc;
                    getFood = true;
                    break;
                }
            }
        }
    }

    private void chooseGoalRight(GameMap map, Location playerLoc) {

        if (playerLoc.x > map.width /2 && !getFood) {
            for (Location loc : map.getFood()) {
                if (loc.x <= map.width / 2 && (!getFood || getFood && loc.x > goal.x)) {
                    goal = loc;
                    getFood = true;
                }
                if (!enemyWeak && loc.x <= map.width / 2 && map.getSpecial().contains(loc)) {
                    goal = loc;
                    getFood = true;
                    break;
                }
            }
        }
    }

    private Direction getMove(Map<Location, Location> origin, Location start, Location goal) {
        Location last = null;
        Location current = goal;
        while (current != null && !current.equals(start)) {
            last = current;
            current = origin.get(current);
        }

        if (start.x < last.x && start.y == last.y) {
            return Direction.EAST;
        } else if (start.x > last.x && start.y == last.y) {
            return Direction.WEST;
        } else if (start.y < last.y && start.x == last.x) {
            return Direction.NORTH;
        } else if (start.y > last.y && start.x == last.x) {
            return Direction.SOUTH;
        } else {
            return Direction.EAST;
        }
    }

    private Location getLocation(float[] pos) {
        return new Location((int)pos[0], (int)pos[1]);
    }

    private GameMap createGameMap(FieldTypes[][] fieldTypes, PublicPlayer player, PublicPlayer enemy) {
        GameMap map = new GameMap(fieldTypes[0].length, fieldTypes.length);
        for(int y = fieldTypes[0].length -1; y >= 0 ; y--) {
            for (int x = 0; x < fieldTypes.length; x++) {
                FieldTypes currentField = fieldTypes[x][y];
                switch (currentField) {
                    case WALL:
                        map.addWall(new Location(y, x));
                        break;
                    case CAPSULE:
                        map.addSpecial(new Location(y, x));
                    case FOOD:
                        map.addFood(new Location(y, x));
                        break;
                }
            }
        }
        if (enemy.isWeakened() || (enemy.getIsPacman() && !player.isWeakened())) {
            map.addFood(getLocation(enemy.getPosition()));
        }
        else if (!enemy.getIsPacman() || player.isWeakened()){
            map.addEnemy(getLocation(enemy.getPosition()));
            map.addWall(getLocation(enemy.getPosition()));
        }
        return map;
    }

    //A* https://www.redblobgames.com/pathfinding/a-star/introduction.html
    public static Map<Location, Location> search(GameMap map, Location start, Location goal) {
        Map<Location, Location> origin = new HashMap<>();
        Map<Location, Integer> cost = new HashMap<>();

        PriorityQueue<LocationPrio> frontier = new PriorityQueue<>();
        frontier.add(new LocationPrio(start, 0));
        origin.put(start,start);
        cost.put(start, 0);

        while (!frontier.isEmpty()) {
            Location current = frontier.poll().location;

            if (current.equals(goal)) {
                break;
            }

            for (Location neighbor : map.neighbours(current)) {
                int newCost = cost.get(current) + map.cost(current, neighbor);
                if (!cost.containsKey(neighbor) || newCost < cost.get(neighbor)) {
                    cost.put(neighbor, newCost);
                    int prio = newCost + heuristic(neighbor, goal);
                    frontier.add(new LocationPrio(neighbor, prio));
                    origin.put(neighbor, current);
                }
            }
        }
        return origin;
    }

    public static int heuristic(Location a, Location b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }

    public static class LocationPrio implements Comparable<LocationPrio>{
        private Location location;
        private int prio;

        public LocationPrio(Location location, int cost) {
            this.location = location;
            this.prio = cost;
        }

        public Location getLocation() {
            return location;
        }

        public int getPrio() {
            return prio;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LocationPrio that = (LocationPrio) o;
            return prio == that.prio &&
                Objects.equals(location, that.location);
        }

        @Override
        public int hashCode() {
            return Objects.hash(location, prio);
        }

        @Override
        public int compareTo(LocationPrio o) {
            return this.prio - o.getPrio();
        }
    }

    public static class Location {
        private int x, y;
        public Location(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Location location = (Location) o;
            return x == location.x &&
                y == location.y;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            return "Location{" +
                "x=" + x +
                ", y=" + y +
                '}';
        }
    }

    public static class GameMap {
        private int width, height;
        private HashSet<Location> walls;
        private HashSet<Location> food;
        private HashSet<Location> special;
        private HashSet<Location> enemy;
        private ArrayList<Location> directions;

        public GameMap(int width, int height) {
            this.width = width;
            this.height = height;
            this.walls = new HashSet<>();
            this.food = new HashSet<>();
            this.special = new HashSet<>();
            this.enemy = new HashSet<>();
            this.directions = new ArrayList<>();
            this.directions.add(new Location(-1, 0));
            this.directions.add(new Location(1, 0));
            this.directions.add(new Location(0, -1));
            this.directions.add(new Location(0, 1));
        }

        public boolean inBounds(Location id) {
            return 0 <= id.x && id.x < width && 0 <= id.y && id.y < height;
        }

        public int cost(Location a, Location b) {
            if (enemy.contains(b)) {
                return Integer.MAX_VALUE/4;
            }
            List<Location> neighbours = neighbours(b);
            for (Location neighbour : neighbours) {
                if (b.equals(neighbour) && enemy.contains(neighbour)) {
                    return Integer.MAX_VALUE/4-1000;
                }
            }
            return food.contains(b) ? 1 : 1000;
        }

        public List<Location> neighbours(Location id) {
            ArrayList<Location> neighbours = new ArrayList<>();
            for (Location dir : directions) {
                    Location current = new Location(id.x + dir.x, id.y + dir.y);
                    if (inBounds(current) && !walls.contains(current)) {
                        neighbours.add(current);
                    }
            }
            return neighbours;
        }

        public void addWall(Location location) {
            walls.add(location);
        }

        public void addFood(Location location) {
            food.add(location);
        }

        public void addSpecial(Location location) {
            special.add(location);
        }

        public void addEnemy(Location location) {
            enemy.add(location);
        }

        public final HashSet<Location> getFood() {
            return food;
        }

        public final HashSet<Location> getSpecial() {
            return special;
        }

        public void print(Location player, Location goal) {
            for (int y = height -1; y >= 0; y--) {
                for (int x = 0; x < width; x++) {
                    if (player.x == x && player.y == y) {
                       System.out.print("P ");
                    }
                    else if (goal.x == x && goal.y == y) {
                        System.out.print("G ");
                    }
                    else if (walls.contains(new Location(x, y))) {
                        System.out.print("X ");
                    } else {
                        System.out.print("  ");
                    }
                }
                System.out.println();
            }
        }
    }
}
