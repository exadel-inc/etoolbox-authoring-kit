/**
 * Granite Environment Emulation
 */

const $ = require('jquery');

window.$ = $;
window.Granite = {};
window.Granite.$ = window.Granite.JQuery = $;
window.Granite.HTTP = {};
window.Granite.author = {};

// JQuery Extend functions

// $.adaptTo
$.fn.adaptTo = function (key) {
    if (key === 'foundation-registry') {
        return {
            register: function (key, value) {
                // do nothing
            }
        };
    }
    return null;
};

// Test manipulation API
window.Granite._emulateDialogReady = function (dialog) {
    $(window).trigger('foundation-contentloaded', [dialog]);
};
