/**
 * Custom dependsOn action for @Autocomplete.
 * The action is intended for dynamic changing of tags scope depending on the Warrior component
 * (that is the main container for other components in the samples module) color theme
 */

(function (Granite, $, DependsOn) {

    'use strict';

    const TAG_PATH_REGEX = /^(?:.*:)(.*?)(?:\/(.*))?$/;
    const TAG_LABEL_SELECTOR = '.coral-TagList-tag-label';
    const SELECT_LIST_SELECTOR = '.coral-SelectList';
    const TAG_LIST_SELECTOR = '.coral-TagList';

    /**
     * @param tag {string}
     * @returns {string}
     * @private
     */
    function _removeTagPath(tag) {
        return tag.replace(TAG_PATH_REGEX, '$2').trim();
    }

    /**
     * Replace displaying full tag path with a tag name
     * @param activeTags {HTMLElement[]}
     * @private
     */
    function _changeActiveTagsTitle(activeTags) {
        let tagLabel = null;
        activeTags.map(tag => {
            tagLabel = tag.querySelector(TAG_LABEL_SELECTOR);
            tagLabel.innerHTML = _removeTagPath(tagLabel.innerHTML);
        });
    }

    /**
     * Get color theme from the tag path
     * @param tag {string}
     * @returns {string}
     * @private
     */
    function _getColor(tag) {
        return tag.replace(TAG_PATH_REGEX, '$1');
    }

    /**
     * Choosing tags depending on the color theme
     * @param tagList {HTMLElement[]}
     * @param colorTheme {'light' | 'dark'}
     * @private
     */
    function _filterTags(tagList, colorTheme) {
        let tagTitle = null;
        tagList.map(tagElement => {
            tagTitle = _getColor(tagElement.dataset.value) === colorTheme
                ? _removeTagPath(tagElement.dataset.value)
                : null;
            tagElement.hidden = !tagTitle;
            tagElement.innerHTML = tagTitle || '';
        });
    }

    /**
     * Register namespaceFilter custom action
     */
    DependsOn.ActionRegistry.register('namespaceFilter', function namespaceFilter(isDark) {

        const element = this.$el[0];
        const colorTheme = isDark ? 'dark' : 'light';
        const tagList = Array.from(element.querySelector(SELECT_LIST_SELECTOR).children);
        const activeTagsElement = element.querySelector(TAG_LIST_SELECTOR);
        const activeTagsList = Array.from(activeTagsElement && activeTagsElement.children);
        const oldColorTheme = activeTagsList.length > 0
            ? _getColor(activeTagsList[0].querySelector('input').value)
            : null;

        _changeActiveTagsTitle(activeTagsList);
        _filterTags(tagList, colorTheme);

        // Discard active tags if color theme was changed
        if (oldColorTheme && (oldColorTheme !== colorTheme)) {
            activeTagsElement.innerHTML = '';
        }
    });
})(Granite, Granite.$, Granite.DependsOnPlugin);