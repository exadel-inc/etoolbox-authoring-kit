const MarkdownIt = require('markdown-it');
const {highlight} = require('./prismjs.lib');
const {setDepthStructure} = require('../helpers/setDepthStructure')
const allContent = setDepthStructure('./views/content')

const markdown = MarkdownIt({html: true, highlight, linkify:true});
markdown.use((md)=>{
  md.normalizeLink = (link) => {
    if(link && typeof link === 'string' && link.match(/.+\.md/)){
      allContent.forEach(file=>{
        if(link.startsWith("docs")) {
          const linkSplit = link.split('/');
          const linkNum = linkSplit.length - 1
          link = linkSplit[linkNum]
        }
        const fileSplit = file.split('/');
        const fileLastNum = fileSplit.length - 1;
        const fileName = fileSplit[fileLastNum];
        if(fileName === link) {
           link = file.replace('.md', '');
        }
      });
      return link;
    };
    return link;
  }
})


module.exports = (config) => {
  config.setLibrary('md', markdown);
};
module.exports.markdown = markdown;
