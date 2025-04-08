import {context} from './env.config.js';
import {minify} from 'html-minifier-terser';

const MINIFICATION_CFG = {
  collapseWhitespace: true,
  conservativeCollapse: true,
  keepClosingSlash: true,
  minifyJS: true,
  minifyCSS: true,
  removeComments: true,
  useShortDoctype: true,
  ignoreCustomFragments: [
    /<%[\s\S]*?%>/,
    /<\?[\s\S]*?\?>/
  ]
};

async function minifier(content, outputPath) {
  if (!outputPath || !outputPath.endsWith('.html')) return content;
  return await minify(content, MINIFICATION_CFG);
}

export default (config) => {
  if (context.isDev) return;
  config.addTransform('htmlmin', minifier);
};
