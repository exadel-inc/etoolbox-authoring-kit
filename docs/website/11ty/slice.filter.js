/** filter to limit collection */
const limit = (collection, count) => collection ? collection.slice(0, count) : [];

export default (config) => {
  config.addFilter('limit', limit);
};
