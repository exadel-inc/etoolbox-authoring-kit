import fs from 'fs';
import path, {dirname} from 'path';
import {fileURLToPath} from 'url';
import yaml from 'js-yaml';

const FILE_ROOT = dirname(fileURLToPath(import.meta.url));

const content = fs.readFileSync(path.resolve(FILE_ROOT, '../views/site.yml'), 'utf8');
const siteConfig = yaml.load(content, {});

// Override the base URL if it's set in the environment
if (process.env['SITE_BASE_URL']) siteConfig.url = process.env['SITE_BASE_URL'];

export default (config) => {
  config.addGlobalData('site', siteConfig);
};
export {siteConfig};
