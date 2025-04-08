import {siteConfig} from './site.config.js';

export default (config) => {
  config.addFilter('canonical', (path) => {
    if (path.startsWith('http(s)?://')) return path;
    return (siteConfig.url + path).replace(/\/$/, '');
  });
};
