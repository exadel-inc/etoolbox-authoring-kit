import path, {dirname} from 'path';
import color from 'kleur';
import {JSDOM} from 'jsdom';
import {Minimatch} from 'minimatch';
import {siteConfig} from './site.config.js';



const ROOT_PATH = path.resolve('../../..');
const DOCS_PATH = path.join(ROOT_PATH, 'docs');

class PathResolver {
  static rules = siteConfig.rewriteRules.map(PathResolver.createRule);

  static createRule({glob, regexp, value}) {
    if (typeof regexp === 'string') regexp = new RegExp(regexp, 'i');
    if (typeof glob === 'string') regexp = (new Minimatch(glob)).makeRe();
    return {regexp, replacement: value};
  }

  static resolve(content, outputUrl) {
    if (!outputUrl.endsWith('.html')) return content;
    const filePath = path.dirname(outputUrl.replace(/dist\//, ''));
    const {window} = new JSDOM(content);

    PathResolver.resolveLinks(window.document.body, filePath);
    PathResolver.resolveImages(window.document, filePath);
    return window.document.documentElement.innerHTML;
  }

  static isExternal(urlString) {
    const domain = new URL(siteConfig.url).hostname;

    try {
      const link = new URL(urlString);
      const isHttp = link.protocol === 'http:' || link.protocol === 'https:';

      return isHttp && domain !== link.hostname;
    } catch (e) {
      return false;
    }
  }

  static resolveLinks(dom, filePath) {
    dom.querySelectorAll('a[href]').forEach((link) => {
      const resolved = PathResolver.resolveLink(link.href, filePath);
      if (resolved !== link.href) console.info(color.blue(`Rewrite link "${link.href}" to "${resolved}"`));
      link.href = resolved;

      if (PathResolver.isExternal(link.href)) {
        link.target = '_blank';
        link.rel = 'noopener norefferer';
      }
    });
  }

  static resolveImages(dom, filePath) {
    dom.body.querySelectorAll('img[src^="."]').forEach((img) => {
      const resolved = PathResolver.resolveLink(img.src, filePath);
      if (resolved !== img.src) console.info(color.blue(`Rewrite image source "${img.src}" to "${resolved}"`));

      const zoomImg = dom.createElement('eak-zoom-image');
      zoomImg.setAttribute('data-src', resolved);

      img.parentNode.replaceChild(zoomImg, img);
    });
  }

  static resolveLink(link, filePath) {
    if (link.startsWith('http:') || link.startsWith('https:')) return link;
    // link.match(/.+\.md/)
    if (link.startsWith('.')) return PathResolver.processRewriteRules(link, filePath);

    return link;
  }

  static processRewriteRules(link, filePath) {
    const [linkUrl, linkAnchor] = link.split('#');
    const fullFilePath = path.join(DOCS_PATH, filePath);
    const fullDirPath = path.dirname(fullFilePath);
    const targetPath = path.resolve(fullDirPath, linkUrl);
    const target = path.relative(ROOT_PATH, targetPath).replace(/\\/g, '/');

    for (const {regexp, replacement} of this.rules) {
      if (!regexp || !regexp.test(target)) continue;
      const url = target.replace(regexp, replacement);
      return url + (linkAnchor ? `#${linkAnchor}` : '');
    }
    return link;
  }
}

export default (config) => {
  config.addTransform('resolve.link', PathResolver.resolve);
};
