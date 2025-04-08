import path, {dirname} from 'path';
import {fileURLToPath} from 'url';
import {JSDOM} from 'jsdom';
import {readFile} from 'fs/promises';
import {markdown} from './markdown.lib.js';
import {siteConfig} from './site.config.js';

const __dirname = dirname(fileURLToPath(import.meta.url));

const recursiveCheckLinks = (arr, link, element, key) => {
  arr.forEach((el) => {
    if (el.hasOwnProperty('fileName') && el.fileName === link) {
      element.setAttribute('href', `/${[key]}/${link.replace('.md', '')}`);
    }
    if (!el.hasOwnProperty('fileName') && Object.keys(el).length === 1) {
      const elKey = Object.keys(el)[0];
      const newKey =  key + '/' + elKey;
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
      // TODO: replace with resolve path utility
      MDRenderer.resolveLinks(window.document.body, filePath);
      MDRenderer.changeImgPath(window.document.body);

      // Render result content
      return MDRenderer.renderContent(window.document.body);
    } catch (e) {
      return `Rendering error: ${e}`;
    }
  }

  /** Read file and render markdown */
  static async parseFile(filePath) {
    const absolutePath = path.resolve(__dirname, '../', filePath);
    const data = await readFile(absolutePath);
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
    for (const [key, value] of Object.entries(siteConfig.rewriteRules)) {
      if (!linkPath.endsWith(key)) continue;
      if (value.startsWith('/')) return siteConfig.urlPrefix.replace(/^\//, '') + value;
      return value;
    }
    return siteConfig.github.srcUrl + linkPath;
  }

  static changeImgPath(dom) {
    const imgArr = dom.querySelectorAll('img');
    const imgPath = '/assets/img';
    imgArr.forEach(elem => {
      const srcLink = elem.getAttribute('src');
      if(srcLink.startsWith('../img/')) elem.setAttribute('src', srcLink.replace('../img', imgPath ));
      if(srcLink.startsWith('./docs/img/')) elem.setAttribute('src', srcLink.replace('./docs/img', imgPath));
    });
  }
}

export default (config) => {
  config.addNunjucksAsyncShortcode('mdRender', MDRenderer.render);
};
