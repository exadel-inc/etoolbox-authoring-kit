/**
 * Granite Environment Emulation
 */

const $ = require('jquery');

window.$ = $;
window.Granite = {};
window.Granite.$ = window.Granite.JQuery = $;
window.Granite.HTTP = {};
window.Granite.author = {};

// JQuery Extend

// $.adaptTo
$.fn.adaptTo = function (key) {
    if (key === 'foundation-registry') {
        return {
            register: function (key, value) {
                // do nothing
            }
        };
    }
    if (key === 'foundation-field') {
        return this.__mock_foundation_api || null;
    }
    if (key === 'foundation-validation-helper') {
        return {
            getSubmittables: function () {
                // do nothing
            }
        };
    }
    return null;
};
// Mock API setter
$.fn.mockFoundationField = function (api) {
    this.__mock_foundation_api = api;
    return this;
};

// :-foundation-submittable pseudo selector
$.expr.pseudos['-foundation-submittable'] = $.expr.createPseudo(function () {
    return function (el) {
        return $(el).is('input, select, textarea, button');
    };
});

// Test manipulation API
window.Granite._emulateDialogReady = function (dialog) {
    $(window).trigger('foundation-contentloaded', [dialog]);
};
