require('../js.txt');

const { ElementAccessors } = Granite.DependsOnPlugin;

describe('DependsOn: ElementAccessors', () => {
    test.each([
        'visibility',
        'readonly',
        'required',
        'disabled',
        'get',
        'set'
    ])('ElementAccessors has at least one $1 type accessor', (type) => {
        const $el = $('<div />');
        expect(ElementAccessors._findAccessor($el, type)).toBeDefined();
    });
});
