# MCEconomy SpigotMC (`plugin-spigotmc`)

The **SpigotMC** platform entry point for the **MCEconomy** multi-platform Minecraft economy
plugin. It wires the shared modules together into a single distributable, shaded jar.

> Part of the MCEconomy multi-repository project. This repository was split out of the
> original `plugin` monorepo. The Maven artifactId is kept as **`spigotmc`** so coordinates
> remain `io.github.mcclauneck:spigotmc`.

## Runtime flow

On `onEnable`, the plugin:

1. Loads `config.yml` and bails out early if `enable: false`.
2. Constructs the configured `IEconomyDatabase` (SQLite by default) and calls `initialize()`.
3. Creates the central `MCEconomyProvider` over that database.
4. `loadCurrencies()` — caches all currencies in memory.
5. `ensureDefaultCurrencies()` — generates the configured default currencies on first run.
6. Registers `PlayerJoinCurrencyListener` so defaults are re-verified on player join.
7. Boots the MCExtension system and registers the `/mceconomyextension` command.

All database operations are asynchronous and never block the main server thread. Follow the
SpigotMC scheduling conventions described in
[`.agents/platforms/spigotmc/concurrency.md`](.agents/platforms/spigotmc/concurrency.md).

## Dependencies

- `io.github.mcclauneck:api`, `:common`, `:bukkit` — the shared MCEconomy modules.
- `io.github.mcengine:manager`, `:spigotmc` — the MCExtension system (relocated into `io.github.mcclauneck`).
- `org.spigotmc:spigot-api` — `compileOnly`.

## Building

```bash
./gradlew build          # produces the shaded plugin jar
./gradlew shadowJar      # the distributable jar only
./gradlew exportJar      # also copies the jar to $SPIGOTMC_PLUGIN_JAR_PATH if set
```

The project uses the Gradle wrapper and targets a **Java 25** toolchain.

## Configuration

See [`src/main/resources/config.yml`](src/main/resources/config.yml). Set `enable: true` to
activate, choose a `database.type`, and customize the `currencies` list.

## Related repositories

- [`plugin-api`](https://github.com/MCClauneck/plugin-api), [`plugin-common`](https://github.com/MCClauneck/plugin-common), [`plugin-bukkit`](https://github.com/MCClauneck/plugin-bukkit) — shared modules.
- [`plugin-papermc`](https://github.com/MCClauneck/plugin-papermc), [`plugin-foliamc`](https://github.com/MCClauneck/plugin-foliamc) — the other platform entry points.

## License

See [LICENSE](LICENSE).
