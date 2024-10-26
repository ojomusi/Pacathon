package com.buaisociety.pacman.maze;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import com.buaisociety.pacman.entity.Direction;

public class SearchResult {
    private final Vector2i targetPosition;
    private final int distance;

    public SearchResult(Vector2i targetPosition, int distance) {
        this.targetPosition = targetPosition;
        this.distance = distance;
    }

    public Vector2i getTargetPosition() {
        return targetPosition;
    }

    public int getDistance() {
        return distance;
    }
   

}

