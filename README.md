# cobblebonus

CobbleBonus is a server-side NeoForge mod for Minecraft 1.21.1 that adds permanent, per-player shiny and capture chance multipliers for Cobblemon.

## Install

1. Install NeoForge for Minecraft 1.21.1.
2. Install Cobblemon (NeoForge 1.7.3+).
3. Drop the CobbleBonus jar into your server's `mods/` folder.
4. Start the server once to generate the config, then edit `config/cobblebonus-server.toml` if desired.

## Commands (OP-only)

```
/cobblebonus shiny modifier add <target> <uuid> <multiplier> [name...]
/cobblebonus shiny modifier remove <target> <uuid>
/cobblebonus shiny modifier list <target>
/cobblebonus shiny modifier clear <target>
/cobblebonus shiny effective <target>

/cobblebonus capture modifier add <target> <uuid> <multiplier> [name...]
/cobblebonus capture modifier remove <target> <uuid>
/cobblebonus capture modifier list <target>
/cobblebonus capture modifier clear <target>
/cobblebonus capture effective <target>
```

## FTB Quests examples

Give a permanent shiny multiplier reward:

```
/cobblebonus shiny modifier add @p 2d0b94c9-3b84-4b4e-9a5d-4e0b42b4e2c1 2.0 "Quest Shiny Reward"
```

Give a permanent capture multiplier reward:

```
/cobblebonus capture modifier add @p 6a8cbd8a-7c9d-4d0f-9d4c-2d4e2c7b1e3a 1.25 "Quest Capture Reward"
```

Use unique UUIDs per quest so repeating the command overwrites the same reward instead of stacking.
