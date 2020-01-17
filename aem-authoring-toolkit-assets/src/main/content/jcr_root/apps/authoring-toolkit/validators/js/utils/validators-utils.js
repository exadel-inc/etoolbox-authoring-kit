window.AATAssets.Utils = window.AATAssets.Utils ||(function () {
    return {
        format: function (template, params) {
            params.forEach(function (param, index) {
                template = template.replace(new RegExp('\\{' + index + '\\}', 'g'), param);
            });
            return template;
        },
    };
})();