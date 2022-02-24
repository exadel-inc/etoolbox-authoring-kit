const path = require('path');
const {blue} = require('kleur');
const {JSDOM} = require('jsdom');

const {rewriteRules} = require('./site.config');

const ROOT_PATH = path.resolve('../../..');
const DOCS_PATH = path.join(ROOT_PATH, 'docs');

class PathResolver {
  static rules = rewriteRules.map(PathResolver.createRule);
  static createRule({regexp, value}) {
    if (typeof regexp === 'string') regexp = new RegExp(regexp, 'i');
    return {regexp, replacement: value};
  }

  static resolve(content, outputUrl) {
    const filePath = path.dirname(outputUrl.replace(/dist\//, ''));
    const {window} = new JSDOM(content);
    PathResolver.resolveLinks(window.document.body, filePath);
    PathResolver.resolveImages(window.document.body, filePath);
    return window.document.documentElement.innerHTML;
  }

  static resolveLinks(dom, filePath) {
    dom.querySelectorAll('a[href^="."]').forEach((link) => {
      const resolved = PathResolver.resolveLink(link.href, filePath);
      if (resolved !== link.href) console.info(blue(`Rewrite link "${link.href}" to "${resolved}"`));
      link.href = resolved;
    });
  }
  static resolveImages(dom, filePath) {
    dom.querySelectorAll('img[src^="."]').forEach((img) => {
      const resolved = PathResolver.resolveLink(img.src, filePath);
      if (resolved !== img.src) console.info(blue(`Rewrite image source "${img.src}" to "${resolved}"`));
      img.src = resolved;
    });
  }

  static resolveLink(link, filePath) {
    if (link.startsWith('http:') || link.startsWith('https:')) return link;
    // link.match(/.+\.md/)
    if (link.startsWith('.')) return PathResolver.processRewriteRules(link, filePath);
  }

  static processRewriteRules(link, filePath) {
    const [linkUrl, linkAnchor] = link.split('#');
    const fullFilePath = path.join(DOCS_PATH, filePath);
    const fullDirPath = path.dirname(fullFilePath);
    const targetPath = path.resolve(fullDirPath, linkUrl);
    const target = path.relative(ROOT_PATH, targetPath).replace(/\\/g, '/');

    for (const {path, regexp, replacement} of this.rules) {
      if (path === target) return replacement;
      if (!regexp || !regexp.test(target)) continue;
      const url = target.replace(regexp, replacement);
      return url + (linkAnchor ? `#${linkAnchor}` : '');
    }
    return link;
  }
}

module.exports = (config) => {
  config.addTransform('resolve.link', PathResolver.resolve);
};
