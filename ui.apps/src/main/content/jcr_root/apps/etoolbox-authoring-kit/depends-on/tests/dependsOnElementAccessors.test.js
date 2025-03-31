require('../../utils/js.txt');
require('../js.txt');

describe('DependsOn: ElementAccessors', () => {
    const { ElementAccessors } = Granite.DependsOnPlugin;

    describe('ElementAccessors: visibility (default accessor)', () => {
        test('ElementAccessors hides element without wrapper', () => {
            const $wrapper = $('<div />');
            const $el = $('<input />');
            $wrapper.append($el);
            ElementAccessors.setVisibility($el, false);
            expect($el.attr('hidden')).toBeDefined();
            expect($wrapper.attr('hidden')).toBeUndefined();
        });

        test('ElementAccessors hides element with wrapper', () => {
            const $wrapper = $('<div class="coral-Form-fieldwrapper"/>');
            const $el = $('<input />');
            $wrapper.append($el);
            ElementAccessors.setVisibility($el, false);
            expect($el.attr('hidden')).toBeDefined();
            expect($wrapper.attr('hidden')).toBeDefined();
        });

        test('ElementAccessors shows element without wrapper', () => {
            const $wrapper = $('<div />');
            const $el = $('<input hidden />');
            $wrapper.append($el);
            ElementAccessors.setVisibility($el, true);
            expect($el.attr('hidden')).toBeUndefined();
            expect($wrapper.attr('hidden')).toBeUndefined();
        });

        test('ElementAccessors shows element with wrapper', () => {
            const $wrapper = $('<div class="coral-Form-fieldwrapper" hidden/>');
            const $el = $('<input hidden />');
            $wrapper.append($el);
            ElementAccessors.setVisibility($el, true);
            expect($el.attr('hidden')).toBeUndefined();
            expect($wrapper.attr('hidden')).toBeUndefined();
        });
    });

    describe('ElementAccessors: readonly (default accessor)', () => {
        test('ElementAccessors sets readonly state', () => {
            const $el = $('<input />');
            ElementAccessors.setReadonly($el, true);
            expect($el.attr('readonly')).toBeDefined();
        });

        test('ElementAccessors unsets readonly state', () => {
            const $el = $('<input readonly />');
            ElementAccessors.setReadonly($el, false);
            expect($el.attr('readonly')).toBeUndefined();
        });
    });

    describe('ElementAccessors: required (default accessor)', () => {
        test('ElementAccessors sets required state', () => {
            const $el = $('<input />');
            ElementAccessors.setRequired($el, true);
            expect($el.attr('required')).toBeDefined();
        });

        test('ElementAccessors unsets required state', () => {
            const $el = $('<input required />');
            ElementAccessors.setRequired($el, false);
            expect($el.attr('required')).toBeUndefined();
        });
    });

    describe('ElementAccessors: disabled (default accessor)', () => {
        test('ElementAccessors sets disabled state', () => {
            const $el = $('<input />');
            ElementAccessors.setDisabled($el, true);
            expect($el.attr('disabled')).toBeDefined();
        });

        test('ElementAccessors unsets disabled state', () => {
            const $el = $('<input disabled />');
            ElementAccessors.setDisabled($el, false);
            expect($el.attr('disabled')).toBeUndefined();
        });
    });

    describe('ElementAccessors: value (default accessor)', () => {
        test('ElementAccessors gets value', () => {
            const $el = $('<input value="test" />');
            expect(ElementAccessors.getValue($el)).toBe('test');
        });

        test('ElementAccessors sets value', () => {
            const $el = $('<input />');
            ElementAccessors.setValue($el, 'test');
            expect($el.val()).toBe('test');
        });
    });

    describe('ElementAccessors supports managed disable (legacy)', () => {
        // managed disable support trough requestDisable and supports actor management
        test('ElementAccessors sets disabled upon first actor', () => {
            const $el = $('<input />');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.requestDisable($el, true, actor1);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.requestDisable($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
        });

        test('ElementAccessors unsets disabled as soon as all actors are gone', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.requestDisable($el, true, actor1);
            ElementAccessors.requestDisable($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.requestDisable($el, false, actor1);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.requestDisable($el, false, actor2);
            expect($el.attr('disabled')).toBeUndefined();
        });

        test('ElementAccessors unsets disabled as soon as all actors are gone (mixed order)', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.requestDisable($el, true, actor1);
            ElementAccessors.requestDisable($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.requestDisable($el, false, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.requestDisable($el, false, actor1);
            expect($el.attr('disabled')).toBeUndefined();
        });

        test('ElementAccessors actor-less disable management clears all actors', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.requestDisable($el, true, actor1);
            ElementAccessors.requestDisable($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false);
            expect($el.attr('disabled')).toBeUndefined();
        });
    });

    describe('ElementAccessors supports managed disable', () => {
        test('ElementAccessors sets disabled upon first actor', () => {
            const $el = $('<input />');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setDisabled($el, true, actor1);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
        });

        test('ElementAccessors unsets disabled as soon as all actors are gone', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setDisabled($el, true, actor1);
            ElementAccessors.setDisabled($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false, actor1);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false, actor2);
            expect($el.attr('disabled')).toBeUndefined();
        });

        test('ElementAccessors unsets disabled as soon as all actors are gone (mixed order)', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setDisabled($el, true, actor1);
            ElementAccessors.setDisabled($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false, actor1);
            expect($el.attr('disabled')).toBeUndefined();
        });

        test('ElementAccessors actor-less disable management clears all actors', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setDisabled($el, true, actor1);
            ElementAccessors.setDisabled($el, true, actor2);
            expect($el.attr('disabled')).toBeDefined();
            ElementAccessors.setDisabled($el, false);
            expect($el.attr('disabled')).toBeUndefined();
        });
    });

    describe('ElementAccessors supports managed readonly', () => {
        test('ElementAccessors sets readonly upon first actor', () => {
            const $el = $('<input />');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setReadonly($el, true, actor1);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, true, actor2);
            expect($el.attr('readonly')).toBeDefined();
        });

        test('ElementAccessors unsets readonly as soon as all actors are gone', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setReadonly($el, true, actor1);
            ElementAccessors.setReadonly($el, true, actor2);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, false, actor1);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, false, actor2);
            expect($el.attr('readonly')).toBeUndefined();
        });

        test('ElementAccessors unsets readonly as soon as all actors are gone (mixed order)', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setReadonly($el, true, actor1);
            ElementAccessors.setReadonly($el, true, actor2);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, false, actor2);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, false, actor1);
            expect($el.attr('readonly')).toBeUndefined();
        });

        test('ElementAccessors actor-less readonly management clears all actors', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setReadonly($el, true, actor1);
            ElementAccessors.setReadonly($el, true, actor2);
            expect($el.attr('readonly')).toBeDefined();
            ElementAccessors.setReadonly($el, false);
            expect($el.attr('readonly')).toBeUndefined();
        });
    });

    describe('ElementAccessors supports managed required', () => {
        test('ElementAccessors sets required upon first actor', () => {
            const $el = $('<input />');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setRequired($el, true, actor1);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, true, actor2);
            expect($el.attr('required')).toBeDefined();
        });

        test('ElementAccessors unsets required as soon as all actors are gone', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setRequired($el, true, actor1);
            ElementAccessors.setRequired($el, true, actor2);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, false, actor1);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, false, actor2);
            expect($el.attr('required')).toBeUndefined();
        });

        test('ElementAccessors unsets required as soon as all actors are gone (mixed order)', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setRequired($el, true, actor1);
            ElementAccessors.setRequired($el, true, actor2);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, false, actor2);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, false, actor1);
            expect($el.attr('required')).toBeUndefined();
        });

        test('ElementAccessors actor-less required management clears all actors', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setRequired($el, true, actor1);
            ElementAccessors.setRequired($el, true, actor2);
            expect($el.attr('required')).toBeDefined();
            ElementAccessors.setRequired($el, false);
            expect($el.attr('required')).toBeUndefined();
        });
    });

    describe('ElementAccessors supports managed visibility', () => {
        // Note: setVisibility is an inverted method, so managed visibility collects requests to hide
        test('ElementAccessors sets hidden upon first actor', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setVisibility($el, false, actor1);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, false, actor2);
            expect($el.attr('hidden')).toBeDefined();
        });

        test('ElementAccessors unsets hidden as soon as all actors are gone', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setVisibility($el, false, actor1);
            ElementAccessors.setVisibility($el, false, actor2);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, true, actor1);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, true, actor2);
            expect($el.attr('hidden')).toBeUndefined();
        });

        test('ElementAccessors unsets hidden as soon as all actors are gone (mixed order)', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setVisibility($el, false, actor1);
            ElementAccessors.setVisibility($el, false, actor2);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, true, actor2);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, true, actor1);
            expect($el.attr('hidden')).toBeUndefined();
        });

        test('ElementAccessors actor-less visibility management clears all actors', () => {
            const $el = $('<input/>');
            const actor1 = {};
            const actor2 = {};
            ElementAccessors.setVisibility($el, false, actor1);
            ElementAccessors.setVisibility($el, false, actor2);
            expect($el.attr('hidden')).toBeDefined();
            ElementAccessors.setVisibility($el, true);
            expect($el.attr('hidden')).toBeUndefined();
        });
    });
});
