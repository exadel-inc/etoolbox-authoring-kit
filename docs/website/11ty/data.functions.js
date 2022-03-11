const fns = {
  findItemsByName:(names, collection) => {
      return names
        .map((name) => collection.find((item) => item.fileSlug === name))
        .filter((item) => !!item);
    },
  isActivePath:(url, collection) => {
      return collection && url.includes(collection);
    }
};

module.exports = (config) => {
  config.addGlobalData('functions', fns);
}
