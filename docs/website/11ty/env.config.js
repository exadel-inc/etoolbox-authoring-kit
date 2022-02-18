const package = require('../package.json')

const version = package.version
const isDev = package.isDev
const date = new Date()
const context = {isDev, version, date};


module.exports = (config) => {
   config.addGlobalData('env', context);
};

