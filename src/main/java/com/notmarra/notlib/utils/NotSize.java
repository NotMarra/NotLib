package com.notmarra.notlib.utils;

public class NotSize {
    public int width;
    public int height;

    public NotSize() {
        this(0, 0);
    }

    public NotSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public NotSize add(NotSize other) {
        return new NotSize(this.width + other.width, this.height + other.height);
    }

    public NotSize sub(NotSize other) {
        return new NotSize(this.width - other.width, this.height - other.height);
    }

    public NotSize mul(NotSize other) {
        return new NotSize(this.width * other.width, this.height * other.height);
    }

    public NotSize div(NotSize other) {
        return new NotSize(this.width / other.width, this.height / other.height);
    }

    public NotSize add(int width, int height) {
        return new NotSize(this.width + width, this.height + height);
    }

    public NotSize sub(int width, int height) {
        return new NotSize(this.width - width, this.height - height);
    }

    public NotSize mul(int width, int height) {
        return new NotSize(this.width * width, this.height * height);
    }

    public NotSize div(int width, int height) {
        return new NotSize(this.width / width, this.height / height);
    }

    public NotSize add(int value) {
        return new NotSize(this.width + value, this.height + value);
    }

    public NotSize sub(int value) {
        return new NotSize(this.width - value, this.height - value);
    }

    public NotSize mul(int value) {
        return new NotSize(this.width * value, this.height * value);
    }

    public NotSize div(int value) {
        return new NotSize(this.width / value, this.height / value);
    }

    public NotSize set(int width) {
        this.width = width;
        this.height = width;
        return this;
    }

    public NotSize set(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public NotSize set(NotSize other) {
        this.width = other.width;
        this.height = other.height;
        return this;
    }

    public String toString() {
        return "NotSize{width=" + this.width + ", height=" + this.height + "}";
    }

    public static NotSize of(int width) {
        return new NotSize(width, width);
    }

    public static NotSize of(int width, int height) {
        return new NotSize(width, height);
    }

    public static NotSize of(NotSize other) {
        return new NotSize(other.width, other.height);
    }

    public static NotSize zero() {
        return new NotSize();
    }
}
