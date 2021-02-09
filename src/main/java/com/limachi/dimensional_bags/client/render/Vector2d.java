package com.limachi.dimensional_bags.client.render;

public class Vector2d {
    public double x;
    public double y;

    public Vector2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Vector2d v) { return x == v.x && y == v.y; }

    public Vector2d copy() { return new Vector2d(x, y); }
}
