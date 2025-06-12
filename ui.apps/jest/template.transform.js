const fs = require('fs');

/**
 * A custom transformer for Jest to support the `require` directive for a clientlib descriptor file.
 * Usage: `require('path/to/clientlib/js.txt')`
 * Note: it does not resolve the clientlib dependencies, it just emulates concatenation of the files.
 */
module.exports = {
    getCacheKey(src, filename) {
        // get file modification time as cache key
        return fs.statSync(filename).mtimeMs.toString();
    },
    process(src, filename) {
        return {
            code: `module.exports = ${JSON.stringify(src)};`,
        };
    }
};
