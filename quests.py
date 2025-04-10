#!/usr/bin/env python3
"""
Quest Configuration Generator
----------------------------
This script generates YAML quest configurations for Minecraft NotQuests plugin.
It creates a variety of quests with randomized objectives, rewards, and descriptions.
"""

import random
import yaml
import os
from typing import Dict, List, Any

# Constants for quest generation
CATEGORIES = ["mining", "combat", "farming", "exploration", "fishing", "daily", "special"]

# Objective types and potential targets
OBJECTIVE_TYPES = {
    "break": [
        "stone", "coal_ore", "iron_ore", "gold_ore", "diamond_ore", "deepslate_coal_ore", 
        "deepslate_iron_ore", "deepslate_gold_ore", "deepslate_diamond_ore", "ancient_debris",
        "oak_log", "spruce_log", "birch_log", "jungle_log", "acacia_log", "dark_oak_log",
        "wheat", "carrots", "potatoes", "beetroots", "pumpkin", "melon"
    ],
    "kill": [
        "zombie", "skeleton", "spider", "creeper", "enderman", "witch", "zombie_villager",
        "husk", "drowned", "stray", "phantom", "slime", "cave_spider", "blaze", "ghast",
        "magma_cube", "piglin", "hoglin", "wither_skeleton", "guardian", "elder_guardian",
        "pillager", "vindicator", "evoker", "ravager", "shulker", "ender_dragon", "wither"
    ],
    "interact": [
        "crafting_table", "furnace", "blast_furnace", "smoker", "smithing_table", "cartography_table",
        "loom", "grindstone", "stonecutter", "enchanting_table", "anvil", "cauldron", "composter",
        "barrel", "chest", "shulker_box", "ender_chest", "beacon", "respawn_anchor", "lodestone",
        "water", "lava", "campfire", "soul_campfire", "brewing_stand", "oak_sign"
    ]
}

# Mining-specific objectives
MINING_OBJECTIVES = {
    "break": [
        "stone", "coal_ore", "iron_ore", "gold_ore", "diamond_ore", "emerald_ore", "lapis_ore",
        "redstone_ore", "deepslate_coal_ore", "deepslate_iron_ore", "deepslate_gold_ore", 
        "deepslate_diamond_ore", "deepslate_emerald_ore", "deepslate_lapis_ore", "deepslate_redstone_ore",
        "nether_gold_ore", "nether_quartz_ore", "ancient_debris", "obsidian"
    ]
}

# Farming-specific objectives
FARMING_OBJECTIVES = {
    "break": [
        "wheat", "carrots", "potatoes", "beetroots", "pumpkin", "melon", "sugarcane",
        "bamboo", "cocoa", "nether_wart", "sweet_berry_bush", "kelp", "sea_pickle"
    ]
}

# Combat-specific objectives
COMBAT_OBJECTIVES = {
    "kill": [
        "zombie", "skeleton", "spider", "creeper", "enderman", "witch", "zombie_villager",
        "husk", "drowned", "stray", "phantom", "slime", "cave_spider", "blaze", "ghast",
        "magma_cube", "piglin", "hoglin", "wither_skeleton", "guardian", "elder_guardian",
        "pillager", "vindicator", "evoker", "ravager", "shulker", "ender_dragon", "wither"
    ]
}

# Exploration-specific objectives
EXPLORATION_OBJECTIVES = {
    "interact": [
        "oak_sign", "spruce_sign", "birch_sign", "jungle_sign", "acacia_sign", "dark_oak_sign",
        "crimson_sign", "warped_sign", "lodestone", "respawn_anchor", "beacon", "dragon_egg"
    ]
}

# Fishing-specific objectives
FISHING_OBJECTIVES = {
    "interact": ["water"]
}

