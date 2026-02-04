# Project rules (CobbleBonus V1)

## Targets
- NeoForge mod for Minecraft 1.21.x
- Cobblemon (NeoForge) compatible
- Server-only must work (client optional)

## Core rules
- V1: permanent modifiers only (NO duration/expiry)
- Stacking: MULTIPLY-only
- Modifiers are keyed by UUID and must be idempotent:
  - "add" with same UUID overwrites existing modifier
- Provide OP-only commands for add/remove/list/clear/effective

## Cobblemon hooks (STRICT)
Use ONLY these Cobblemon events (do not invent others):
1) com.cobblemon.mod.common.api.events.pokeball.PokemonCatchRateEvent
   - modify var catchRate (Float) when thrower is ServerPlayer

2) com.cobblemon.mod.common.api.events.pokemon.ShinyChanceCalculationEvent
   - use addModificationFunction((Float, ServerPlayer?, Pokemon) -> Float)
   - DO NOT call isShiny()

## Output quality
- ./gradlew build must succeed
- Include README.md with install + FTBQuests command examples
- Keep implementation minimal and documented
