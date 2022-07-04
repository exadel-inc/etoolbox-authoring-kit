const fns = {
  isActivePath: (url, collection) => {
    return collection && url.includes(collection);
  }
};

module.exports = (config) => {
  config.addGlobalData('functions', fns);
}
