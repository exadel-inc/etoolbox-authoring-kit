/** Filters to extract article (rich text) parts */
export default (config) => {
  config.addFilter('extractArticleContent', (text) => {
    const contentRegex = /<p.*>.*?<\/p>/ig;
    return text && text.match(contentRegex) ? text.match(contentRegex).join(' ') : "";
  });

  config.addFilter('extractArticleHeader', (text) => {
    const headerRegex = /<h2.*>.*?<\/h2>/ig;
    return text && text.match(headerRegex) ? text.match(headerRegex)[0] : "";
  });
};
