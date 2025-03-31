require('../../utils/js.txt');
require('../js.txt');

describe('DependsOn: ActionRegistry', () => {
    const { ActionRegistry } = Granite.DependsOnPlugin;

    describe('Test ActionRegistry base action availability', () => {
        test.each([
            'show',
            'hide',
            'enable',
            'disable',
            'required',
            'readonly',
            'set',
            'set-if-blank'
        ])('Default action "%s" should be available', (name) => {
            expect(ActionRegistry.getAction(name)).toBeDefined();
        });

        test.each([
            'visibility',
            'enabled',
            'disabled',
        ])('Alias action "%s" should be available', (name) => {
            expect(ActionRegistry.getAction(name)).toBeDefined();
        });
    });

    describe('Test ActionRegistry custom action registration', () => {
        const testAction = jest.fn();
        const consoleWarnSpy = jest.spyOn(console, 'warn')
            .mockImplementation(() => {});

        beforeAll(() => ActionRegistry.register('test-action', testAction));

        test('Test action is available', () => {
            expect(ActionRegistry.getAction('test-action')).toBe(testAction);
        });

        test('Overriding the action with existing name throws warn massage', () => {
            ActionRegistry.register('test-action', () => {});
            expect(ActionRegistry.getAction('test-action')).not.toBe(testAction);
            expect(consoleWarnSpy).toHaveBeenCalledWith(expect.stringContaining('overridden'));
        });

        test('Incorrect action definition throws error', () => {
            expect(() => ActionRegistry.register('test-action-1', null)).toThrowError();
            expect(() => ActionRegistry.register('test-action-2', {})).toThrowError();
        });

        describe('ActionRegistry auto sanitize action name', () => {
            beforeEach(() => consoleWarnSpy.mockReset());

            test('Action name should be sanitized to lower case', () => {
                ActionRegistry.register('Test-Action-case', testAction);
                expect(ActionRegistry.getAction('test-action-case')).toBe(testAction);
                expect(consoleWarnSpy).toHaveBeenCalledWith(expect.stringContaining('sanitized'));
            });

            test('Action name special characters should be removed', () => {
                ActionRegistry.register('test_action$special@', testAction);
                expect(ActionRegistry.getAction('testactionspecial')).toBe(testAction);
                expect(consoleWarnSpy).toHaveBeenCalledWith(expect.stringContaining('sanitized'));
            });

            test('Action name should be trimmed (trim does not count as warning)', () => {
                ActionRegistry.register(' test-action-trim ', testAction);
                expect(ActionRegistry.getAction('test-action-trim')).toBe(testAction);
                expect(consoleWarnSpy).not.toHaveBeenCalled();
            });
        });
    });
});
