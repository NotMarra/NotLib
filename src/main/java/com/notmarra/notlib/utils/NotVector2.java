package com.notmarra.notlib.utils;

public class NotVector2 {
    public int x;
    public int y;

    public NotVector2() {
        this(0, 0);
    }

    public NotVector2(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public NotVector2 add(NotVector2 other) {
        return new NotVector2(this.x + other.x, this.y + other.y);
    }

    public NotVector2 sub(NotVector2 other) {
        return new NotVector2(this.x - other.x, this.y - other.y);
    }

    public NotVector2 mul(NotVector2 other) {
        return new NotVector2(this.x * other.x, this.y * other.y);
    }

    public NotVector2 div(NotVector2 other) {
        return new NotVector2(this.x / other.x, this.y / other.y);
    }

    public NotVector2 add(int x, int y) {
        return new NotVector2(this.x + x, this.y + y);
    }

    public NotVector2 sub(int x, int y) {
        return new NotVector2(this.x - x, this.y - y);
    }

    public NotVector2 mul(int x, int y) {
        return new NotVector2(this.x * x, this.y * y);
    }

    public NotVector2 div(int x, int y) {
        return new NotVector2(this.x / x, this.y / y);
    }

    public NotVector2 add(int value) {
        return new NotVector2(this.x + value, this.y + value);
    }

    public NotVector2 sub(int value) {
        return new NotVector2(this.x - value, this.y - value);
    }

    public NotVector2 mul(int value) {
        return new NotVector2(this.x * value, this.y * value);
    }

    public NotVector2 div(int value) {
        return new NotVector2(this.x / value, this.y / value);
    }

    public NotVector2 set(int x) {
        this.x = x;
        this.y = x;
        return this;
    }

    public NotVector2 set(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }

    public NotVector2 set(NotVector2 other) {
        this.x = other.x;
        this.y = other.y;
        return this;
    }

    public NotVector2 copy() {
        return new NotVector2(this.x, this.y);
    }

    public boolean equals(NotVector2 other) {
        return this.x == other.x && this.y == other.y;
    }

    public String toString() {
        return "NotVector2{x=" + this.x + ", y=" + this.y + "}";
    }

    public static NotVector2 of(int x) {
        return new NotVector2(x, x);
    }

    public static NotVector2 of(int x, int y) {
        return new NotVector2(x, y);
    }

    public static NotVector2 of(NotVector2 other) {
        return new NotVector2(other.x, other.y);
    }

    public static NotVector2 zero() {
        return new NotVector2();
    }
}
