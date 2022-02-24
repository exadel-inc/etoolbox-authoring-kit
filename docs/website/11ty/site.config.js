const fs = require('fs');
const path =require('path')
const yaml = require('js-yaml');

const content = fs.readFileSync(path.resolve(__dirname, '../views/site.yml'), 'utf8');
const siteConfig = yaml.load(content, {});

module.exports = (config) => {
  config.addGlobalData('site', siteConfig);
};
Object.assign(module.exports, siteConfig);
