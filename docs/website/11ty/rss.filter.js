import {JSDOM} from 'jsdom';
import Parser from 'rss-parser';

const parser = new Parser({defaultRSS: 2.0});
const timeout = (ms) => new Promise(resolve => setTimeout(resolve, ms));

class EAKRssService {
  static META_FETCH_TIMEOUT = 5000;
  static DATE_FORMATTER = new Intl.DateTimeFormat('en-US', {
    day: 'numeric',
    month: 'long',
    year: 'numeric'
  });

  static async load({feed, count, baseUrl, fallback}, callback) {
    const items = await EAKRssService.loadFeed(feed);
    // Load meta for each item
    const result = await Promise.all(items.slice(0, count).map(async (item) => {
      const meta = await Promise.race([EAKRssService.loadMeta(item.link), timeout(EAKRssService.META_FETCH_TIMEOUT)]);
      return Object.assign({}, item, meta || {});
    }));
    // Add fallback items and limit result
    callback(null, result.concat(fallback.items || []).slice(0, count));
  }

  static async loadFeed(feed) {
    try {
      const feedContent = await parser.parseURL(feed);
      return feedContent.items.map(EAKRssService.parseItem);
    } catch {
      console.error('Feed fetching error');
    }
    return [];
  }

  static parseItem(item) {
    const {title, link, pubDate} = item;
    const date = EAKRssService.DATE_FORMATTER.format(new Date(pubDate));
    return {title, link, date};
  }

  static async loadMeta(link) {
    const result = {};
    try {
      // Load page content
      const content = await (await fetch(link)).text();
      // Parse with JSDOM
      const {window} = new JSDOM(content);

      // Resolve image
      const image = window.document.querySelector('meta[property="og:image"]');
      if (image) Object.assign(result, {image: image.content});

      // Resolve description
      const description = window.document.querySelector('meta[property="og:description"]');
      if (description) Object.assign(result, {description: description.content});
    } catch {
      console.error('Meta fetching error');
    }
    return result;
  }
}

export default (config) => {
  config.addNunjucksAsyncFilter('feed', EAKRssService.load);
};
