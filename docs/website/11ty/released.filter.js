import {context} from './env.config.js';

const identical = (values) => values;
const draftsFilter = (values) => {
  return values.filter((item) => {
    const tags = [].concat(item.data.tags);
    return !tags.includes('draft');
  });
};

export default (config) => {
  config.addFilter('released', context.isDev ? identical : draftsFilter);
  config.addFilter('releasedStrict', draftsFilter);
};
