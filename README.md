<div align="center">

<img src="https://i.imgur.com/placeholder.png" alt="NotLib" width="120" />

# NotLib

**A batteries-included developer library for Paper & Folia Minecraft plugins.**

[![License: GPL-3.0](https://img.shields.io/github/license/NotMarra/NotLib?style=for-the-badge
)](LICENSE)
[![Paper](https://img.shields.io/badge/Paper-1.21%2B-brightgreen?style=for-the-badge)](https://papermc.io)
[![Folia](https://img.shields.io/badge/Folia-compatible-brightgreen?style=for-the-badge)](https://papermc.io/software/folia)
[![Java](https://img.shields.io/badge/Java-21%2B-orange?style=for-the-badge)](https://adoptium.net)

[📖 Documentation](https://wiki.notmarra.dev/notlib/getting-started/installation/) · [🐛 Report a bug](https://github.com/NotMarra/NotLib/issues/new?template=bug_report.md) · [💡 Request a feature](https://github.com/NotMarra/NotLib/issues/new?template=feature_request.md)

</div>

---

## Overview

NotLib eliminates the boilerplate that every Paper plugin repeats — database connections, config management, GUI building, command registration and more — so you can focus on your plugin's actual logic.

```java
public class MyPlugin extends NotPlugin {
    private DatabaseManager db;
    private LanguageManager lang;

    @Override
    public void initPlugin() {
        db   = sqliteDatabase(getDataFolder(), "data").build();
        lang = languageManager().defaultLocale("en_US").build();

        db.registerCached(PlayerProfile.class);
        addListener(new PlayerListener(this));
    }
}
```

## Features

| Module | What it does |
|---|---|
| **NotPlugin** | Base plugin class — wires up everything automatically |
| **Database** | HikariCP ORM with SQLite & MariaDB, annotation-based entities, fluent QueryBuilder |
| **Cache** | In-memory cache with TTL, LRU/FIFO eviction and three write strategies |
| **DatabaseManager** | Single entry point combining database + cache with auto lifecycle management |
| **Language** | YAML-based localisation with MiniMessage support, prefix injection and hot-reload |
| **GUI** | Fluent inventory builder with pattern layouts, containers and animations |
| **Commands** | Brigadier command system with typed arguments and built-in help generation |
| **Config** | Managed YAML configs with auto-update, comment preservation and directory watching |
| **Scheduler** | Unified Folia/Paper scheduling API |

## Requirements

- Java **21+**
- Paper or Folia **1.21+**

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
<dependency>
    <groupId>com.github.NotMarra</groupId>
    <artifactId>NotLib</artifactId>
    <version>Tag</version>
</dependency>
</dependencies>
```

### Gradle (Kotlin DSL)

```kotlin
repositories {
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.github.NotMarra:NotLib:Tag")
}
```

### plugin.yml

```yaml
depend:
  - NotLib
```

## Quick examples

### Database + Cache

```java
// Define entity
@Table(name = "players")
public class PlayerProfile {
    @Column(name = "id", primaryKey = true)
    private UUID uuid;

    @Column(name = "name")
    private String name;

    @Column(name = "level")
    private int level;
}

// In initPlugin()
db = sqliteDatabase(getDataFolder(), "data")
        .defaultWriteStrategy(WriteStrategy.WRITE_BEHIND)
        .build();
db.registerCached(PlayerProfile.class);

// Usage
CachedRepository<UUID, PlayerProfile> repo = db.cached(PlayerProfile.class);
repo.findByIdAsync(uuid).thenAccept(opt -> opt.ifPresent(this::handle));
repo.upsert(updatedProfile);
```

### Language

```yaml
# languages/en_US.yml
prefix: "<gray>[<aqua>MyPlugin</aqua>]</gray> "
player:
  join: "%prefix%<green>%player% joined!"
```

```java
lang.get("player.join")
    .withPlayer(player)
    .sendTo(player);
```

### GUI

```java
GUI gui = GUI.create("Shop")
        .rows(3)
        .pattern("""
                #########
                #A#B#C###
                #########
                """)
        .emptySlotChars(List.of('#'))
        .onPatternMatch(info -> switch (info.ch) {
            case 'A' -> gui.createItem(Material.APPLE).name("Apple");
            case 'B' -> gui.createItem(Material.BREAD).name("Bread");
            default  -> null;
        });
gui.open(player);
```

### Commands

```java
Command.of("give", "Give items to a player")
        .permission("myplugin.give")
        .arg(new PlayerArg("target")
                .arg(new IntArg("amount")
                        .onExecute(c -> {
                            Player target = c.getPlayerV("target");
                            int amount    = c.getIntegerV("target.amount");
                            // …
                        })));
```

## Contributing

Contributions are welcome! Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'feat: add my feature'`)
4. Push and open a Pull Request

## License

Distributed under the GPL-3.0 License. See [LICENSE](LICENSE) for details.
