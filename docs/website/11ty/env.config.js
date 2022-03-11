const version = process.env.npm_package_version;
const env = process.argv.find(arg => arg.startsWith('--env='))?.split('=')[1];
const isDev = env === 'development';
const date = new Date();

const context = {isDev, version, date};

module.exports = (config) => {
  config.addGlobalData('env', context);
};
Object.assign(module.exports, context);
