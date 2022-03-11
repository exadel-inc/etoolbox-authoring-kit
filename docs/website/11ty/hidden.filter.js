module.exports = (config) => {
  /** Filter items by hidden marker */
  const notHiddenFilter = (collection) => (collection || []).filter( item => !item.data.hidden );

  config.addFilter('notHidden', notHiddenFilter);
};
