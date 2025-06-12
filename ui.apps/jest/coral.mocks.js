// Coral Mocks for Jest Testing
window.Coral = {};

// Emulate Coral Commons ready
Coral.commons = {
    ready: function (container, callback) {
        if (typeof container === 'function') {
            callback = container;
            container = document;
        }
        // Emulate Coral Commons ready
        setTimeout(() => {
            if (typeof callback === 'function') {
                callback();
            }
        }, 0);
    }
};
