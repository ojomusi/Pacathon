package com.buaisociety.pacman.entity.behavior;

import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.entity.Entity;
import com.buaisociety.pacman.entity.PacmanEntity;
import com.cjcrafter.neat.compute.Calculator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.buaisociety.pacman.maze.Maze;
import com.buaisociety.pacman.maze.SearchResult;
import com.buaisociety.pacman.maze.Tile;
import com.buaisociety.pacman.maze.TileState;
import com.buaisociety.pacman.maze.AStar;
import com.buaisociety.pacman.sprite.DebugDrawing;
import com.cjcrafter.neat.Client;
import com.buaisociety.pacman.Searcher;
import com.buaisociety.pacman.entity.Direction;
import com.buaisociety.pacman.entity.Entity;
import com.buaisociety.pacman.entity.GhostEntity;
import com.buaisociety.pacman.entity.GhostState;
import com.buaisociety.pacman.entity.PacmanEntity;

import java.util.Map;
import java.util.Random;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List; 

public class TournamentBehavior implements Behavior {

    private final Calculator calculator;
    private @Nullable PacmanEntity pacman;
    private LinkedList<Direction> movementHistory; // Specify the type
    // Score modifiers help us maintain "multiple pools" of points.
    // This is great for training, because we can take away points from
    // specific pools of points instead of subtracting from all.
    private int scoreModifier = 0;
    
    private int lastScore = 0;
    private int updatesSinceLastScore = 0;
    private Vector2i lastPosition = null; // Initialize lastPosition
    public Direction lastDirection = null;

    private int previousScore = 0;
    private int framesSinceScoreUpdate = 0;

    public TournamentBehavior(Calculator calculator) {
        this.calculator = calculator;
    }

