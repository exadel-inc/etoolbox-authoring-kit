const env = process.argv.find(arg => arg.startsWith('--env='))?.split('=')[1];
const version = 4
const isDev = env === 'development';

const context = {isDev, version, env};

module.exports = (config) => {
//   Waiting for 11ty 1.0.0
//   config.addGlobalData('env', context);
};
Object.assign(module.exports, context);
