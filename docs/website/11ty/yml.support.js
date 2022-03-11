const yaml = require('js-yaml');

module.exports = (config) => {
  config.addDataExtension('yml', contents => yaml.load(contents));
};
