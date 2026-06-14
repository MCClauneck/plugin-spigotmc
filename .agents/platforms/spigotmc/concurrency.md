# SpigotMC Concurrency & Standards

- Use the standard Bukkit scheduler (`BukkitScheduler`) for task management and scheduling.
- Ensure all heavy computations, API calls, and database interactions run asynchronously off
  the main server thread (`runTaskAsynchronously`).
- Synchronize back to the main thread (`runTask`) only when interacting with thread-unsafe
  Bukkit API methods.
- SpigotMC has no Adventure API guarantee; use the legacy `String`-based messaging APIs for
  player messages and keep message text configurable.
