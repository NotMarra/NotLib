import json

materials = [x.split("(")[0] for x in open("item_atlas_material.txt", "r").readlines() if x]

content = open("item_atlas.json", "r").read()
jsn: dict[str, dict] = json.loads(content)

items: list[str] = []
blocks: list[str] = []
for name in jsn.keys():
    if name.startswith('minecraft:item'):
        items.append(name)
    else:
        blocks.append(name)

java = "public class NotFontBlock {\n"
for name in blocks:
    prop = name.split("/")[-1].upper()
    java += f"    public static final Character {prop} = '\\u{jsn[name]['code']}';\n"

java += "\n\n    public static Character fromMaterial(Material material) {\n"

for name in blocks:
    prop = name.split("/")[-1].upper()
    if prop in materials:
        java += f"        if (material == Material.{prop}) return {prop};\n"

java += "        return Material.AIR;\n"
java += "    }\n"
java += "}"

java += "\n\n"

java += "public class NotFontItem {\n"
for name in items:
    prop = name.split("/")[-1].upper()
    java += f"    public static final Character {prop} = '\\u{jsn[name]['code']}';\n"

java += "\n\n    public static Character fromMaterial(Material material) {\n"

for name in items:
    prop = name.split("/")[-1].upper()
    if prop in materials:
        java += f"        if (material == Material.{prop}) return {prop};\n"

java += "        return Material.AIR;\n"
java += "    }\n"
java += "}"

with open("item_atlas.java", "w") as f:
    f.write(java)