# ADR-0002: Migrate the frontend from Svelte to ClojureScript

## Status

Accepted (2026-08-26).

## Context

ADR-0001 repaired the Svelte 5 + Vite 6 frontend at
`appview/etzhayyim-wasm-gyotaku-i3zinrs2/svelte` so it installed, built, and
type-checked. It remained a Svelte/TypeScript stack, which is not this
workspace's standard for new frontend work: the workspace default for web/local
app UI is ClojureScript compiled by shadow-cljs, reagent + re-frame for
view/state, and `jp-go-dds` (デジタル庁デザインシステム) as the base design
system — not Svelte, not hand-rolled Tailwind.

`App.svelte` was a one-heading, one-paragraph placeholder (*"Vite entry
scaffold after SvelteKit cleanup"*) with no routing, no interactivity, and no
archive UI behind it (see README's "What is declared but not implemented").
There was no product logic to port beyond that static text, only the toolchain
itself.

## Decision

Replace `appview/etzhayyim-wasm-gyotaku-i3zinrs2/svelte` with
`appview/etzhayyim-wasm-gyotaku-i3zinrs2/cljs`:

- **ClojureScript**, compiled by **shadow-cljs** (`:browser` target for the
  app, `:node-test` target for tests).
- **reagent 1.2.0** + **re-frame 1.4.3** for view/state — the heading and
  paragraph text the old scaffold hard-coded into markup now live in re-frame
  `app-db` (`gyotaku.app/default-db`), set by a `:initialize-db`
  `reg-event-db` handler and read by `:page/heading` / `:page/description`
  `reg-sub` subscriptions, so there is real event/sub logic instead of static
  markup.
- **jp-go-dds.core** (`dds/heading`) for the one piece of real markup,
  laid out with the `dds-ext-hero`/`dds-ext-center` classes jp-go-dds already
  ships in `ext-css` — no hand-authored layout CSS.
- **`public/index.html`** is the literal output of `jp-go-dds.page/->page`,
  produced once at authoring time on the JVM (inlining the vendored `dds.css`
  + `jp-go-dds.core/ext-css`, exactly what `jp-go-dds.page/page` composes for
  its own `<style>` block). The regeneration command is recorded in
  `src/gyotaku/app.cljs`'s namespace docstring. The running app itself only
  requires `jp-go-dds.core` at runtime — `jp-go-dds.page` and `html.core` are
  JVM-only tools used to author the shell, not browser-bundle dependencies.
- `reagent`/`re-frame`/`clojurescript`/`shadow-cljs` live under the `:cljs`
  alias in `deps.edn`, not top-level `:deps` — this repo's PreToolUse tooling
  forbids new top-level JVM-runtime Maven deps (workspace cutover), and it
  matches the existing shape in `kotoba-lang/kami-genko` and
  `kotoba-lang/plm`'s `deps.edn`.
- `kotodama.jsonld`'s `triggers.http.staticDir` moved from `/wasm/svelte/dist`
  to `/wasm/cljs/public` (the new build's served directory: `index.html` +
  `js/app.js`, `asset-path` kept relative since these pages are served under a
  path prefix).

We deliberately did not add any UI beyond the original scaffold's heading +
paragraph — there is still no archive runtime behind this frontend, and
inventing interactive features here would misrepresent that gap the same way
ADR-0001 warned against.

## Consequences

- Zero `.svelte` / `.ts` / `.tsx` / `.jsx` files and no Svelte/Vite/vitest
  config remain anywhere under `appview/`.
- `package-lock.json` is not committed for the new frontend (none existed to
  carry forward reproducibly at migration time — see Verification for the
  exact `npm install` output this ADR is based on); a future contributor
  regenerating `node_modules` may resolve slightly different transitive
  versions of `react`/`react-dom` within the `^18.2.0` range this repo pins.
- Tailwind is gone; jp-go-dds's `dds-ext-*` layout classes replace its role
  (see `kotoba-uiux` skill for the base-design-system rationale).
- README and `docs/operator-quickstart.md` were updated to describe the cljs
  build instead of the Svelte one; they were not left describing a directory
  that no longer exists.

## Verification

Executed from `appview/etzhayyim-wasm-gyotaku-i3zinrs2/cljs` on this machine
(Node/npm as reported by the commands below):

- `amu compile --target wasm32-browser app` — see commit message for verbatim output.
- `amu compile --target wasm32-browser test && node out/tests.js` — see commit message for
  verbatim output (4 `cljs.test` assertions over the `:initialize-db` event and
  the `:page/heading`/`:page/description` subs).
