# Architecture & Technical Standards — `plugin-spigotmc`

## Repository Scope

This repository is the **`spigotmc`** platform module of the MCEconomy project, split out into
its own repository. It is the SpigotMC plugin entry point (`MCEconomyPlugin`) and produces the
distributable shaded jar. It publishes the Maven artifact `io.github.mcclauneck:spigotmc`.

## Rules

- This module is dedicated to handling platform-specific implementations and integrations for
  SpigotMC. Shared logic belongs upstream in `plugin-api`, `plugin-common`, or `plugin-bukkit`.
- Follow `.agents/platforms/spigotmc/concurrency.md` for scheduling and thread-safety.
- The shadow jar relocates `io.github.mcengine` to `io.github.mcclauneck`. Keep this relocation.
- Do NOT change the Maven artifactId. `rootProject.name` MUST remain `spigotmc`, even though the
  repository is named `plugin-spigotmc`, so published coordinates stay `io.github.mcclauneck:spigotmc`.

## Dependencies

Resolved from GitHub Packages and public repositories:

- `io.github.mcclauneck:api` — `MCClauneck/plugin-api`.
- `io.github.mcclauneck:common` — `MCClauneck/plugin-common`.
- `io.github.mcclauneck:bukkit` — `MCClauneck/plugin-bukkit`.
- `io.github.mcengine:manager`, `io.github.mcengine:spigotmc` — `MCEngine/mcextension`.
- `org.spigotmc:spigot-api` (`compileOnly`).
