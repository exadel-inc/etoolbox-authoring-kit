const {setDepthStructure} = require('./setDepthStructure');
const allContent = setDepthStructure('./views/content');

const resolveLink = (md) => {
  md.normalizeLink = (link)=>{
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
  return link;}

}


module.exports.resolveLink = resolveLink;
