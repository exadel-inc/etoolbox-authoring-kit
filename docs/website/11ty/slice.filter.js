/** filter to limit collection */
const limit = (collection, count) => collection ? collection.slice(0, count) : [];

module.exports = (config) => {
  config.addFilter('limit', limit);
};
