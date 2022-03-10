const {isDev} = require('./env.config');

const identical = (values) => values;
const draftsFilter = (values) => {
  return values.filter((item) => {
    const tags = [].concat(item.data.tags);
    return !tags.includes('draft');
  });
};

module.exports = function (config) {
  config.addFilter('released', isDev ? identical : draftsFilter);
  config.addFilter('releasedStrict', draftsFilter);
};
