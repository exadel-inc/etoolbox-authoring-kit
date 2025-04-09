import config from '@exadel/esl/package.json' with {type: 'json'};

const version = config.version;
const env = process.argv.find(arg => arg.startsWith('--env='))?.split('=')[1];
const isDev = env === 'development';
const date = new Date();

export const context = {isDev, version, env, date};

export default (config) => {
  config.addGlobalData('env', context);
};
