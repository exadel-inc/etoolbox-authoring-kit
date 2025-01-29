const fs = require('fs');
const path =require('path')
const yaml = require('js-yaml');

const content = fs.readFileSync(path.resolve(__dirname, '../views/site.yml'), 'utf8');
const siteConfig = yaml.load(content, {});

// Override the base URL if it's set in the environment
if (process.env['SITE_BASE_URL']) siteConfig.url = process.env['SITE_BASE_URL'];

module.exports = (config) => {
  config.addGlobalData('site', siteConfig);
};
Object.assign(module.exports, siteConfig);
