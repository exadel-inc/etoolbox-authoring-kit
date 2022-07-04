/** Filters to extract article (rich text) parts */
module.exports = (config) => {
  config.addFilter('extractArticleContent', (text) => {
    const contentRegex = /<p.*>.*?<\/p>/ig;
    return text.match(contentRegex).join(' ');
  });

  config.addFilter('extractArticleHeader', (text) => {
    const headerRegex = /<h2.*>.*?<\/h2>/ig;
    return text.match(headerRegex)[0];
  });
};
