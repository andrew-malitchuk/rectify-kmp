# docs/

Source for the documentation site published to GitHub Pages.

```
docs/
├── site.json          # nav manifest — section order, page titles, slugs
├── content/           # the Markdown, one file per page
└── assets/            # stylesheet, search script, favicon, README banner
```

Markdown is the single source of truth. The renderer lives in `:docs-generator`.

## Working on it

```shell
./gradlew generateDocs -PdocsBaseUrl=.          # relative links — browsable from disk
open docs-generator/build/docs-site/index.html
```

or, closer to how it is actually served:

```shell
./gradlew generateDocs
python3 -m http.server -d docs-generator/build/docs-site 8000
```

`-PdocsBaseUrl` controls how links are written, and the three modes exist for three different
hosts:

| Value | Links look like | For |
|---|---|---|
| *(omitted)* | `/guide/devices/` | A site served from a domain root |
| `/rectify-kmp` | `/rectify-kmp/guide/devices/` | A GitHub Pages **project** site — what CI passes |
| `.` | `../guide/devices/index.html` | `file://`, or any unknown mount point |

## Adding a page

1. Write the Markdown under `content/`.
2. Add an entry to `site.json` — `title`, `file`, `slug`, `description`.

The generator fails loudly if a manifest entry points at a file that is not there, so a typo
is a build error rather than a missing page nobody notices.

Raw HTML passes through, which is how the landing page gets its hero and card grid. The
stylesheet already carries `.card`, `.cta`, `.hero` and the signal `.dot-*` classes.

## Publishing

`.github/workflows/docs.yml` builds and deploys on every push to `main` that touches `docs/`,
the generator, or the version catalogue. It derives the base path from the repository name, so
renaming the repo does not silently 404 every stylesheet.

**One-time setup in the repository:** Settings → Pages → Source → **GitHub Actions**.
