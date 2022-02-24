module.exports = (config) => {
  config.addCollection("sitemap", function(collectionApi) {
    return collectionApi.getAll().filter(item => {
      return item.data.ignoreSitemap == undefined;
    });
  });
}
