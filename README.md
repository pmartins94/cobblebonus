# Cobblemon-Bonus (NeoForge 1.21.x)

Cobblemon-Bonus provides permanent, per-player multipliers for Cobblemon shiny chance and capture catch rate. All modifiers are permanent and stack by multiplication only (no additive bonuses). Capture multipliers now act like additional invisible attempts against the final capture result, so they stack naturally with ball bonuses and feel stronger in-game.

## Install
1. Install NeoForge for Minecraft 1.21.x on your server.
2. Install Cobblemon (NeoForge) 1.7.3+.
3. Drop the Cobblemon-Bonus jar into your server's `mods/` folder.
4. (Optional) Edit `config/cobblebonus-server.toml` to adjust multiplier caps and catch rate clamp.

This is a server-side mod. Clients do not need to install it.

## Commands (OP-only)
Root command: `/cobblebonus`

### Shiny modifiers (examples)
```
/cobblebonus shiny modifier add <target> <id> <multiplier>
/cobblebonus shiny modifier remove <target> <id>
/cobblebonus shiny modifier list <target>
/cobblebonus shiny modifier clear <target>
/cobblebonus shiny effective <target>
```
The `<id>` is a string identifier. Numeric IDs like `2` are allowed.

Example:
```
# Add a permanent +25% shiny multiplier to Skinstadd
/cobblebonus shiny modifier add Skinstadd shiny_bonus 1.25

# Verify the effective multiplier
/cobblebonus shiny effective Skinstadd
```

### Capture modifiers (examples)
```
/cobblebonus capture modifier add <target> <id> <multiplier>
/cobblebonus capture modifier remove <target> <id>
/cobblebonus capture modifier list <target>
/cobblebonus capture modifier clear <target>
/cobblebonus capture effective <target>
```

Example:
```
# Add a permanent +50% capture multiplier to all online players
/cobblebonus capture modifier add @a capture_bonus 1.50

# Verify the effective multiplier
/cobblebonus capture effective @a
```

### Multiplier math
Modifiers are multiplicative and permanent. For example:
- A multiplier of `1.10` is a **+10%** relative increase.
- Two modifiers `1.10` and `1.25` combine to `1.10 * 1.25 = 1.375` (a **+37.5%** increase).
For shiny odds, multipliers divide the base denominator (e.g., `1/8000` with `x2` becomes `1/4000`).
For capture, the multiplier grants extra invisible attempts at the final capture calculation, so an Ultra Ball's bonus still applies and a quest buff feels noticeable (e.g., Ultra Ball `x2` plus quest buff `x3` makes catches feel much easier).

Each modifier is keyed by a string ID. Adding a modifier with an existing ID overwrites the previous value.

### How to check your current multiplier
Use the `effective` command to see the final multiplier after all modifiers are combined and capped:
```
/cobblebonus shiny effective <target>
/cobblebonus capture effective <target>
```

## Config (server)
`config/cobblebonus-server.toml`:
```
[caps]
maxEffectiveShinyMultiplier = 1000.0
maxEffectiveCaptureMultiplier = 100.0
maxCatchRate = 255.0

[capture]
enableCaptureRerolls = true
maxCaptureAttempts = 20
```

Config meanings:
- `maxEffectiveShinyMultiplier`: Maximum combined shiny multiplier after all modifiers are multiplied.
- `maxEffectiveCaptureMultiplier`: Maximum combined capture multiplier after all modifiers are multiplied.
- `maxCatchRate`: Hard clamp on the final catch rate value after the capture multiplier is applied.
- `enableCaptureRerolls`: When true, extra capture attempts are rolled based on the effective capture multiplier.
- `maxCaptureAttempts`: Hard cap on the total attempts (original + rerolls) to prevent extreme multipliers from spamming rerolls.

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
