package com.buaisociety.pacman;

import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.maze.Maze;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.function.Predicate;
import com.buaisociety.pacman.maze.SearchResult;

public class Searcher {

    /**
     * Stores the result of the BFS search, including the found tile, distance, and weight score.
     */
    

    /**
     * Performs a BFS search in each of the four directions to find tiles matching the predicate.
     *
     * @param startTile The starting tile for the BFS.
     * @param predicate The predicate to test each tile.
     * @return A Map containing the first matching tile, distance, and weighted score for each direction.
     */
    public static Map<Direction, SearchResult> findTileInAllDirections(@NotNull Tile startTile, @NotNull Predicate<Tile> predicate, @NotNull Maze maze) {
        Map<Direction, SearchResult> results = new EnumMap<>(Direction.class);
    
        for (Direction direction : Direction.values()) {
            SearchResult result = findTileWithBFS(startTile, predicate, direction, maze); // Add maze argument here
            if (result != null) {
                results.put(direction, result);
            }
        }
    
        return results;
    }

    /**
     * Performs a BFS to find the closest tile in the specified direction that matches the predicate,
     * prioritizing tiles based on weighted score.
     *
     * @param startTile The starting tile for the BFS.
     * @param predicate The predicate to test each tile.
     * @param initialDirection The initial direction for the search.
     * @return The SearchResult containing the tile, distance, and weighted score, or null if no matching tile is found.
     */
    private static SearchResult findTileWithBFS(@NotNull Tile startTile, @NotNull Predicate<Tile> predicate, @NotNull Direction direction, Maze maze) {
        Queue<Vector2ic> queue = new ArrayDeque<>();
        Queue<Integer> distances = new ArrayDeque<>();
        Set<Vector2ic> visited = new HashSet<>();
    
        Vector2i startPosition = new Vector2i(startTile.getPosition()).add(getDirectionOffset(direction));
        queue.add(startPosition);
        visited.add(startPosition);
        distances.add(1);
    
        SearchResult closestPowerPellet = null;
        int closestPowerPelletDistance = Integer.MAX_VALUE;
    
        while (!queue.isEmpty()) {
            Vector2ic currentPos = queue.poll();
            int currentDistance = distances.poll();
            
            Vector2i wrappedPos = wrapPosition(new Vector2i(currentPos), (Vector2i) maze.getDimensions());
            Tile currentTile = maze.getTile(wrappedPos);
    
            if (predicate.test(currentTile)) {
                if (currentTile.getState() == TileState.POWER_PELLET) {
                    if (currentDistance < closestPowerPelletDistance) {
                        closestPowerPellet = new SearchResult(wrappedPos, currentDistance);
                        closestPowerPelletDistance = currentDistance;
                    }
                } else if (closestPowerPellet == null) {
                    return new SearchResult(wrappedPos, currentDistance);
                }
            }
    
            for (Vector2i neighbor : getNeighborsInDirection(wrappedPos, direction)) {
                Vector2i wrappedNeighbor = wrapPosition(neighbor, (Vector2i) maze.getDimensions());
                if (visited.contains(wrappedNeighbor)) continue;
    
                Tile neighborTile = maze.getTile(wrappedNeighbor);
                if (neighborTile == null || neighborTile.getState() == TileState.WALL) continue;
    
                visited.add(wrappedNeighbor);
                queue.add(wrappedNeighbor);
                distances.add(currentDistance + 1);
            }
        }
    
        return closestPowerPellet != null ? closestPowerPellet : null;
    }
    
    
    /**
     * Wraps a position based on the maze’s dimensions, ensuring grid wrap-around.
     */
    private static Vector2i wrapPosition(Vector2i position, Vector2i dimensions) {
        int x = (position.x + dimensions.x) % dimensions.x;
        int y = (position.y + dimensions.y) % dimensions.y;
        return new Vector2i(x, y);
    }
   
    /**
     * Calculates an initial weight score for a tile based on its distance.
     * Closer tiles get higher scores, making them more attractive options.
     *
     * @param distance The distance to the tile.
     * @return The initial score for the tile.
     */
    private static float calculateInitialScore(int distance) {
        return 1.0f / (distance * distance);  // Quadratic decrease for further tiles
    }

    /**
     * Calculates the weighted score based on distance and cumulative score.
     *
     * @param distance The distance to the tile.
     * @param currentScore The current cumulative score.
     * @return The weighted score for the tile.
     */
    private static float calculateWeightedScore(int distance, float currentScore) {
        return currentScore + calculateInitialScore(distance);
    }

    /**
     * Returns the offset vector based on the initial direction.
     *
     * @param direction The direction for the offset.
     * @return The vector offset for moving in that direction.
     */
    private static List<Vector2i> getNeighborsInDirection(Vector2i position, Direction direction) {
        List<Vector2i> neighbors = new ArrayList<>();
        Vector2i offset = getDirectionOffset(direction);  // Assumes this helper method is available
        neighbors.add(new Vector2i(position.x + offset.x, position.y + offset.y));
        return neighbors;
       
    }
    private static Vector2i getDirectionOffset(Direction direction) {
        return switch (direction) {
            case UP -> new Vector2i(0, 1);
            case DOWN -> new Vector2i(0, -1);
            case LEFT -> new Vector2i(-1, 0);
            case RIGHT -> new Vector2i(1, 0);
        };
    }
}
