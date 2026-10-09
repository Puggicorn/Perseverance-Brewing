# Perseverance: Brewing

The flagship of the Perseverance series and total-conversion mod for **Minecraft 1.21.1** that overhauls brewing into a modular, purely **data-driven alchemical pipeline**.

## Ingredient steps:

### Step 1: Brewing a Catalyst into a potion base
Brew a **Catalyst** onto a water bottle to create a potion base, each base has it's own unique method of picking effects from a Reagent.

### Step 2: Using Reagents
Every **Reagent** holds at least 1 potion effect, the potion base you use will determine *how those effects are picked.* One base could grant the first effect in the list, while another might randomly pick from the list.

### Step 3: Converter ingredients
A **Converter** allows "converting" existing status effects into other effects. ex: Poison -> Instant harming

### Step 4: Additives
Additives are ingredients that modify the stats of a potion, such as redstone and glowstone. Each potion gets *1* additive to work with.

### Step 5: Container Modifiers
Modifiers allow you to change the type of potion you have. Such as normal -> splash -> lingering using gunpowder then dragons breath respectively.

---

## The Centrifuge

Splits a multi-effect potion back into clean potions. Craft it like a brewing stand: a **Breeze Rod
over 3 Copper Blocks**.

- **Top slot:** one potion with effects to separate.
- **Bottom 3 slots:** water bottles or awkward potions (these become the results).
- **Corner slot:** **Breeze Powder** fuel (crafted shapeless from a Breeze Rod → 2).

Press **Start**. Each effect moves into its own bottle; if there are fewer bottles than effects,
the extras land in random bottles.


---

## New effects!

We have added a few new effects to sink your teeth into, and have made a small adjustment to some of the vanilla ones.

### New effects
* Climbing: A simple effect that allows you to climb on any block as if it was a ladder or vine.
* Burning rage: Causes anything inflicted to uncontrollably lash out at other entities, while suffering burn damage. *Plus other things.

#### Adjusted effects
* Saturation now applies as if consumed via mushroom stew, even as a potion
* Glowing now exposes your location to monsters


---

## Datapack JSON Examples

Every stage of the brewing pipeline is fully data-driven for easy compatibility with other mods!
#### Follow the filepath instructions below.
### 1. Catalyst
* **Path:** `data/<namespace>/alchemy/catalysts/example_base.json`

```json
{
  "catalyst": {
    "item": "minecraft:nether_wart"
  },
  "base_strategy": "pure",
  "base_id": "example_id",
  "color": "#951414"
}
```
*Note: ingredient IDs are used for the language keys

### 2. Reagent
* **Path:** `data/<namespace>/alchemy/reagents/spider_eye.json`
```json
{
  "reagent": {
    "item": "minecraft:spider_eye"
  },
  "effects": [
    {"id": "minecraft:poison", "duration": 900, "amplifier": 0},
    {"id": "perseverance_brewing:climbing", "duration": 3600, "amplifier": 0},
    {"id": "minecraft:night_vision", "duration": 3600, "amplifier": 0},
    {"id": "minecraft:invisibility", "duration": 3600, "amplifier": 0}
  ]
}
```
*Note: You can add as many effects as you want, the base system will filter them out for you.

### 3. Converter
* **Path:** `data/<namespace>/alchemy/converters/fermented_eye.json`

```json
{
  "converter": {
    "item": "minecraft:fermented_spider_eye"
  },
  "conversions": {
    "minecraft:night_vision": {
      "target": "minecraft:invisibility",
      "duration": 1.0,
      "amplifier": 1.0
    },
    "minecraft:speed": {
      "target": "minecraft:slowness",
      "duration": 0.5,
      "amplifier": 1.0
    },
    "minecraft:poison": "minecraft:instant_damage",
    "minecraft:instant_health": "minecraft:instant_damage"
  }
}
```
*Note: Converting effects already present on a potion will give a fairly strong bonus.

### 4. Additive
* **Path:** `data/<namespace>/alchemy/additives/redstone.json`

```json
{
  "additive": {
    "item": "minecraft:redstone"
  },
  "duration_multiplier": 2.66666666667,
  "durationFlatBonus": 0,
  "amplifierIncrease": 0,
  "maxAmplifierLimit": 0,
  "additiveID": "extended"
}
```

### 5. Modifier
* **Path:** `data/<namespace>/alchemy/modifiers/dragons_breath.json`

```json
{
  "modifier": {
    "item": "minecraft:dragon_breath"
  },
  "modifier_id": "lingering",
  "target_item": "minecraft:lingering_potion",
  "valid_input_item": "minecraft:splash_potion",
  "duration_multiplier": 0.25
}
```