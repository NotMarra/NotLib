package dev.notmarra.notlib.gui.utils;

public class Size {
    public int width;
    public int height;

    public Size() {
        this(0, 0);
    }

    public Size(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Size add(Size other) {
        return new Size(this.width + other.width, this.height + other.height);
    }

    public Size sub(Size other) {
        return new Size(this.width - other.width, this.height - other.height);
    }

    public Size mul(Size other) {
        return new Size(this.width * other.width, this.height * other.height);
    }

    public Size div(Size other) {
        return new Size(this.width / other.width, this.height / other.height);
    }

    public Size add(int width, int height) {
        return new Size(this.width + width, this.height + height);
    }

    public Size sub(int width, int height) {
        return new Size(this.width - width, this.height - height);
    }

    public Size mul(int width, int height) {
        return new Size(this.width * width, this.height * height);
    }

    public Size div(int width, int height) {
        return new Size(this.width / width, this.height / height);
    }

    public Size add(int value) {
        return new Size(this.width + value, this.height + value);
    }

    public Size sub(int value) {
        return new Size(this.width - value, this.height - value);
    }

    public Size mul(int value) {
        return new Size(this.width * value, this.height * value);
    }

    public Size div(int value) {
        return new Size(this.width / value, this.height / value);
    }

    public Size set(int width) {
        this.width = width;
        this.height = width;
        return this;
    }

    public Size set(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public Size set(Size other) {
        this.width = other.width;
        this.height = other.height;
        return this;
    }

    public String toString() {
        return "Size{width=" + this.width + ", height=" + this.height + "}";
    }

    public static Size of(int width) {
        return new Size(width, width);
    }

    public static Size of(int width, int height) {
        return new Size(width, height);
    }

    public static Size of(Size other) {
        return new Size(other.width, other.height);
    }

    public static Size zero() {
        return new Size();
    }
}
