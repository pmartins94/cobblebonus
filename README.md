# CobbleBonus (NeoForge 1.21.x)

CobbleBonus provides permanent, per-player multipliers for Cobblemon shiny chance and capture catch rate. All modifiers are permanent and stack by multiplication only (no additive bonuses).

## Install
1. Install NeoForge for Minecraft 1.21.x on your server.
2. Install Cobblemon (NeoForge) 1.7.3+.
3. Drop the CobbleBonus jar into your server's `mods/` folder.
4. (Optional) Edit `config/cobblebonus-server.toml` to adjust multiplier caps and catch rate clamp.

This is a server-side mod. Clients do not need to install it.

## Commands (OP-only)
Root command: `/cobblebonus`

### Shiny modifiers
```
/cobblebonus shiny modifier add <target> <id> <multiplier>
/cobblebonus shiny modifier remove <target> <id>
/cobblebonus shiny modifier list <target>
/cobblebonus shiny modifier clear <target>
/cobblebonus shiny effective <target>
```

### Capture modifiers
```
/cobblebonus capture modifier add <target> <id> <multiplier>
/cobblebonus capture modifier remove <target> <id>
/cobblebonus capture modifier list <target>
/cobblebonus capture modifier clear <target>
/cobblebonus capture effective <target>
```

### Multiplier math
Modifiers are multiplicative and permanent. For example:
- A multiplier of `1.10` is a **+10%** relative increase.
- Two modifiers `1.10` and `1.25` combine to `1.10 * 1.25 = 1.375` (a **+37.5%** increase).

Each modifier is keyed by a string ID. Adding a modifier with an existing ID overwrites the previous value.

## Config (server)
`config/cobblebonus-server.toml`:
```
[caps]
maxEffectiveShinyMultiplier = 1000.0
maxEffectiveCaptureMultiplier = 100.0
maxCatchRate = 255.0
```

## FTB Quests examples
Use fixed string IDs per quest to keep rewards idempotent.

```
# +10% shiny chance for the player who completes the quest
/cobblebonus shiny modifier add @p quest_shiny_bonus 1.10

# +15% capture rate
/cobblebonus capture modifier add @p quest_capture_bonus 1.15

# Remove a quest reward later (e.g., admin cleanup)
/cobblebonus shiny modifier remove @p quest_shiny_bonus
```
