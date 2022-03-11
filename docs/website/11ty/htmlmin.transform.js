const {isDev} = require('./env.config');
const htmlmin = require('html-minifier');

const MINIFICATION_CFG = {
  useShortDoctype: true,
  removeComments: true,
  collapseWhitespace: true,
  conservativeCollapse: true,
  keepClosingSlash: true,
  minifyJS: true,
  minifyCSS: true
};

function minifier(content, outputPath) {
  if (!outputPath || !outputPath.endsWith('.html')) return content;
  return htmlmin.minify(content, MINIFICATION_CFG);
}

module.exports = (config) => {
  if (isDev) return;
  config.addTransform('htmlmin', minifier);
};
module.exports.minifier = minifier;
