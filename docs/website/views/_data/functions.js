module.exports = {
  findItemsByName(names, collection) {
    return names
      .map((name) => collection.find((item) => item.fileSlug === name))
      .filter((item) => !!item);
  }
};
