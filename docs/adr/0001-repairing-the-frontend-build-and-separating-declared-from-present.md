# ADR-0001: Repair the frontend build, and separate what is declared from what is present

## Status

Accepted (2026-08-21).

## Context

`app-gyotaku` is a migrated repository: `migration.edn` records its origin as
`etzhayyim/root` at `60-apps/etzhayyim-project-gyotaku`, **16 tracked files /
10,958 bytes**, with `README.edn` and `migration.edn` as the only permitted
additions. The tree today is exactly those 18 files. The migration was faithful;
the runtime was never inside that path.

Two problems followed from that, and they compound.

**1. The build did not work.** `npm install` in
`appview/etzhayyim-wasm-gyotaku-i3zinrs2/svelte` failed with `ERESOLVE`:

- `@sveltejs/vite-plugin-svelte@4.0.4` peer-requires `vite@^5`, but `package.json`
  pins `vite@^6.4.2`.
- `@sveltejs/adapter-static@^3.0.10` transitively requires `@sveltejs/kit`, a
  SvelteKit leftover. There is no `svelte.config.js`, no SvelteKit import, and
  no reference to the adapter anywhere in the tree — `App.svelte` itself is
  labelled *"Vite entry scaffold after SvelteKit cleanup"*. The cleanup removed
  SvelteKit but left its adapter in the manifest.

`npm run check` additionally exited 1 — svelte-check resolves Svelte config
through `svelte.config.js`, which did not exist, so it reported *"No Svelte
configuration found in vite config"* even though `vite.config.ts` registers the
plugin correctly.

Because nothing installed, nothing built, and no step in this repository was
verifiable at all.

**2. Four documents described four different systems.** None matched the tree:

| Source | Describes |
|---|---|
| `README.md` | crawler ingest, Common Crawl CDX/Range, kotodama WIT Arrow `gyotaku_snapshots`, MCP tools + REST, `/xrpc` |
| `PROJECT.jsonld` | `"stack": "go"`, gRPC routes at `/api/grpc` |
| `CLAUDE.md` | AT Protocol appview, `did:web:gyotaku.etzhayyim.com`, five `com.etzhayyim.apps.gyotaku.*` XRPC commands |
| `appview/*/kotodama.jsonld` | two components, both pointing at a `component.wasm` |

Measured against the tracked tree: **0 `.wasm` files, 0 `.go` files**, no
handler source, and a placeholder `App.svelte`. A reader could not tell which
document to believe, and each one individually reads as a description of
working software.

## Decision

**Repair the build, then state the gap instead of narrating around it.**

1. Drop `@sveltejs/adapter-static` — unreferenced, and the sole cause of the
   `@sveltejs/kit` peer conflict.
2. Raise `@sveltejs/vite-plugin-svelte` to `^5`, which accepts Vite 6.
3. Add a minimal `svelte.config.js` so `npm run check` can resolve config.
4. Rewrite `README.md` so that **what is present** and **what is declared** are
   two clearly separated sections, and add `docs/operator-quickstart.md`
   recording only commands that were actually executed, including an explicit
   "what you cannot do" section.

We deliberately did **not** implement the archive runtime. That is a real
project, not a documentation fix, and inventing a partial one would recreate
exactly the ambiguity this ADR removes.

## Consequences

- `npm install`, `npm run build`, `npm run check`, and `npm run preview` all
  succeed and are recorded with their real output (see quickstart).
- The descriptors are now labelled as intent rather than implementation, so the
  next contributor knows what is missing without reverse-engineering the tree.
- The `component.wasm` referenced by both `kotodama.jsonld` files, the Go stack
  in `PROJECT.jsonld`, and the XRPC surface in `CLAUDE.md` remain open work,
  named in the README as such.
- Tailwind is configured but no CSS entrypoint is imported, so it does not reach
  the bundle. Left in place and documented rather than silently removed — the
  intent to use it is legible, and deleting config is not obviously right.
- `package-lock.json` is now committed, so the output recorded in the quickstart
  stays reproducible instead of drifting with the next Vite or Svelte release.
  A `.gitignore` was added for `node_modules/` and `dist/`; neither could appear
  before, because the install failed.

## Verification

Executed on Node v26.3.0 / npm 11.16.0:

- `npm install` → `added 118 packages` (previously: `ERESOLVE` failure)
- `npm run build` → 110 modules transformed, `dist/` emitted
- `npm run check` → `112 FILES 0 ERRORS 0 WARNINGS`, exit 0 (previously: exit 1)
- `npm run preview` → `HTTP 200` on `/` and on the hashed JS asset
- all three `.jsonld` and both `.edn` descriptors parse
