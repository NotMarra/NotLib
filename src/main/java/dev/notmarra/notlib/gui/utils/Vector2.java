package dev.notmarra.notlib.gui.utils;

public class Vector2 {
    public int x;
    public int y;

    public Vector2() {
        this(0, 0);
    }

    public Vector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector2 add(Vector2 other) {
        return new Vector2(this.x + other.x, this.y + other.y);
    }

    public Vector2 sub(Vector2 other) {
        return new Vector2(this.x - other.x, this.y - other.y);
    }

    public Vector2 mul(Vector2 other) {
        return new Vector2(this.x * other.x, this.y * other.y);
    }

    public Vector2 div(Vector2 other) {
        return new Vector2(this.x / other.x, this.y / other.y);
    }

    public Vector2 add(int x, int y) {
        return new Vector2(this.x + x, this.y + y);
    }

    public Vector2 sub(int x, int y) {
        return new Vector2(this.x - x, this.y - y);
    }

    public Vector2 mul(int x, int y) {
        return new Vector2(this.x * x, this.y * y);
    }

    public Vector2 div(int x, int y) {
        return new Vector2(this.x / x, this.y / y);
    }

    public Vector2 add(int value) {
        return new Vector2(this.x + value, this.y + value);
    }

    public Vector2 sub(int value) {
        return new Vector2(this.x - value, this.y - value);
    }

    public Vector2 mul(int value) {
        return new Vector2(this.x * value, this.y * value);
    }

    public Vector2 div(int value) {
        return new Vector2(this.x / value, this.y / value);
    }

    public Vector2 set(int x) {
        this.x = x;
        this.y = x;
        return this;
    }

    public Vector2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public Vector2 set(Vector2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public Vector2 copy() {
        return new Vector2(this.x, this.y);
    }

    public boolean equals(Vector2 other) {
        return this.x == other.x && this.y == other.y;
    }

    public String toString() {
        return "Vector2{x=" + this.x + ", y=" + this.y + "}";
    }

    public static Vector2 of(int x) {
        return new Vector2(x, x);
    }

    public static Vector2 of(int x, int y) {
        return new Vector2(x, y);
    }

    public static Vector2 of(Vector2 other) {
        return new Vector2(other.x, other.y);
    }

    public static Vector2 zero() {
        return new Vector2();
    }
}