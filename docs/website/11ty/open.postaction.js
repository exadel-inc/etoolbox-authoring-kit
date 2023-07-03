const {isDev} = require('./env.config');

/**
 * Auto-open development server
 * Should be replaced with OOTB solution when https://github.com/11ty/eleventy-dev-server/issues/28 will be resolved
 */
module.exports = (config) => {
  if (!isDev) return;
  config.on('eleventy.after', async () => {
    const {port} = config.serverOptions;
    if (!port || global.hasOpened) return;
    await require('out-url').open(`http://localhost:${port}`);
    global.hasOpened = true;
  });
};
