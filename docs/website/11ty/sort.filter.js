module.exports = (config) => {
  config.addFilter('sortByOrder', (values) => {
      return values.sort((a,b) => a.data.orderVal - b.data.orderVal)
  });
};
