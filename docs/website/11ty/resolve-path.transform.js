const path = require('path');
const {JSDOM} = require('jsdom');

const ROOT_PATH = path.resolve('../../..');
const DOCS_PATH = path.join(ROOT_PATH, 'docs');

class PathResolver {

  static resolve(content, outputUrl) {
    const filePath = path.dirname(outputUrl.replace(/dist\//, ''));
    const {window} = new JSDOM(content);
    PathResolver.resolveLinks(window.document.body, filePath);
    return window.document.documentElement.innerHTML;
  }

  static resolveLinks(dom, filePath) {
    dom.querySelectorAll('a[href^="."]').forEach((link) => {
      const resolved = PathResolver.resolveLink(link.href, filePath);
      if (resolved !== link.href) console.info(`Rewrite link "${link.href}" to "${resolved}"`);
      link.href = resolved;
    });
  }

  static resolveLink(link, filePath) {
    if (link.startsWith('http:') || link.startsWith('https:')) return link;
    if (link.match(/.+\.md/)) return PathResolver.processRewriteRules(link, filePath);
  }

  static processRewriteRules(link, filePath) {
    const fullFilePath = path.join(DOCS_PATH, filePath);
    const targetPath = path.resolve(fullFilePath, link);
    // console.log('!!!!', link, targetPath);
    return link;
    // for (const [key, value] of Object.entries(rewriteRules)) {
    //   if (!linkPath.endsWith(key)) continue;
    //   if (value.startsWith('/')) return urlPrefix + value;
    //   return value;
    // }
    // return github.srcUrl + linkPath;
  }
}

module.exports = (config) => {
  config.addTransform('resolve.link', PathResolver.resolve);
};
