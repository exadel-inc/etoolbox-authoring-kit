const {url: siteUrl} = require('../views/_data/site.json');

module.exports = (config) => {
  config.addFilter('canonical', (path) => {
    if (path.startsWith('http(s)?://')) return path;
    return (siteUrl + path).replace(/\/$/, '');
  });
};
