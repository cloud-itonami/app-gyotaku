import { vitePreprocess } from '@sveltejs/vite-plugin-svelte'

// svelte-check resolves the Svelte configuration through this file; without it
// `npm run check` exits 1 with "No Svelte configuration found in vite config",
// even though vite.config.ts does register the plugin.
export default {
  preprocess: vitePreprocess(),
}
