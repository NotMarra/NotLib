package com.notmarra.notlib.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.bossbar.BossBar.Color;
import net.kyori.adventure.bossbar.BossBar.Overlay;
import net.kyori.adventure.text.Component;

public class NotBossBar {
    private BossBar bossBar;
    private Component title;
    private float progress;
    private Color color;
    private Overlay overlay;

    public enum NotColor {
        BLUE(Color.BLUE),
        GREEN(Color.GREEN),
        PINK(Color.PINK),
        PURPLE(Color.PURPLE),
        RED(Color.RED),
        WHITE(Color.WHITE),
        YELLOW(Color.YELLOW);
        
        private final Color color;
        
        NotColor(Color color) {
            this.color = color;
        }
        
        public Color get() {
            return color;
        }
    }
    
    public enum NotOverlay {
        PROGRESS(Overlay.PROGRESS),
        NOTCHED_6(Overlay.NOTCHED_6),
        NOTCHED_10(Overlay.NOTCHED_10),
        NOTCHED_12(Overlay.NOTCHED_12),
        NOTCHED_20(Overlay.NOTCHED_20);
        
        private final Overlay overlay;
        
        NotOverlay(Overlay overlay) {
            this.overlay = overlay;
        }
        
        public Overlay get() {
            return overlay;
        }
    }
    

    public NotBossBar(Component title, float progress, NotColor color, NotOverlay overlay) {
        this.title = title;
        this.progress = progress;
        this.color = color.color;
        this.overlay = overlay.overlay;
        this.bossBar = build();
    }

    public static NotBossBar create(Component title, float progress, NotColor color, NotOverlay overlay) {
        return new NotBossBar(title, progress, color, overlay);
    }

    public static NotBossBar create(Component title, float progress, NotColor color) {
        return new NotBossBar(title, progress, color, NotOverlay.PROGRESS);
    }

    public static NotBossBar create(Component title, float progress) {
        return new NotBossBar(title, progress, NotColor.BLUE, NotOverlay.PROGRESS);
    }

    public static NotBossBar create(Component title) {
        return new NotBossBar(title, 1.0f, NotColor.BLUE, NotOverlay.PROGRESS);
    }

    public BossBar build() {
        return BossBar.bossBar(title, progress, color, overlay);
    }

    public BossBar get() {
        return bossBar;
    }
}
