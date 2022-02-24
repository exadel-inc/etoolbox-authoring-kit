const {url: siteUrl} = require('./site.config');

module.exports = (config) => {
  config.addFilter('canonical', (path) => {
    if (path.startsWith('http(s)?://')) return path;
    return (siteUrl + path).replace(/\/$/, '');
  });
};
