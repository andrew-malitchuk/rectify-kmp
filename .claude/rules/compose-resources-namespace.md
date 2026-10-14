---
paths:
  - "presentation-core-localisation/**"
  - "settings.gradle.kts"
---

# Compose Resources generated namespace

String/font/drawable resources generate under a package derived from `rootProject.name`
(`settings.gradle.kts`): `rectify-kmp` → `rectify_kmp.<module>.generated.resources.*`
(hyphens become underscores). You'll see imports like
`import rectify_kmp.presentation_core_localisation.generated.resources.Res` and
`...generated.resources.settings_title` across every feature module.

- This package is **generated on every build** (`generateComposeResClass` and related
  compose-resources tasks) — never hand-edit a file under a `generated/` output directory, and
  never assume the import path is stable source you can grep-and-replace independently of
  `rootProject.name`.
- If `rootProject.name` ever changes, every one of these imports changes too, automatically, on
  the next build — don't go edit them by hand across the repo; a stale IDE index showing red
  imports right after the rename just needs a rebuild/resync, not a manual fix.
- New string resources go in
  `presentation-core-localisation/src/commonMain/composeResources/values/strings.xml`; the
  generated accessor (`Res.string.<key>`) appears automatically next build — there's no
  separate registration step.
