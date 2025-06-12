require('../../utils/js.txt');
require('../js.txt');

const task$$ = () => new Promise(resolve => setTimeout(resolve, 0));

describe('DependsOn: ActionTabVisibility', () => {
    beforeEach(async() => {
        Granite.DependsOnPlugin.initialize(
            $('body').html(require('./dependsOnActionTabVisibility.template.html'))
        );
        await task$$();
        await task$$();
    });
    afterEach(() => {
        // Restore original content
        $('body').html('');
    });
    describe('tab-visibility action should affect tab panel', () => {
        test('tab-visibility action should provide hidden tag on the tab panel', () => {
            $('#enableTab').prop('checked', false).trigger('change');
            expect($('#tab-2').attr('hidden')).not.toBeUndefined();
        });
        test('tab-visibility action should remove hidden tag on the tab panel on activation', () => {
            $('#enableTab').prop('checked', true).trigger('change');
            expect($('#tab-2').attr('hidden')).toBeUndefined();
        });
    });

    describe('tab-visibility action should affect tab control', () => {
        test('tab-visibility action should provide hidden tag on the tab control', () => {
            $('#enableTab').prop('checked', false).trigger('change');
            expect($('#tab-2-control').attr('hidden')).not.toBeUndefined();
        });
        test('tab-visibility action should remove hidden tag on the tab control on activation', () => {
            $('#enableTab').prop('checked', true).trigger('change');
            expect($('#tab-2-control').attr('hidden')).toBeUndefined();
        });
    });

    describe('tab-visibility action should affect tab children', () => {
        test('tab-visibility action should hide all fields on the tab', () => {
            $('#enableTab').prop('checked', false).trigger('change');
            expect($('#tab-2').find('.coral-Form-field').not('[hidden]').length).toBe(0);
        });
        test('tab-visibility action should show all fields on the tab on activation', () => {
            $('#enableTab').prop('checked', true).trigger('change');
            expect($('#tab-2').find('.coral-Form-field').not('[hidden]').length).toBeGreaterThan(1);
        });

        describe('conditional fields should depend on their own conditions', () => {
            const activateTab = () => $('#enableTab').prop('checked', true).trigger('change');
            beforeEach(activateTab);
            test('Switch off conditional field should hide depending field', () => {
                $('#switchDependent').prop('checked', false).trigger('change');
                expect($('#dependentOn').attr('hidden')).not.toBeUndefined();
                expect($('#dependentOff').attr('hidden')).toBeUndefined();
                expect($('#dependentOffAlt').attr('hidden')).toBeUndefined();
            });
            test('Switch on conditional field should show depending field', () => {
                $('#switchDependent').prop('checked', true).trigger('change');
                expect($('#dependentOn').attr('hidden')).toBeUndefined();
                expect($('#dependentOff').attr('hidden')).not.toBeUndefined();
                expect($('#dependentOffAlt').attr('hidden')).not.toBeUndefined();
            });
            test('Switch off conditional field should hide depending field after tab activation', () => {
                $('#switchDependent').prop('checked', false).trigger('change');
                activateTab();
                expect($('#dependentOn').attr('hidden')).not.toBeUndefined();
                expect($('#dependentOff').attr('hidden')).toBeUndefined();
                expect($('#dependentOffAlt').attr('hidden')).toBeUndefined();
            });
            test('Switch on conditional field should show depending field after tab activation', () => {
                $('#switchDependent').prop('checked', true).trigger('change');
                activateTab();
                expect($('#dependentOn').attr('hidden')).toBeUndefined();
                expect($('#dependentOff').attr('hidden')).not.toBeUndefined();
                expect($('#dependentOffAlt').attr('hidden')).not.toBeUndefined();
            });
        });
    });
});
