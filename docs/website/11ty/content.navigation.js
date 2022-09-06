module.exports = (config) => {
  function getNavPath(path) {
    const parts = path.split('/');
    const isIndex = path.endsWith('/index');
    return parts.slice(1, parts.length - +isIndex);
  }

  config.addCollection('content', function (collectionApi) {
    const items = collectionApi.getFilteredByGlob('./views/content/**/*');

    items.forEach((item) => {
      item.navPath = getNavPath(item.filePathStem);
      item.key = item.navPath.join('|');
      item.parent = item.navPath.slice(0, -1).join('|');
      item.mainParent = item.navPath.slice(0, 3).join('|');
    });

    items.forEach((item) => {
      item.parentPage = items.find((itm) => itm.key === item.parent);
      item.mainParentPage = items.find((itm) => itm.key === item.mainParent);
      item.children = items.filter((itm) => itm.parent === item.key);
    });
    
    return items;
  });

  config.addNunjucksFilter('findRoot', function (items) {
    return items.find((item) => !item.parent);
  });

  config.addNunjucksFilter('findByPage', function (collection, page) {
    return collection.find((item) => page.filePathStem === item.filePathStem);
  });
};
