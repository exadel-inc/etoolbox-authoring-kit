/** Check if the collection contains passed url */
function includesUrl(collection, url) {
  return collection && url.includes(collection);
}

/** Find item in collection by url */
function equalByUrl(collection, url) {
  return collection.filter((item) => item.data.page.filePathStem === url);
}

export default (config) => {
  config.addFilter('equalByUrl', equalByUrl);
  config.addFilter('includesUrl', includesUrl);
};
