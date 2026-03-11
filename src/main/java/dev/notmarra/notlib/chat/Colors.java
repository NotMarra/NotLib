package dev.notmarra.notlib.chat;

import net.kyori.adventure.text.format.TextColor;

public enum Colors {
    DODGERBLUE(30, 144, 255),
    RED(255, 0, 0),
    GREEN(0, 255, 0),
    BLUE(0, 0, 255),
    YELLOW(255, 255, 0),
    ORANGE(255, 165, 0),
    WHITE(255, 255, 255),
    BLACK(0, 0, 0),
    GRAY(128, 128, 128),
    DARKGRAY(169, 169, 169),
    LIGHTGRAY(211, 211, 211),
    PURPLE(128, 0, 128),
    PINK(255, 192, 203),
    CYAN(0, 255, 255),
    MAGENTA(255, 0, 255),
    LIME(0, 255, 0),
    BROWN(165, 42, 42),
    GOLD(255, 215, 0),
    AQUA(0, 255, 255),
    LIGHTPURPLE(255, 182, 193),
    LIGHTBLUE(173, 216, 230),
    LIGHTRED(255, 182, 193);

    private final TextColor color;

    Colors(int r, int g, int b) {
        this.color = TextColor.color(r, g, b);
    }

    public TextColor get() {
        return color;
    }
}
