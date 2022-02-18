module.exports = (config) => {
    config.setFrontMatterParsingOptions({
        excerpt: true,
        delims:["<!--", "-->"]
    });
};
