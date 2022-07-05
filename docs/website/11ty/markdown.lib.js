const MarkdownIt = require('markdown-it');
const MarkdownItAnchor = require('markdown-it-anchor');
const {highlight} = require('./prismjs.lib');

const markdown = MarkdownIt({html: true, highlight, linkify: true});

const slugify = (heading) => {
  heading = String(heading).trim().toLowerCase();
  heading = heading.replace(/\s+/g, '-');
  heading = heading.replace(/@/g, '');
  return encodeURIComponent(heading);
};
markdown.use(MarkdownItAnchor, {level: 2, slugify, tabIndex: false});

module.exports = (config) => {
  config.setLibrary('md', markdown);
};
module.exports.markdown = markdown;
