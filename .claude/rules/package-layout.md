# Package layout: `core/` vs `source/`

This split is identical in every module in the repo — apply it without re-deriving it per
module:

- `core/` holds `internal`/`private` declarations only — infrastructure, wiring, helpers not
  meant to be consumed outside the module.
- `source/` holds everything `public` — the module's actual API surface — grouped into one
  subfolder per semantic concern (`source/activity/`, `source/datasource/`,
  `source/usecase/configuration/`, etc.).
- A module with no `internal` declarations has **no `core/` directory at all** — don't create
  an empty one "for consistency" (see `domain-usecase-api`, `data-database-api`,
  `domain-repository-api`). Add `core/` only the moment you introduce something that needs to
  be `internal`.
- `explicitApi()` is enforced repo-wide (set by the convention plugins) — every `public`
  declaration needs an explicit visibility modifier and an explicit return type; this is what
  actually polices the `core/`-vs-`source/` boundary at compile time, not just convention.
