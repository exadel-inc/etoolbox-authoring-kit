module.exports = (config) => {
  config.addCollection('content', function (collectionApi) {
    return collectionApi.getFilteredByGlob('./views/content/**/*');
  });

  config.addNunjucksFilter('navigation', function (items) {
    let root;
    items.forEach((item) => {
      const path = item.filePathStem;
      const parts = path.split('/');
      const isIndex = path.endsWith('/index');
      item.navPath = parts.slice(1, parts.length - +isIndex);
      item.key = item.navPath.join('|');
      item.parent = item.navPath.slice(0, -1).join('|');
      if (!item.parent) root = item;
    });
    items.forEach((item) => {
      item.children = items.filter((itm) => itm.parent === item.key);
    });
    return root;
  });
};