# Quest title templates by category
QUEST_TITLES = {
    "mining": [
        "The Deep Delver", "Mineral Hunter", "Stone Gatherer", "Coal Collector", "Iron Miner",
        "Gold Rush", "Diamond Seeker", "Emerald Excavator", "Obsidian Harvester", "Ancient Debris Hunter",
        "Deepslate Explorer", "The Quarry Master", "Ore Hunter", "The Prospector", "Gem Collector"
    ],
    "combat": [
        "Monster Slayer", "Zombie Hunter", "Spider Exterminator", "Skeleton Archer", "Creeper Bomber",
        "Nether Beast Hunter", "The Executioner", "Undead Slayer", "Mob Bounty Hunter", "The Exterminator",
        "Wither Hunter", "Dragon Slayer", "Beast Master", "Phantom Menace", "Guardian of the Sea"
    ],
    "farming": [
        "Crop Harvester", "Wheat Farmer", "Carrot Collector", "Potato Gatherer", "Beetroot Farmer",
        "Pumpkin Patch", "Melon Master", "Sugar Cane Farmer", "Bamboo Harvester", "Sweet Berry Picker",
        "Nether Wart Gatherer", "Mushroom Collector", "Kelp Farmer", "Master Gardener", "Orchard Keeper"
    ],
    "exploration": [
        "World Traveler", "Treasure Hunter", "Monument Explorer", "Village Visitor", "Nether Explorer",
        "End Adventurer", "Sign Collector", "Beacon Builder", "Respawn Point Maker", "Outpost Infiltrator",
        "Mansion Explorer", "Ancient City Explorer", "Trail Runner", "Desert Nomad", "Arctic Explorer"
    ],
    "fishing": [
        "Master Angler", "Treasure Fisher", "Deep Sea Catcher", "Lake Fisherman", "River Angler",
        "Rare Fish Collector", "Ocean Harvester", "Fishing Champion", "Enchanted Catcher", "Sea Scavenger",
        "Tropical Fisher", "Ice Fisher", "Midnight Angler", "Dawn Fisher", "Twilight Caster"
    ],
    "daily": [
        "Daily Miner", "Daily Hunter", "Daily Farmer", "Daily Fisher", "Daily Explorer",
        "Daily Crafting", "Daily Build", "Daily Enchanter", "Daily Brewer", "Daily Challenge",
        "Morning Task", "Afternoon Activity", "Evening Quest", "Night Mission", "Dawn Patrol"
    ],
    "special": [
        "Welcome to the Server", "Server Anniversary", "Holiday Special", "Dragon Hunter",
        "Wither Challenger", "Ancient Building", "Master of All Trades", "The Final Challenge",
        "Legendary Quest", "Hero's Journey", "Epic Adventure", "Unique Discovery", "Ultimate Challenge",
        "Special Event", "Limited Time Offer", "Seasonal Special"
    ]
}

# Quest description templates by category
QUEST_DESCRIPTIONS = {
    "mining": [
        "Delve deep into the caves and collect valuable minerals.",
        "The blacksmith needs special ores. Can you help?",
        "Put your mining skills to use by collecting these rare materials.",
        "Test your mining prowess by gathering these valuable resources.",
        "The kingdom needs these materials for construction. Will you answer the call?",
        "Rare minerals are needed for a special project. Are you up to the task?"
    ],
    "combat": [
        "These monsters have been causing trouble. Eliminate them!",
        "Prove your combat skills by defeating these enemies.",
        "The village is under attack! Help defend it by slaying these creatures.",
        "A bounty has been placed on these monsters. Can you claim it?",
        "These creatures are a threat to the kingdom. Will you help remove them?",
        "Test your fighting abilities against these formidable foes."
    ],
    "farming": [
        "The village needs food supplies. Can you harvest these crops?",
        "Show off your farming skills by gathering these plants.",
        "A feast is being prepared and your crops are needed!",
        "Fresh produce is required for the upcoming market day.",
        "The kingdom faces food shortages. Your farming skills are needed!",
        "Demonstrate your green thumb by harvesting these crops."
    ],
    "exploration": [
        "Venture into the unknown and discover hidden places.",
        "Find and interact with these special locations around the world.",
        "Chart unexplored territories by visiting these landmarks.",
        "Important information is hidden throughout the land. Can you find it?",
        "Ancient secrets await those brave enough to explore.",
        "An expedition is underway, and your exploration skills are needed!"
    ],
    "fishing": [
        "Test your fishing skills in the waters around the kingdom.",
        "The local chef needs fresh fish for a special recipe.",
        "Cast your line and see what treasures you can reel in!",
        "A fishing competition is happening - show off your angling skills!",
        "Rare aquatic treasures are waiting to be caught. Are you skilled enough?",
        "The royal table requires the finest fish. Can you provide them?"
    ],
    "daily": [
        "Complete these tasks before the day is done!",
        "A new day brings new challenges. Can you complete them all?",
        "Daily tasks to keep the kingdom running smoothly.",
        "The morning's tasks await a skilled adventurer like yourself.",
        "Today's challenges require your unique abilities.",
        "Each day brings opportunities to prove your worth!"
    ],
    "special": [
        "A rare opportunity for the most skilled adventurers!",
        "Only the bravest dare attempt this special challenge.",
        "A unique quest that tests all your abilities!",
        "This special task comes with great rewards for those who succeed.",
        "A once-in-a-lifetime adventure awaits!",
        "The kingdom calls upon its greatest heroes for this special mission."
    ]
}

