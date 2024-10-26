package com.buaisociety.pacman.maze;

import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import org.joml.Vector2i;

import java.util.*;

public class AStar { 
    private final Maze maze;

    public AStar(Maze maze) {
        this.maze = maze;
    }
     // New BFS method to locate the nearest pellet
      // Returns a map with the direction and corresponding SearchResult for each BFS search
    public Map<Direction, SearchResult> findNearestPelletBFS(Vector2i startPosition) {
        Map<Direction, SearchResult> directionResults = new HashMap<>();

        for (Direction direction : Direction.values()) {
            Queue<Vector2i> queue = new LinkedList<>();
            Set<Vector2i> visited = new HashSet<>();
            queue.add(startPosition);
            visited.add(startPosition);

            int distance = 0;
            boolean foundPellet = false;
            Vector2i pelletPosition = null;

            while (!queue.isEmpty() && !foundPellet) {
                int levelSize = queue.size();
                distance++;

                for (int i = 0; i < levelSize; i++) {
                    Vector2i currentPos = queue.poll();
                    Tile currentTile = maze.getTile(currentPos.x() / 10, currentPos.y() / 10);

                    if (currentTile != null && currentTile.getState() == TileState.PELLET) {
                        pelletPosition = currentPos;
                        foundPellet = true;
                        break;
                    }

                    // Explore only in the initial direction for each BFS search
                    int nextX = currentPos.x() + direction.getDx();
                    int nextY = currentPos.y() + direction.getDy();
                    Vector2i neighborPos = new Vector2i(nextX, nextY);
                    Tile neighborTile = maze.getTile(nextX / 10, nextY / 10).getNeighbor(direction);

                    if (neighborTile != null && neighborTile.getState() != TileState.WALL && !visited.contains(neighborPos)) {
                        queue.add(neighborPos);
                        visited.add(neighborPos);
                    }
                }
            }

            if (foundPellet && pelletPosition != null) {
                directionResults.put(direction, new SearchResult(pelletPosition, distance));
            } else {
                // If no pellet found in this direction, mark it with a high distance or null
                directionResults.put(direction, new SearchResult(null, Integer.MAX_VALUE));
            }
        }
        return directionResults;
    }
    // Method to calculate minimum distance to the nearest pellet
    public float findMinDistanceToPellet(Vector2i startPosition) {
        PriorityQueue<TileCosts> openList = new PriorityQueue<>(Comparator.comparingInt(tc -> tc.fCost));
        Map<Vector2i, TileCosts> costs = new HashMap<>();
        Vector2i goal = findNearestPellet(startPosition);
        if (goal == null) return -1; // No pellet found

        TileCosts startCosts = new TileCosts(startPosition, 0, getHeuristicCost(startPosition, goal));
        costs.put(startPosition, startCosts);
        openList.add(startCosts);

        while (!openList.isEmpty()) {
            TileCosts current = openList.poll();
            if (current.position.equals(goal)) {
                return current.fCost; // Return distance as cost to goal
            }

            for (Direction direction : Direction.values()) {
                Tile neighborTile = maze.getTile(current.position.x() / 10 + direction.getDx(), current.position.y() / 10 + direction.getDy()).getNeighbor(direction);
                if (neighborTile == null || neighborTile.getState() == TileState.WALL) continue; // Ignore walls

                Vector2i neighborPos = (Vector2i) neighborTile.getPosition();
                int tentativeGCost = current.gCost + 1; // Assume a cost of 1 per tile

                TileCosts neighborCosts = costs.getOrDefault(neighborPos, new TileCosts(neighborPos));
                if (tentativeGCost < neighborCosts.gCost) {
                    neighborCosts.gCost = tentativeGCost;
                    neighborCosts.hCost = getHeuristicCost(neighborPos, goal);
                    neighborCosts.fCost = neighborCosts.gCost + neighborCosts.hCost;
                    openList.add(neighborCosts);
                    costs.put(neighborPos, neighborCosts);
                }
            }
        }
        return -1; // Path not found
    }

    public Direction findBackupPathOrRandom(Vector2i currentPosition) {
        List<Direction> possibleDirections = new ArrayList<>();
        
        for (Direction direction : Direction.values()) {
            Tile neighborTile = maze.getTile(currentPosition.x() / 10 + direction.getDx(), currentPosition.y() / 10 + direction.getDy());
            if (neighborTile != null && neighborTile.getState() != TileState.WALL) {
                possibleDirections.add(direction);
            }
        }
    
        if (possibleDirections.isEmpty()) return Direction.UP;
    
        return possibleDirections.get(new Random().nextInt(possibleDirections.size()));
    }
    
    public boolean isStuck(Vector2i currentPosition, Vector2i lastPosition) {
        return currentPosition.equals(lastPosition);
    }

    public int getHeuristicCost(Vector2i start, Vector2i goal) {
        return Math.abs(start.x() - goal.x()) + Math.abs(start.y() - goal.y());
    }

    public Vector2i findNearestPellet(Vector2i startPosition) {
        Vector2i closestPellet = null;
        int minDistance = Integer.MAX_VALUE;

        for (int y = 0; y < maze.getDimensions().y(); y++) {
            for (int x = 0; x < maze.getDimensions().x(); x++) {
                Tile tile = maze.getTile(x, y);
                if (tile.getState() == TileState.PELLET) {
                    int distance = getHeuristicCost(startPosition, new Vector2i(x, y));
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestPellet = new Vector2i(x, y);
                    }
                }
            }
        }
        return closestPellet;
    }
    public Direction findBestPathAvoidingWalls(Vector2i currentPosition, Vector2i pelletPosition) {
        Direction bestDirection = Direction.UP; // Default fallback
        int minHeuristicCost = Integer.MAX_VALUE;
    
        for (Direction direction : Direction.values()) {
            Tile neighborTile = maze.getTile(currentPosition.x() / 10 + direction.getDx(), currentPosition.y() / 10 + direction.getDy());
    
            if (neighborTile == null || neighborTile.getState() == TileState.WALL) continue; // Skip walls
    
            Vector2i neighborPos = (Vector2i) neighborTile.getPosition();
            int heuristicCost = getHeuristicCost(neighborPos, pelletPosition);
    
            // Choose direction with lowest heuristic and no wall
            if (heuristicCost < minHeuristicCost) {
                minHeuristicCost = heuristicCost;
                bestDirection = direction;
            }
        }
        return bestDirection;
    }
    
}

// Helper class for storing costs
class TileCosts {
    Vector2i position;
    int gCost, hCost, fCost;

    TileCosts(Vector2i position) {
        this.position = position;
        this.gCost = Integer.MAX_VALUE; // Initialize to infinity
        this.hCost = 0;
        this.fCost = 0;
    }

    TileCosts(Vector2i position, int gCost, int hCost) {
        this.position = position;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }
}
