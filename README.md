# app-gyotaku

Time-series web archive ("魚拓") for pages crawled elsewhere in the etzhayyim
fleet — intended to let a reader pick a URL and walk back through its captures.

**Status: descriptors + frontend scaffold. The archive runtime is not in this
repository.** Read [What is actually here](#what-is-actually-here) before
planning work against it.

Start with **[`docs/operator-quickstart.md`](docs/operator-quickstart.md)** —
it builds the frontend from a clean clone in five commands, all verified.

## What is actually here

20 tracked files. Measured, not asserted:

| Path | What it is | State |
|---|---|---|
| `appview/etzhayyim-wasm-gyotaku-i3zinrs2/cljs/` | ClojureScript (shadow-cljs) + reagent + re-frame + jp-go-dds frontend | **builds and serves**; the view is a placeholder page |
| `appview/*/kotodama.jsonld` | two component descriptors (`gyotaku`, `gyotaku-mcp-component`) | parse; both point at a `component.wasm` that is not tracked |
| `PROJECT.jsonld` | project/route descriptor | parses; declares a Go gRPC component |
| `README.edn`, `migration.edn` | repository + migration metadata | parse |
| `CLAUDE.md` | intended XRPC command surface | design note only |

This repository was migrated from `etzhayyim/root`
(`60-apps/etzhayyim-project-gyotaku`, 16 files / 10,958 bytes) plus the two
metadata files `migration.edn` permits. The migration was complete; the runtime
simply never lived at that path.

## What is declared but not implemented

These are **intent**, recorded so the gap is legible. None of them is backed by
code in this tree:

- **Archive runtime** — `component.wasm` is referenced by both
  `kotodama.jsonld` files. No `.wasm` is tracked (0 files).
- **Go gRPC service** — `PROJECT.jsonld` declares `"stack": "go"` with routes at
  `/api/grpc`. No `.go` is tracked (0 files).
- **XRPC command surface** — `CLAUDE.md` specifies five commands
  (`searchSnapshots`, `listDomains`, `getSnapshot`, `getTimeline`, `getStats`)
  under `com.etzhayyim.apps.gyotaku.*`. No handler source exists.
- **Ingest** — Common Crawl CDX/Range retrieval and crawler hand-off, with
  snapshots stored in the kotodama WIT `gyotaku_snapshots` Arrow table.
- **Archive UI** — URL search, domain browse, timeline, snapshot detail
  (WET text / WAT metadata / WebP screenshot). The frontend currently renders a
  scaffold heading.

Note the declarations disagree with each other about transport (`/xrpc` vs
`/api/grpc`) and stack (Go vs WASM component). Reconcile them before building;
do not treat any single file as authoritative. Background: [ADR-0001](docs/adr/0001-repairing-the-frontend-build-and-separating-declared-from-present.md).

## Frontend

```bash
cd appview/etzhayyim-wasm-gyotaku-i3zinrs2/cljs
npm install
npx shadow-cljs compile app      # -> public/js/, served alongside public/index.html
npx shadow-cljs compile test && node out/tests.js   # cljs.test over the re-frame event/sub logic
```

ClojureScript (shadow-cljs) + reagent 1.2.0 + re-frame 1.4.3, rendered with
`jp-go-dds.core` (デジタル庁デザインシステム) hiccup — this workspace's base
design system. `public/index.html`'s inlined CSS was produced once via
`jp-go-dds.page/->page`; see the docstring at the top of
`src/gyotaku/app.cljs` for how to regenerate it. Migrated from the previous
Svelte 5 + Vite 6 frontend (`appview/etzhayyim-wasm-gyotaku-i3zinrs2/svelte`,
removed) — see [ADR-0002](docs/adr/0002-migrate-frontend-from-svelte-to-clojurescript.md).

## Serving

`appview/etzhayyim-wasm-gyotaku-i3zinrs2/kotodama.jsonld` declares an HTTP
trigger on `0.0.0.0:8080` in SPA mode with `staticDir` `/wasm/cljs/public`, and
subscribes to `com.etzhayyim.apps.site.{wet,wat,screenshot,domain}` — i.e. the
archive is meant to read what `site.etzhayyim.com` already crawled rather than
crawl anything itself. The host that consumes this descriptor is outside this
repository.
