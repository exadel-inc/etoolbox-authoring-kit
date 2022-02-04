module.exports = (config) => {
    config.addFilter('toUrlAnchor', (string) => {
        return "#" + string.split(" ").join("-").split("@").join("").split(",").join("").toLowerCase();
    });
}
