package com.notmarra.notlib.extensions;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.block.Biome;
import org.bukkit.block.BlockType;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemType;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;

public class NotMinecraftStuff {
    public static NotMinecraftStuff instance;

    public static NotMinecraftStuff getInstance() {
        if (instance == null) {
            instance = new NotMinecraftStuff();
        }

        return instance;
    }

    public List<String> blockIdNames;
    public List<String> itemIdNames;
    public List<String> entityIdNames;
    public List<String> biomeIdNames;

    public void initialize() {
        blockIdNames = allBlockIdNames();
        itemIdNames = allItemIdNames();
        entityIdNames = allEntityIdNames();
        biomeIdNames = allBiomeIdNames();
    }

    private static List<String> allBlockIdNames() {
        ArrayList<String> blocks = new ArrayList<>();
        Collection<Tag<BlockType>> blockTypes = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).getTags();

        for (Tag<BlockType> blockType : blockTypes) {
            Collection<TypedKey<BlockType>> typedBlocks = blockType.values();

            for (TypedKey<BlockType> typedBlock : typedBlocks) {
                blocks.add(typedBlock.asString().split(":")[1].toUpperCase().replaceAll("_", " "));
            }
        }

        return blocks;
    }

    private static List<String> allItemIdNames() {
        ArrayList<String> items = new ArrayList<>();
        Collection<Tag<ItemType>> itemTypes = RegistryAccess.registryAccess().getRegistry(RegistryKey.ITEM).getTags();

        for (Tag<ItemType> itemType : itemTypes) {
            Collection<TypedKey<ItemType>> typedItems = itemType.values();

            for (TypedKey<ItemType> typedItem : typedItems) {
                items.add(typedItem.asString().split(":")[1].toUpperCase().replaceAll("_", " "));
            }
        }

        return items;
    }

    private static List<String> allEntityIdNames() {
        ArrayList<String> entities = new ArrayList<>();
        Collection<Tag<EntityType>> entityTypes = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).getTags();

        for (Tag<EntityType> entityType : entityTypes) {
            Collection<TypedKey<EntityType>> typedEntities = entityType.values();

            for (TypedKey<EntityType> typedEntity : typedEntities) {
                entities.add(typedEntity.asString().split(":")[1].toUpperCase().replaceAll("_", " "));
            }
        }

        return entities;
    }

    private static List<String> allBiomeIdNames() {
        ArrayList<String> biomes = new ArrayList<>();
        Collection<Tag<Biome>> biomeTypes = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).getTags();

        for (Tag<Biome> biomeType : biomeTypes) {
            Collection<TypedKey<Biome>> typedBiomes = biomeType.values();

            for (TypedKey<Biome> typedBiome : typedBiomes) {
                biomes.add(typedBiome.asString().split(":")[1].toUpperCase().replaceAll("_", " "));
            }
        }

        return biomes;
    }
}
