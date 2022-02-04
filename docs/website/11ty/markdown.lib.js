const MarkdownIt = require('markdown-it');
const {highlight} = require('./prismjs.lib');

const markdown = MarkdownIt({html: true, highlight});

module.exports = (config) => {
  config.setLibrary('md', markdown);
};
module.exports.markdown = markdown;
