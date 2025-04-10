import yaml from 'js-yaml';

export default (config) => {
  config.addDataExtension('yml', (contents) => yaml.load(contents));
};
