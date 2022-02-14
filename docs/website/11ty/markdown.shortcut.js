const path = require('path');
const fsAsync = require('fs').promises;
const structure = require('../structure.json');

const {JSDOM} = require('jsdom');
const {markdown} = require('./markdown.lib');

const {github, rewriteRules, urlPrefix} = require('../views/_data/site.json');

class MDRenderer {

  static async render(filePath, startAnchor, endAnchor) {
    try {
      const content = await MDRenderer.parseFile(filePath);
      const {window} = new JSDOM(content);


      // Exclude part before start anchor
      if (startAnchor) {
        const startAnchorElement = MDRenderer.findAnchor(window.document, startAnchor);
        while (startAnchorElement.previousSibling) startAnchorElement.previousSibling.remove();
        startAnchorElement.remove();
      }

      // Exclude part after end anchor
      if (endAnchor) {
        const endAnchorElement = MDRenderer.findAnchor(window.document, endAnchor);
        while (endAnchorElement.nextSibling) endAnchorElement.nextSibling.remove();
        endAnchorElement.remove();
      }

      // Resolve content links
      MDRenderer.resolveLinks(window.document.body, filePath);

     MDRenderer.changePath(window.document.body);
      const res = await MDRenderer.changeImgPath(window.document.body);
      MDRenderer.setHeadingsId(res);
      // Render result content
      return MDRenderer.renderContent(res);
    } catch (e) {
      return `Rendering error: ${e}`;
    }
  }

  /** Read file and render markdown */
  static async parseFile(filePath) {
    const absolutePath = path.resolve(__dirname, '../', filePath);
    const data = await fsAsync.readFile(absolutePath);
    const content = data.toString();
    return markdown.render(content);
  }

  static findAnchor(dom, name) {
    const anchor = dom.querySelector(`a[name='${name}']`);
    return anchor && anchor.matches(':only-child') ? anchor.parentElement : anchor;
  }

  static renderContent(content) {
    return `<div class="markdown-container">${content.innerHTML}</div>`;
  }

  static resolveLinks(dom, basePath) {
    dom.querySelectorAll('a[href^="."]').forEach((link) => {
      const absolutePath = path.join(path.dirname(basePath), link.href).replace(/\\/g, '/');
      const resultPath = MDRenderer.processRewriteRules(absolutePath);
      console.info(`Rewrite link "${link.href}" to "${resultPath}"`);
      link.href = resultPath;
    });
  }
  static processRewriteRules(linkPath) {
    for (const [key, value] of Object.entries(rewriteRules)) {
      if (!linkPath.endsWith(key)) continue;
      if (value.startsWith('/')) return urlPrefix + value;
      return value;
    }
    return github.srcUrl + linkPath;
  }
  static changePath(content){
    const reForMdLinks = /^[^https].+\.md/;
    const reForInstallationLinks = /docs\/md\/.+\.md/
    const reForGHLinks = /https:\/\/github\.com\/exadel-inc\/etoolbox-authoring-kit\/tree\/.+/;
    const linkArr = content.querySelectorAll('a');
    linkArr.forEach(elem =>{
      const elemLink = elem.getAttribute('href');
        if(elemLink.match(reForGHLinks)){
          const link = elemLink.replace("/tree/", "/blob/master/");
          elem.setAttribute("href", link);
        }
        else if (elemLink.match(/.*samples.*/) && !elemLink.match(reForGHLinks)){
        elem.setAttribute("href", '/introduction/samples/')
        }
        else if(elemLink.match(reForInstallationLinks)){
          let link = elemLink.replace('docs/md/', "")
          for(let key in structure){
            structure[key].includes(link) && elem.setAttribute("href", `/${[key]}/${link.replace(".md", "")}`)
          }
        }
        else if(elemLink.match(reForMdLinks)){
        for(let key in structure){
            structure[key].includes(elemLink) && elem.setAttribute("href", `/${[key]}/${elemLink.replace(".md", "")}`)
        }
      }
    });
    return content;
  };
  static changeImgPath(content){
      const imgArr = content.querySelectorAll('img');
      const imgPath = '../../assets/components';
      imgArr.forEach(elem => {
          const srcLink = elem.getAttribute('src');
          if(srcLink.startsWith('../img/')) elem.setAttribute('src', srcLink.replace('../img', imgPath ))
          if(srcLink.startsWith('./docs/img/')) elem.setAttribute('src', srcLink.replace('./docs/img', imgPath))
      });
      return content;
  };
  static setHeadingsId(content){
      const headingsArr = content.querySelectorAll("h1,h2,h3,h4,h5,h6");
      headingsArr.forEach( elem => {
          const id = elem.textContent.split(" ").join("-").split("@").join("").split(",").join("").toLowerCase();
          elem.setAttribute('id', id);
      })
  }
}

module.exports = (config) => {
  config.addNunjucksAsyncShortcode('mdRender', MDRenderer.render);

};
module.exports.MDRenderer = MDRenderer;
