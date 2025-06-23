require('../../utils/js.txt');
require('../js.txt');

describe('DependsOn: Utils', () => {
    const { castToType, parseSafe } = Granite.DependsOnPlugin;
    describe('parseSafe', () => {
        test('empty object', () => {
            expect(parseSafe('{}')).toStrictEqual({});
        });
        test('string', () => {
            expect(parseSafe('"hello"')).toStrictEqual('hello');
        });
        test('number', () => {
            expect(parseSafe('0')).toStrictEqual(0);
        });
        test('incorrect json', () => {
            expect(parseSafe('incorrect')).toStrictEqual(null);
        });
        test('empty value', () => {
            expect(parseSafe('')).toStrictEqual(null);
        });
        test('empty string', () => {
            expect(parseSafe('""')).toStrictEqual('');
        });
    });
    describe('castToType', () => {
        test('string', () => {
            expect(typeof castToType('hello', 'string')).toBe('string');
        });
        test('number', () => {
            expect(typeof castToType(0, 'number')).toBe('number');
        });
        test('json string', () => {
            expect(typeof castToType('"hello"', 'json')).toBe('string');
        });
        test('incorrect json', () => {
            expect(typeof castToType('incorrect', 'json')).toBe('undefined');
        });
        test('boolean string', () => {
            expect(typeof castToType('false', 'boolstring')).toBe('boolean');
        });
        test('boolean', () => {
            expect(typeof castToType(true, 'boolean')).toBe('boolean');
        });
    });
});
