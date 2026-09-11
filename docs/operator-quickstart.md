# Operator Quickstart — app-gyotaku

Shortest path from clone to a **verified local build** of the one thing in this
repository that runs today: the `gyotaku` appview frontend.

Every command below was executed against this tree before it was written down.
Recorded output is real. Where a step does *not* work, this document says so
instead of omitting it.

> **Read [`README.md`](../README.md) first if you expect a web archive.** The
> archive runtime (MCP component, Common Crawl ingest, snapshot storage) is
> **not in this repository** — only its descriptors are. This quickstart builds
> the frontend shell, not an archive.

## Prerequisites

- Node.js 20+ and npm 10+
- Clojure CLI (`clojure`) — only needed if you want to regenerate
  `public/index.html`, not for the ordinary build/test cycle below
- Git

No credentials and no `.env` are required. The build needs network access once
(to resolve `deps.edn`/npm dependencies, including the `jp-go-dds` git
dependency and the `react`/`react-dom` npm packages reagent needs); after that
it is fully local.

## 1. Clone

```bash
git clone git@github.com:cloud-itonami/app-gyotaku.git
cd app-gyotaku
```

## 2. Enter the frontend

The frontend is not at the repository root:

```bash
cd appview/etzhayyim-wasm-gyotaku-i3zinrs2/cljs
```

## 3. Install

```bash
npm install
```

## 4. Build

```bash
amu compile --target wasm32-browser app
```

Expected — a compiled bundle in `public/js/`, served alongside the committed
`public/index.html`. See the migration commit message for the verbatim
recorded output of this exact command.

## 5. Test

```bash
amu compile --target wasm32-browser test && node out/tests.js
```

Expected: 4 `cljs.test` assertions pass, covering the `:initialize-db`
`reg-event-db` handler and the `:page/heading` / `:page/description`
`reg-sub` subscriptions in `src/gyotaku/app.cljs`. See the migration commit
message for the verbatim recorded output.

## 6. Serve the built bundle

`public/index.html` + `public/js/` is a static bundle; serve `public/` with any
static file server (there is no `npm run preview` here — shadow-cljs's own dev
server is for the `watch` workflow, not for smoke-testing a release build) and
confirm `/` returns the page with `<div id="app">` populated by the mounted
reagent view — the heading `etzhayyim-wasm-gyotaku-i3zinrs2` — **not** an
archive UI (see §8).

## 7. Validate the descriptors

The repository is mostly declarative. These parse checks are the only
verification the descriptors currently have:

```bash
cd "$(git rev-parse --show-toplevel)"
for f in PROJECT.jsonld \
         appview/gyotaku-mcp-component/kotodama.jsonld \
         appview/etzhayyim-wasm-gyotaku-i3zinrs2/kotodama.jsonld; do
  node -e "JSON.parse(require('fs').readFileSync('$f','utf8'));console.log('OK $f')"
done
```

All three parse. `README.edn` and `migration.edn` likewise read as EDN.

## 8. What you cannot do from this repository

Verified absent from the tracked tree:

| Declared by | Declares | In tree |
|---|---|---|
| both `kotodama.jsonld` | `component.wasm` | **absent** — 0 `.wasm` files tracked |
| `PROJECT.jsonld` | `"stack": "go"`, `/api/grpc` | **absent** — 0 `.go` files tracked |
| `CLAUDE.md` | 5 XRPC commands, `com.etzhayyim.apps.gyotaku.*` | no handler source |
| `README.md` (pre-2026-08-21) | Common Crawl ingest, snapshot storage | no ingest source |

So there is no server to start, no snapshot to fetch, and no endpoint to call.
`src/gyotaku/app.cljs`'s `app` view is a placeholder — it renders the same
heading + one paragraph the old `App.svelte` scaffold did (now sourced from
re-frame `app-db` instead of hard-coded markup), nothing more.

## 9. Background

The frontend was Svelte 5 + Vite 6 + TypeScript through ADR-0001 (which repaired
its build). ADR-0002 replaced it with ClojureScript + shadow-cljs + reagent +
re-frame + jp-go-dds, this workspace's standard frontend stack — see
[ADR-0002](adr/0002-migrate-frontend-from-svelte-to-clojurescript.md) for why
and what changed.