def generate_quests(num_quests: int = 20) -> Dict[str, Any]:
    """Generate a complete quest configuration with the specified number of quests."""
    quests_config = {
        "settings": {
            "notify-on-progress": True,
            "sound-on-complete": True,
            "completion-sound": "entity.player.levelup",
            "sound-volume": 1.0,
            "sound-pitch": 1.2,
            "max-active-quests": 5,
            "auto-assign-daily": True
        },
        "quests": {}
    }
    
    # Track used quest IDs to avoid duplicates
    used_quest_ids = set()
    # Track used titles to avoid duplicates
    used_titles = set()
    
    for i in range(num_quests):
        # Choose a category with some weighting (more common categories appear more often)
        category_weights = {
            "mining": 3, 
            "combat": 3, 
            "farming": 2, 
            "exploration": 2, 
            "fishing": 2, 
            "daily": 4,
            "special": 1
        }
        category = random.choices(
            list(category_weights.keys()), 
            weights=list(category_weights.values()),
            k=1
        )[0]
        
        # Generate a unique quest ID
        quest_id = f"{category}_{i+1}"
        counter = 1
        while quest_id in used_quest_ids:
            quest_id = f"{category}_{i+1}_{counter}"
            counter += 1
        used_quest_ids.add(quest_id)
        
        # Generate unique title
        available_titles = [t for t in QUEST_TITLES[category] if t not in used_titles]
        if not available_titles:  # If we've used all titles for this category
            available_titles = QUEST_TITLES[category]
            title = f"{random.choice(available_titles)} {counter}"  # Add counter to make unique
        else:
            title = random.choice(available_titles)
        used_titles.add(title)
        
        # Generate description
        description = random.choice(QUEST_DESCRIPTIONS[category])
        
        # Determine if quest is repeatable (dailies always are, specials rarely are)
        if category == "daily":
            repeatable = True
            cooldown_hours = 24
        elif category == "special":
            repeatable = random.random() < 0.2  # 20% chance
            cooldown_hours = random.choice([48, 72, 168])  # 2 days, 3 days, or 1 week
        else:
            repeatable = random.random() < 0.7  # 70% chance
            cooldown_hours = random.choice([24, 48, 72])  # 1-3 days
            
        # Generate objectives based on category
        objectives = {}
        num_objectives = random.randint(1, 3)  # 1-3 objectives per quest
        
        # Use category-specific objectives when possible
        if category == "mining":
            objective_pool = MINING_OBJECTIVES
        elif category == "combat":
            objective_pool = COMBAT_OBJECTIVES
        elif category == "farming":
            objective_pool = FARMING_OBJECTIVES
        elif category == "exploration":
            objective_pool = EXPLORATION_OBJECTIVES
        elif category == "fishing":
            objective_pool = FISHING_OBJECTIVES
        else:
            objective_pool = OBJECTIVE_TYPES
        
        # Keep track of used targets to prevent duplicates
        used_targets = set()
        
        for j in range(num_objectives):
            obj_id = f"obj{j+1}"
            
            # Select objective type and unique target
            obj_type = random.choice(list(objective_pool.keys()))
            available_targets = [t for t in objective_pool[obj_type] if t not in used_targets]
            
            # If all targets have been used, try a different objective type
            if not available_targets:
                # Try to find another objective type with available targets
                alternative_types = [t for t in objective_pool.keys() if t != obj_type]
                for alt_type in alternative_types:
                    alt_available_targets = [t for t in objective_pool[alt_type] if t not in used_targets]
                    if alt_available_targets:
                        obj_type = alt_type
                        available_targets = alt_available_targets
                        break
                
                # If still no available targets, reuse one but with different amount
                if not available_targets:
                    available_targets = objective_pool[obj_type]
            
            obj_target = random.choice(available_targets)
            used_targets.add(obj_target)
            
            # Generate appropriate amount based on the type and target
            if obj_type == "break":
                if obj_target in ["diamond_ore", "ancient_debris", "emerald_ore"]:
                    amount = random.randint(3, 8)  # Rare ores
                elif obj_target in ["gold_ore", "redstone_ore", "lapis_ore"]:
                    amount = random.randint(8, 16)  # Uncommon ores
                elif obj_target in ["iron_ore", "coal_ore"]:
                    amount = random.randint(16, 32)  # Common ores
                elif "deepslate" in obj_target:
                    amount = random.randint(8, 24)  # Deepslate variants
                elif obj_target == "stone":
                    amount = random.randint(64, 128)  # Very common
                elif obj_target in ["pumpkin", "melon"]:
                    amount = random.randint(8, 16)
                else:
                    amount = random.randint(16, 48)  # Default for crops etc.
            elif obj_type == "kill":
                if obj_target in ["ender_dragon", "wither"]:
                    amount = 1  # Boss mobs
                elif obj_target in ["elder_guardian", "ravager", "evoker"]:
                    amount = random.randint(1, 3)  # Mini-boss mobs
                elif obj_target in ["blaze", "enderman", "witch", "phantom"]:
                    amount = random.randint(5, 15)  # Harder mobs
                else:
                    amount = random.randint(15, 30)  # Common mobs
            elif obj_type == "interact":
                if obj_target in ["enchanting_table", "anvil", "beacon"]:
                    amount = random.randint(1, 5)  # Special blocks
                elif obj_target in ["water"]:  # For fishing
                    amount = random.randint(10, 25)
                else:
                    amount = random.randint(1, 10)  # General interactions
                    
            # Create description based on objective
            if obj_type == "break":
                obj_description = f"Mine {amount} {obj_target.replace('_', ' ')}"
            elif obj_type == "kill":
                if amount == 1:
                    obj_description = f"Defeat the {obj_target.replace('_', ' ')}"
                else:
                    obj_description = f"Kill {amount} {obj_target.replace('_', ' ')}s"
            elif obj_type == "interact":
                if obj_target == "water" and category == "fishing":
                    obj_description = f"Catch {amount} fish"
                else:
                    obj_description = f"Interact with {amount} {obj_target.replace('_', ' ')}"
                
            objectives[obj_id] = {
                "type": obj_type,
                "target": obj_target,
                "amount": amount,
                "description": obj_description
            }
            
        # Generate rewards
        rewards = {}
        
        # Money reward (almost always present)
        if random.random() < 0.95:  # 95% chance
            if category == "special":
                money = random.randint(500, 10000)
            elif category == "daily":
                money = random.randint(100, 500)
            else:
                money = random.randint(50, 300) * (len(objectives) + 1)  # Scale with objectives
            rewards["money"] = money
            
        # XP reward (common)
        if random.random() < 0.8:  # 80% chance
            if category == "special":
                xp = random.randint(250, 5000)
            elif category == "daily":
                xp = random.randint(50, 250)
            else:
                xp = random.randint(25, 150) * (len(objectives) + 1)  # Scale with objectives
            rewards["xp"] = xp
            
        # Item rewards (sometimes)
        if random.random() < 0.4:  # 40% chance
            # Common item rewards based on category
            category_items = {
                "mining": ["iron_pickaxe", "diamond", "iron_ingot", "gold_ingot", "emerald"],
                "combat": ["iron_sword", "bow", "arrow", "shield", "golden_apple"],
                "farming": ["bread", "golden_carrot", "apple", "cake", "pumpkin_pie"],
                "exploration": ["map", "compass", "spyglass", "ender_pearl", "elytra"],
                "fishing": ["fishing_rod", "nautilus_shell", "tropical_fish", "pufferfish", "sea_pickle"],
                "daily": ["experience_bottle", "iron_ingot", "emerald", "diamond", "golden_apple"],
                "special": ["diamond_sword", "diamond_pickaxe", "diamond_axe", "enchanted_golden_apple", "dragon_egg"]
            }
            
            # Choose 1-3 UNIQUE items from the category's item pool
            num_items = random.randint(1, min(3, len(category_items[category])))
            items = random.sample(category_items[category], num_items)
                
            # If multiple items, use a list format, otherwise a string
            if len(items) > 1:
                rewards["item"] = items
            else:
                rewards["item"] = items[0]
                
        # Command rewards (rarely, and mainly for special quests)
        if category == "special" and random.random() < 0.7:  # 70% chance for special quests
            possible_commands = [
                "effect give %player% regeneration 30 1",
                "give %player% diamond 5",
                "give %player% enchanted_golden_apple 1",
                "give %player% experience_bottle 10",
                "broadcast %player% has completed a special quest!",
                "lp user %player% permission set special.quest true"
            ]
            
            # Choose 1-3 UNIQUE commands
            num_commands = random.randint(1, min(3, len(possible_commands)))
            commands = random.sample(possible_commands, num_commands)
            
            # If multiple commands, use a list format, otherwise a string
            if len(commands) > 1:
                rewards["command"] = commands
            else:
                rewards["command"] = commands[0]
        elif random.random() < 0.1:  # 10% chance for other categories
            rewards["command"] = "effect give %player% regeneration 30 1"
        
        # Create quest entry
        quests_config["quests"][quest_id] = {
            "title": title,
            "description": description,
            "category": category,
            "repeatable": repeatable,
            "cooldown-hours": cooldown_hours,
            "objectives": objectives,
            "rewards": rewards
        }
    
    return quests_config

