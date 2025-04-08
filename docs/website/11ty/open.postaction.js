import outUrl from 'out-url';
import {context} from './env.config.js';

/**
 * Auto-open development server
 * Should be replaced with OOTB solution when https://github.com/11ty/eleventy-dev-server/issues/28 will be resolved
 */
export default (config) => {
  if (!context.isDev) return;
  config.on('eleventy.after', async () => {
    const {port} = config.serverOptions;
    if (!port || global.hasOpened) return;
    await outUrl.open(`http://localhost:${port}`);
    global.hasOpened = true;
  });
};
