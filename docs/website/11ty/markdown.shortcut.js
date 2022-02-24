const path = require('path');
const fsAsync = require('fs').promises;
// const structure = require('../structure.json');

const {JSDOM} = require('jsdom');
const {markdown} = require('./markdown.lib');

const {github, rewriteRules, urlPrefix} = require('../views/_data/site.json');

const recursiveCheckLinks = (arr, link, element, key) => {
    arr.forEach(el=>{
        if(el.hasOwnProperty("fileName") && el.fileName === link){
            element.setAttribute("href", `/${[key]}/${link.replace(".md", "")}`);
        }
        if(!el.hasOwnProperty("fileName") && Object.keys(el).length === 1){
            const elKey = Object.keys(el)[0];
            const newKey =  key + "/" + elKey;
            recursiveCheckLinks(el[elKey], link,element, newKey);
        }
    })
}

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
      const res = await MDRenderer.changeImgPath(window.document.body);
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

  static changeImgPath(content){
      const imgArr = content.querySelectorAll('img');
      const imgPath = '../../assets/components';
      imgArr.forEach(elem => {
          const srcLink = elem.getAttribute('src');
          if(srcLink.startsWith('../img/')) elem.setAttribute('src', srcLink.replace('../img', imgPath ));
          if(srcLink.startsWith('./docs/img/')) elem.setAttribute('src', srcLink.replace('./docs/img', imgPath));
      });
      return content;
  };
}

module.exports = (config) => {
  config.addNunjucksAsyncShortcode('mdRender', MDRenderer.render);

};
module.exports.MDRenderer = MDRenderer;
