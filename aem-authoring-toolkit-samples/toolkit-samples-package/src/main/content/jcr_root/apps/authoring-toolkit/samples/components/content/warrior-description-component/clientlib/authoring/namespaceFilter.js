(function (Granite, $, DependsOn) {

    'use strict';

    const PORT = 'http://localhost:4502';
    const COMPONENT_FORMAT = '.json';
    const TAG_PATH_REGEX = /^(?:.*:)(.*?)(?:\/(.*))?$/;
    const TAG_LABEL_SELECTOR = '.coral-TagList-tag-label';
    const SELECT_LIST_SELECTOR = '.coral-SelectList';
    const TAG_LIST_SELECTOR = '.coral-TagList';

    function removeTagPath(tag) {
        return tag.replace(TAG_PATH_REGEX, '$2').trim();
    }

    function changeActiveTagsTitle(activeTags) {
        let tagLabel = null;
        activeTags.map(tag => {
            tagLabel = tag.querySelector(TAG_LABEL_SELECTOR);
            tagLabel.innerHTML = removeTagPath(tagLabel.innerHTML);
        });
    }

    function getColor(tag) {
        return tag.replace(TAG_PATH_REGEX, '$1');
    }

    function success(data) {
        if (!data) { return false; }
        return (data.colorTheme === 'true');
    }

    function sortTags(tagList, colorTheme) {
        let tagTitle = null;
        tagList.map(tagElement => {
            tagTitle = getColor(tagElement.dataset.value) === colorTheme
                ? removeTagPath(tagElement.dataset.value)
                : null;
            tagElement.hidden = !tagTitle;
            tagElement.innerHTML = tagTitle || '';
        });
    }

    DependsOn.ActionRegistry.register('namespaceFilter', function namespaceFilter(parentPath) {
        const element = this.$el.context;
        let promise = $.get(Granite.HTTP.externalize(PORT + parentPath + COMPONENT_FORMAT));
        promise
            .then(success, () => false)
            .then(
            function handler(isDark) {

                const colorTheme = isDark ? 'dark' : 'light';
                const tagList =  Array.from(element.querySelector(SELECT_LIST_SELECTOR).children);
                const activeTagsElement = element.querySelector(TAG_LIST_SELECTOR);
                const activeTagsList = Array.from(activeTagsElement && activeTagsElement.children);
                const oldColorTheme = activeTagsList.length > 0
                ? getColor(activeTagsList[0].querySelector('input').value)
                : null;

                changeActiveTagsTitle(activeTagsList);
                sortTags(tagList, colorTheme);

                if (oldColorTheme && (oldColorTheme !== colorTheme)) {
                    activeTagsElement.innerHTML = '';
                }

            }.bind(this)
        );
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);