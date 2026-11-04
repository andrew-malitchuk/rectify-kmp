---
paths:
  - "presentation-core-ui/**/atom/icon/**"
  - "icons/**"
  - "desktop-application/build.gradle.kts"
  - "tools/render-banner.swift"
  - "docs/assets/banner.svg"
---

# Icons and branding

Two separate things live under the word "icon" here. Don't mix them.

## The app icon (`icons/`)

The brand mark — a phone outline with a blue sparkle on a light rounded square. `rectify.svg`
is the **source of truth**; the three raster formats beside it are generated, never hand-edited:

| File | Consumer |
|---|---|
| `rectify.icns` | macOS bundle — `nativeDistributions { macOS { iconFile } }` |
| `rectify.ico` | Windows installer |
| `rectify.png` | Linux package **and** the runtime window icon |

Regenerate with `./icons/regenerate.sh` after changing the SVG. It is macOS-only and it uses a
small AppKit renderer (`render-svg.swift`) rather than an off-the-shelf tool, for reasons
recorded in the script: `qlmanage` bakes an opaque white ground and misplaces the art at small
sizes, and the installed `cairosvg` is broken by a Homebrew python/libexpat mismatch. An app
icon with white corners instead of transparent ones is the failure that matters here.

Three places consume the artwork, and they are deliberately different mechanisms:

- **Packaged app** — `nativeDistributions` in `desktop-application/build.gradle.kts`.
- **`./gradlew run` on macOS** — nothing extra to write. The Compose plugin turns
  `macOS.iconFile` into `-Xdock:icon` for the run task by itself. Do NOT add a `jvmArgs`
  entry for this: a second `-Xdock:icon` overrides the first, so a hand-rolled one quietly
  downgrades the multi-resolution `.icns` to whatever single raster it names.
- **Window icon** — `Window(icon = painterResource("rectify.png"))` in `RectifyWindow`.
  `icons/` is wired in as a resource root of `:compose-application` (PNG only) so the runtime
  reads the same file the packaging does instead of a second copy. Inert on macOS, which draws
  no window icon; real on Windows and Linux.

**The mark is full-bleed** — the rounded square touches all four edges. Apple's own icons sit
inset within their canvas, so in the Dock this one reads slightly larger than its neighbours.
That is how the asset was supplied; change the SVG, not the pipeline, if it should be inset.

## In-app icons (`presentation-core-ui/.../kit/atom/icon/`)

A different family: 22 monochrome, stroke-based, 24×24 Lucide-style vectors drawn in
`Color.Black` and tinted by the caller. The app icon above does **not** belong here — it is
multi-colour and has its own background.

`RectifyLogo.kt` and `AppIcon.kt` were both deleted as dead code when the splash screen went.
If an in-app brand mark is wanted again, add ONE, and give it a real call site in the same
change — an `ImageVector` nothing renders is the exact dead weight that removal cleared out.

## The README banner (`docs/assets/banner.svg`)

Generated, never hand-edited — `swift tools/render-banner.swift` rewrites it from the strings,
colours and layout in that script. It reuses the mark's own paths from `rectify.svg` (minus the
rounded square, which *is* the banner ground, and with the sparkle moved to the phone's top
corner), so the banner and the app icon can never drift apart.

**Every string is converted to glyph outlines.** GitHub serves a README SVG as an `<img>`, which
loads no external fonts and honours no `@font-face` the page sets, so live `<text>` would fall
back to whatever each visitor's browser picks. Embedding the TTFs as base64 instead costs ~1.8 MB
against the outlines' 68 KB.

The faces are the product's own — `roboto_flex.ttf` and `jetbrains_mono.ttf`, read straight out of
`presentation-core-styling/src/main/composeResources/font/`, the module that ships them to the
app, and the same two the docs site pulls from Google Fonts. Do not reach for a font outside that
pair: the app, the site and the banner are deliberately one typographic system. Both are variable
fonts, so a weight is an axis value (`wght`, plus `opsz` for Roboto Flex — CoreText does not infer
optical size), not a second file.

It lives under `docs/assets/` because `:docs-generator` copies that directory wholesale into the
published site, so the banner is fetchable from the docs host too. The generator stays in
`tools/` for the same reason — anything beside it ships.

The banner is **light-only** (`#EDEDEB` ground). That is why it is a README asset and not part
of the docs-site hero, which has a theme toggle. A dark variant would need a second render and a
`<picture>` switch in the README.