    /**
     * Returns the desired direction that the entity should move towards.
     *
     * @param entity the entity to get the direction for
     * @return the desired direction for the entity
     */
    @NotNull
    @Override
    public Direction getDirection(@NotNull Entity entity) {
        // --- DO NOT REMOVE ---
        if (pacman == null) {
            pacman = (PacmanEntity) entity;
        }

        int newScore = pacman.getMaze().getLevelManager().getScore();
        if (previousScore != newScore) {
            previousScore = newScore;
            framesSinceScoreUpdate = 0;
        } else {
            framesSinceScoreUpdate++;
        }

        if (framesSinceScoreUpdate > 60 * 40) {
            pacman.kill();
            framesSinceScoreUpdate = 0;
        }
        // --- END OF DO NOT REMOVE ---

        // TODO: Put all your code for info into the neural network here
         // Determine reward based on maintaining the same direction
    float directionReward = 0f;
    // Check for score updates and reset updatesSinceLastScore
    if (newScore > lastScore) {
        lastScore = newScore;
        updatesSinceLastScore = 0;
    }
    
    // Reward for maintaining the same direction
    if (newScore > lastScore && lastDirection != null && lastDirection == pacman.getDirection()) {
        scoreModifier += 3;
        directionReward = 1.0f; // Assign a reward for maintaining direction
    }

    // Track movement updates
    updatesSinceLastScore++;

    // Initialize movement history if not done
    if (movementHistory == null) {
        movementHistory = new LinkedList<>();
    }

    Direction newDirection = null;
    Vector2i closestPellet = null; // Declare closestPellet here
    
    // Check for penalty condition
    if (updatesSinceLastScore > 60 * 5) {
        // Get the last direction from movement history
        lastDirection = movementHistory.size() >= 4 ? movementHistory.get(movementHistory.size() - 4) : null;
        List<Direction> possibleDirections = new ArrayList<>();

        if (lastDirection != null) {
            switch (lastDirection) {
                case LEFT:
                    if (pacman.canMove(Direction.RIGHT)) possibleDirections.add(Direction.RIGHT);
                    if (pacman.canMove(Direction.DOWN)) possibleDirections.add(Direction.DOWN);
                    if (pacman.canMove(Direction.UP)) possibleDirections.add(Direction.UP);
                    break;
                case RIGHT:
                    if (pacman.canMove(Direction.LEFT)) possibleDirections.add(Direction.LEFT);
                    if (pacman.canMove(Direction.DOWN)) possibleDirections.add(Direction.DOWN);
                    if (pacman.canMove(Direction.UP)) possibleDirections.add(Direction.UP);
                    break;
                default:
                    // Default case to check all directions
                    if (pacman.canMove(Direction.UP)) possibleDirections.add(Direction.UP);
                    if (pacman.canMove(Direction.DOWN)) possibleDirections.add(Direction.DOWN);
                    if (pacman.canMove(Direction.LEFT)) possibleDirections.add(Direction.LEFT);
                    if (pacman.canMove(Direction.RIGHT)) possibleDirections.add(Direction.RIGHT);
                    break;
            }

            // If possible directions exist, select one randomly
            if (!possibleDirections.isEmpty()) {
                newDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
            }
        }
        
        // If possible directions exist, select one randomly
        if (!possibleDirections.isEmpty()) {
            newDirection = possibleDirections.get(new Random().nextInt(possibleDirections.size()));
        }
    }

    

    // If newDirection is null, proceed with A* or BFS to find the closest pellet
    if (newDirection == null) {
        AStar aStarInstance = new AStar(pacman.getMaze());
        Vector2i posStoreInt = new Vector2i((int) (pacman.getPosition().x() * 10), (int) (pacman.getPosition().y() * 10));
        if (lastPosition == null) {
            lastPosition = new Vector2i(posStoreInt); // Initialize lastPosition at start
        }
        closestPellet = aStarInstance.findNearestPellet(posStoreInt); // Compute closest pellet here
        Map<Direction, SearchResult> bfsResults = aStarInstance.findNearestPelletBFS(posStoreInt);

        // Select the best direction from BFS results
        Direction bestDirection = null;
        int minDistance = Integer.MAX_VALUE;

        for (Map.Entry<Direction, SearchResult> entry : bfsResults.entrySet()) {
            Direction direction = entry.getKey();
            SearchResult result = entry.getValue();

            if (result != null && result.getDistance() < minDistance && pacman.canMove(direction)) {
                minDistance = result.getDistance();
                bestDirection = direction;
            }
        }

        // Fallback if no valid path found
        if (bestDirection == null) {
            bestDirection = aStarInstance.findBackupPathOrRandom(posStoreInt);
        }
        
        // We are going to use these directions a lot for different inputs. Get them all once for clarity and brevity
        Direction forward = pacman.getDirection();
        Direction left = pacman.getDirection().left();
        Direction right = pacman.getDirection().right();
        Direction behind = pacman.getDirection().behind();
        
        // Input nodes 1, 2, 3, and 4 show if the pacman can move in the forward, left, right, and behind directions
        boolean canMoveForward = pacman.canMove(forward);
        boolean canMoveLeft = pacman.canMove(left);
        boolean canMoveRight = pacman.canMove(right);
        boolean canMoveBehind = pacman.canMove(behind);
        
        Tile currentTile = pacman.getMaze().getTile(pacman.getTilePosition());
        Map<Direction, SearchResult> nearestPellets = Searcher.findTileInAllDirections(currentTile, tile -> tile.getState() == TileState.PELLET, pacman.getMaze());

        // Calculate the maximum distance to normalize the distance values
        int maxDistance = -1;
        for (SearchResult result : nearestPellets.values()) {
            if (result != null) {
                maxDistance = Math.max(maxDistance, result.getDistance());
            }
        }

        // Calculate normalized distances for each direction
        float nearestPelletForward = nearestPellets.get(forward) != null 
            ? 1 - (float) nearestPellets.get(forward).getDistance() / maxDistance : 0;
        float nearestPelletLeft = nearestPellets.get(left) != null 
            ? 1 - (float) nearestPellets.get(left).getDistance() / maxDistance : 0;
        float nearestPelletRight = nearestPellets.get(right) != null 
            ? 1 - (float) nearestPellets.get(right).getDistance() / maxDistance : 0;
        float nearestPelletBehind = nearestPellets.get(behind) != null 
            ? 1 - (float) nearestPellets.get(behind).getDistance() / maxDistance : 0;

        // Final direction choice based on neural network

        float[] inputs = new float[] {
            canMoveForward ? 1f : 0f,
            canMoveLeft ? 1f : 0f,
            canMoveRight ? 1f : 0f,
            canMoveBehind ? 1f : 0f,
            closestPellet != null ? closestPellet.x() / 10.0f : 0,
            closestPellet != null ? closestPellet.y() / 10.0f : 0,
            directionReward,
            nearestPelletForward,
            nearestPelletLeft,
            nearestPelletRight,
            nearestPelletBehind 
        };
        float[] outputs = calculator.calculate(inputs).join();

        // Chooses the maximum output as the direction to go... feel free to change this ofc!
        // Adjust this to whatever you used in the NeatPacmanBehavior.class
        int index = 0;
        float max = outputs[0];
        for (int i = 1; i < outputs.length; i++) {
            if (outputs[i] > max) {
                max = outputs[i];
                index = i;
            }
        }

        // Set final direction based on neural network output
        Direction neuralNetDirection = switch (index) {
            case 0 -> pacman.getDirection();
            case 1 -> pacman.getDirection().left();
            case 2 -> pacman.getDirection().right();
            case 3 -> pacman.getDirection().behind();
            default -> throw new IllegalStateException("Unexpected value: " + index);
        };

        // Choose final direction based on newDirection or bestDirection
        Direction finalDirection = (newDirection != null) ? newDirection : 
            (pacman.canMove(bestDirection) && !aStarInstance.isStuck(posStoreInt, lastPosition)) ? 
            neuralNetDirection : 
            bestDirection;

        // Update movement history with the final direction
        movementHistory.add(finalDirection);
        // Limit history to the last 4 moves
        if (movementHistory.size() > 4) {
            movementHistory.removeFirst();
        }

        lastPosition.set(new Vector2i((int) (pacman.getPosition().x() * 10), (int) (pacman.getPosition().y() * 10))); // Update last position for the next check
    //    client.setScore(pacman.getMaze().getLevelManager().getScore() + scoreModifier);

        return finalDirection;
    } else {
        // If newDirection is chosen, assign it as the finalDirection
        return newDirection;
    }
    }
}

