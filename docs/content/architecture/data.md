# Data and persistence

Three separate stores, for three different jobs.

## Preferences

`data-preference-api` defines one generic contract:

```kotlin
public interface PreferenceSource<T> {
    public suspend fun getData(): T
    public suspend fun setData(data: T)
    public fun observeData(): Flow<T>
}
```

One typed interface per preference (`ThemePreferenceSource : PreferenceSource<ThemePreference>`),
one implementation in `data-preference-impl`. Each holds a `MutableStateFlow` seeded from the
backing store on construction: `setData` writes through and updates the flow, `observeData`
returns it, and `getData` re-reads fresh so the first call is correct before anything has
subscribed.

The backend is `multiplatform-settings` over `java.util.prefs.Preferences`, under the node
`dev.rectify.io`.

Keys follow `pref_<name>` and are declared as constants in the implementation. **Never change
an existing key** — it silently orphans the stored value on every existing install. Add a new
key and migrate.

## History — Room

`data-database-api` holds only generic, composable base interfaces: `ReadableDataSource<T>`,
`ObservableDataSource<T>`, `AppendableDataSource<T>`, `UpsertableDataSource<T>`. No
entity-specific interface lives there — those are defined where they are consumed, by mixing in
whichever bases apply.

`data-database-impl` is the Room implementation: two tables, `history_entry` (one row per sent
batch) and `history_annotation` (its per-rect children, with
`FOREIGN KEY … ON DELETE CASCADE`, indexed on `batchId`). The database file is
`~/.rectify/rectify.db`.

### No `@ConstructedBy`

That annotation and `RoomDatabaseConstructor` are the Room **KMP** pattern, needed only when an
`expect object` constructor has to be generated per target. This is a plain JVM project: Room
resolves `AppDatabase_Impl` itself and `AppDatabase` carries `@Database` alone. KSP is wired
with the ordinary `ksp(libs.room.compiler)` configuration — no per-target entries.

### Migrations

`exportSchema = true`; the exported JSON for version 1 is in `schemas/`. Destructive migration
is **disabled** — there is no fallback, so a missing migration crashes the app on an existing
install.

The current schema shipped at version 1 with no `Migration`, which is an exception rather than
the rule: the same change relocated the database from `~/rectify.db` to `~/.rectify/rectify.db`,
a path nothing had ever written to, which is what made skipping it provably safe. The next
change touching either table on the *current* path needs a real migration.

## The handoff slot

`data-handoff-api` is the contract shared between the desktop app and the MCP server. It is the
only module both processes depend on.

It deliberately does **not** apply the DI convention plugin. That plugin exposes `koin-core` as
`api()`, which would put Koin on the MCP server's classpath for no benefit. The Koin binding for
`AnnotationHandoff` lives in `presentation-feature-mirror` instead.

The slot is a file at `~/.rectify/handoff`, with bulk bytes under `~/.rectify/history/`. Both
are created on demand, owner-only (`rwx------`) where the filesystem supports POSIX
permissions — `~` is world-readable on a typical Linux box, and these files contain screenshots
of whatever was on the device.

A file is the right primitive here precisely because it outlives both processes. Rectify can
close and the batch is still readable; Claude Code can start afterwards and still get it.

## Mappers

Data-layer resources never cross into the domain. `data-repository-impl/core/mapper/` converts
between them via `ModelResourceMapper`, whose `toModel` and `toResource` are plain functions —
which is why they are some of the best-tested code in the repository.
