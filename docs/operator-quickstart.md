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

- Node.js 20+ and npm 10+ — verified on **Node v26.3.0 / npm 11.16.0**
- Git

No credentials, no network services, and no `.env` are required. The build is
fully local after `npm install`.

## 1. Clone

```bash
git clone git@github.com:cloud-itonami/app-gyotaku.git
cd app-gyotaku
```

## 2. Enter the frontend

The frontend is not at the repository root:

```bash
cd appview/etzhayyim-wasm-gyotaku-i3zinrs2/svelte
```

## 3. Install

```bash
npm install
```

Expected: `added 119 packages` (from a fresh clone, resolved against the
committed `package-lock.json`).

## 4. Build

```bash
npm run build
```

Expected — a production bundle in `dist/`:

```
vite v6.4.3 building for production...
✓ 110 modules transformed.
dist/index.html                  0.42 kB │ gzip:  0.28 kB
dist/assets/index-C-zwCK5o.css   0.24 kB │ gzip:  0.21 kB
dist/assets/index-z-6MKmrn.js   28.38 kB │ gzip: 10.92 kB
✓ built in 755ms
```

`dist/` is what `kotodama.jsonld` declares as `triggers.http.staticDir`
(`/wasm/svelte/dist`).

## 5. Type-check

```bash
npm run check
```

Expected: `COMPLETED 112 FILES 0 ERRORS 0 WARNINGS`, exit code 0.

## 6. Serve the built bundle

```bash
npm run preview -- --port 4319
curl -s -o /dev/null -w '%{http_code}\n' http://localhost:4319/
```

Expected: `200`, serving `dist/index.html` with the hashed JS/CSS assets
injected. The page renders the scaffold heading
`etzhayyim-wasm-gyotaku-i3zinrs2` — **not** an archive UI (see §8).

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

Verified absent from the tracked tree (18 files total):

| Declared by | Declares | In tree |
|---|---|---|
| both `kotodama.jsonld` | `component.wasm` | **absent** — 0 `.wasm` files tracked |
| `PROJECT.jsonld` | `"stack": "go"`, `/api/grpc` | **absent** — 0 `.go` files tracked |
| `CLAUDE.md` | 5 XRPC commands, `com.etzhayyim.apps.gyotaku.*` | no handler source |
| `README.md` (pre-2026-08-21) | Common Crawl ingest, snapshot storage | no ingest source |

So there is no server to start, no snapshot to fetch, and no endpoint to call.
`src/App.svelte` is a placeholder that says so in its own body text
(*"Vite entry scaffold after SvelteKit cleanup"*).

Tailwind is configured (`tailwind.config.js`, `postcss.config.js`) but **no CSS
entrypoint is imported**, so no utility classes reach the bundle — the built CSS
contains only App.svelte's scoped rules. Wire a CSS entry before relying on
Tailwind.

## 9. If `npm install` fails

It did, before 2026-08-21. Two dependency defects were fixed together
(ADR-0001); if you are on an older commit you will see:

```
npm error peer vite@"^5.0.0" from @sveltejs/vite-plugin-svelte@4.0.4
```

Fix: `@sveltejs/vite-plugin-svelte` must be `^5` to accept the pinned Vite 6,
and the unreferenced `@sveltejs/adapter-static` (a SvelteKit leftover that pulls
`@sveltejs/kit`) must be removed.
