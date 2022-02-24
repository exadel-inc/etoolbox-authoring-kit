const MarkdownIt = require('markdown-it');
const {highlight} = require('./prismjs.lib');
const {resolveLink} = require('../helpers/resolveLink');
const {setCorrectImageSrc} = require('../helpers/setCorrectImageSrc')
const replaceSymbolAt = (str) =>{
  return str.startsWith('@') ? str.replace('@','') : str;
}
const slugify = (s)=>encodeURIComponent(replaceSymbolAt(String(s).trim().toLowerCase().replace(/\s+/g, '-')));

const markdown = MarkdownIt({html: true, highlight, linkify:true});
markdown.use(md=>resolveLink(md))
.use(require('markdown-it-anchor'), {level:2, slugify, tabIndex:false})
.use((md) =>setCorrectImageSrc(md));


module.exports = (config) => {
  config.setLibrary('md', markdown);
};
module.exports.markdown = markdown;
