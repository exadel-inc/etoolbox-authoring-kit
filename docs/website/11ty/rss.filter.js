const {JSDOM} = require('jsdom');
const Parser = require('rss-parser');
const parser = new Parser();

class EAKRssService {
  static DATE_FORMATTER = new Intl.DateTimeFormat('en-US', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });

  static async load({feed, count, baseUrl, fallback}, callback) {
    const items = await EAKRssService.loadFeed(feed);
    const result = items
      .concat(fallback.items || [])
      .slice(0, count)
      .map((item) => EAKRssService.parseItems(item, baseUrl, fallback.image));
    callback(null, result);
  }

  static async loadFeed(feed) {
    try {
      const feedContent = await parser.parseURL(feed);
      return feedContent.items;
    } catch {
      console.error('Feed fetching error');
    }
    return [];
  }

  static parseItems(item, baseUrl, imgFallback) {
    const {title, link, pubDate} = item;
    const tags = item.tags || item.categories;
    const content = item.contentSnippet || item.content;
    const img = item.img || EAKRssService.extractImg(item, baseUrl) || imgFallback;
    const date = EAKRssService.DATE_FORMATTER.format(new Date(pubDate));
    return {title, link, img, content, date, tags};
  }

  static extractImg(item, baseUrl) {
    try {
      const {window} = new JSDOM(item['content:encoded']);
      const image = window.document.querySelector('img');
      const imageSrc = image.src;
      return imageSrc.startsWith('/') ? (baseUrl + imageSrc) : imageSrc;
    } catch {
      return '';
    }
  }
}

module.exports = (config) => {
  config.addNunjucksAsyncFilter('feed', EAKRssService.load);
};
