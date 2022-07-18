const MarkdownIt = require('markdown-it');
const MarkdownItAnchor = require('markdown-it-anchor');
const {highlight} = require('./prismjs.lib');
const mila = require("markdown-it-link-attributes");

const markdown = MarkdownIt({html: true, highlight, linkify: true});

const slugify = (heading) => {
  heading = String(heading).trim().toLowerCase();
  heading = heading.replace(/\s+/g, '-');
  heading = heading.replace(/@/g, '');
  return encodeURIComponent(heading);
};

markdown.use(MarkdownItAnchor, {level: 2, slugify, tabIndex: false});

markdown.use(mila, {
  matcher(href) {
    return href.startsWith("https:");
  },
  attrs: {
    target: '_blank',
    rel: 'noopener noreferrer',
  },
});

module.exports = (config) => {
  config.setLibrary('md', markdown);
};

module.exports.markdown = markdown;
