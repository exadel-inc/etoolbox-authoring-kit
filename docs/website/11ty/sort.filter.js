
module.exports = (config) => {
  config.addFilter('sortByOrder', (values) => {
      return values.sort((a,b) => a.data.orderValue - b.data.orderValue)
  });
};
