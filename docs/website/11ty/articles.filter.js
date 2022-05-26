module.exports = (config) => {
  config.addFilter('articleContent', (text) => {
    const contentRegex = /<p.*>.*?<\/p>/ig;
    return text.match(contentRegex).join(' ');
  });
  config.addFilter('articleHeader', (text) => {
    const headerRegex = /<h2.*>.*?<\/h2>/ig;
    return text.match(headerRegex)[0];
  });
  config.addFilter('filterArticles',  (arr, linkToArticle) => {
    return arr.filter((article) => article.data.page.filePathStem === linkToArticle);
  });
};
