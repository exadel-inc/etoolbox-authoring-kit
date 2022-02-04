

module.exports = (config) => {

    const consoleInfo = (elem) => {
        console.info(elem)
    };
    config.addFilter('consoleInfo', consoleInfo);
};
