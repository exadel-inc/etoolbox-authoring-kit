export default (config) => {
  config.addCollection('sitemap', (collectionApi) => {
    return collectionApi.getAll().filter(item => item.data.ignoreSitemap === undefined);
  });
}
