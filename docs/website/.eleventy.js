const fs = require('fs');
const {isDev} = require('./11ty/env.config');

module.exports = (config) => {
  // Init all 11ty config modules
  const cfgFiles = fs.readdirSync('./11ty');
  for (const file of cfgFiles) {
    // TODO: ignore dir
    if (file.startsWith('_')) continue;
    try {
      console.info(`Initializing module: ${file}`);
      require('./11ty/' + file)(config);
      console.info(`Module ${file} initialized.`);
    } catch (e) {
      console.error(`Module ${file} initialization failed`);
      throw e;
    }
  }

  // Watch for changes in the content folder ignored by GIT
  config.setUseGitIgnore(false);
  config.addWatchTarget('views/content/');

  // Setup simple copy operations
  config.addPassthroughCopy({
    '../img': 'assets/img',
    'static/assets': 'assets',
    'static/tools': '.'
  });

  // Update BS observed directories
  config.setBrowserSyncConfig({
    files: [
      'dist/bundles/*.js',
      'dist/bundles/*.css',
      'dist/bundles/*.map'
    ],
    open: isDev
  });

  return {
    dir: {
      input: 'views',
      output: 'dist',
      layouts: '_layouts'
    },
    dataTemplateEngine: 'njk',
    htmlTemplateEngine: 'njk',
    passthroughFileCopy: true,
    templateFormats: ['md', 'njk', 'html'],
    pathPrefix: '/'
  };
};
