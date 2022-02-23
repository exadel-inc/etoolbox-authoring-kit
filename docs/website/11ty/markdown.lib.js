const MarkdownIt = require('markdown-it');
const {highlight} = require('./prismjs.lib');
const {setDepthStructure} = require('../helpers/setDepthStructure')
const allContent = setDepthStructure('./views/content')
const slugify = (s)=>encodeURIComponent(String(s).trim().toLowerCase().replace(/\s+/g, '-').replace('@', ''))

const markdown = MarkdownIt({html: true, highlight, linkify:true});
markdown.use((md)=>{
  md.normalizeLink = (link) => {
    if(link.match(/.+\.md/)){
      allContent.forEach(file=>{
        if(link.startsWith('docs')) {
          const linkSplit = link.split('/');
          const linkNum = linkSplit.length - 1;
          link = linkSplit[linkNum];
        }
        if(link === '../../README.md') link = "/introduction/installation/";
        const fileSplit = file.split('/');
        const fileLastNum = fileSplit.length - 1;
        const fileName = fileSplit[fileLastNum];
        if(fileName === link) return link = file.replace('.md', '');
        if(link.includes(fileName)){
           const linkSplit = link.split('#');
           if(linkSplit[0] === fileName ){
             linkSplit[0] = file.replace('.md', '');
             link = linkSplit.join('#');
           };
        };
      });
      return link;
    };
    return link;
  }
}).use(require('markdown-it-anchor'), {level:2, slugify, tabIndex:false});


module.exports = (config) => {
  config.setLibrary('md', markdown);
};
module.exports.markdown = markdown;
