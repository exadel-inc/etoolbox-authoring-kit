import MarkdownIt from 'markdown-it';
import MarkdownItAnchor from 'markdown-it-anchor';
import {highlight} from './prismjs.lib.js';

export const markdown = MarkdownIt({html: true, highlight, linkify: true});

export const slugify = (heading) => {
  heading = String(heading).trim().toLowerCase();
  heading = heading.replace(/\s+/g, '-');
  heading = heading.replace(/@/g, '');
  return encodeURIComponent(heading);
};
markdown.use(MarkdownItAnchor, {level: 2, slugify, tabIndex: false});

export default (config) => {
  config.setLibrary('md', markdown);
};