def save_quests_config(config: Dict[str, Any], output_file: str = "quests.yml") -> None:
    """Save the quest configuration to a YAML file."""
    # Ensure directory exists
    os.makedirs(os.path.dirname(output_file) if os.path.dirname(output_file) else '.', exist_ok=True)
    
    # Write the YAML file with proper formatting
    with open(output_file, 'w') as file:
        yaml.dump(config, file, default_flow_style=False, sort_keys=False)
    
    print(f"Generated quest configuration saved to {output_file}")

def main() -> None:
    """Run the quest generator."""
    import argparse
    
    parser = argparse.ArgumentParser(description='Generate quest configurations for Minecraft NotQuests plugin')
    parser.add_argument('-n', '--num-quests', type=int, default=20, help='Number of quests to generate (default: 20)')
    parser.add_argument('-o', '--output', type=str, default='quests.yml', help='Output file (default: quests.yml)')
    args = parser.parse_args()
    
    print(f"Generating {args.num_quests} quests...")
    quests_config = generate_quests(args.num_quests)
    save_quests_config(quests_config, args.output)
    
    # Print summary of generated quests
    categories = {}
    for quest_id, quest in quests_config["quests"].items():
        category = quest["category"]
        categories[category] = categories.get(category, 0) + 1
    
    print("\nGenerated Quest Summary:")
    print(f"Total Quests: {len(quests_config['quests'])}")
    for category, count in sorted(categories.items()):
        print(f"  - {category.capitalize()}: {count} quests")
    
    repeatable = sum(1 for quest in quests_config["quests"].values() if quest["repeatable"])
    print(f"Repeatable Quests: {repeatable}")
    print(f"One-time Quests: {len(quests_config['quests']) - repeatable}")

if __name__ == "__main__":
    main()