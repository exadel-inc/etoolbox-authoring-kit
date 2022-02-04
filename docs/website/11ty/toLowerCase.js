module.exports = (config) => {
    config.addFilter('toLowerCase', (string) => {
        return string.toLowerCase();
    });
}
