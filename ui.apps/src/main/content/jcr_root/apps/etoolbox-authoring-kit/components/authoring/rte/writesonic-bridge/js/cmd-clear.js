'use strict';
(function (RTE, Class) {
    RTE.commands.ClearCommand = new Class({

        extend: RTE.commands.Command,

        toString: 'ClearCommand',

        isCommand: function (cmdStr) {
            return cmdStr === 'clear';
        },

        getProcessingOptions: function () {
            const cmd = RTE.commands.Command;
            return cmd.PO_NODELIST;
        },

        execute: function (execDef) {
            const root = execDef.editContext.root;
            while (root.childNodes.length) {
                root.childNodes[0].remove();
            }
            RTE.DomProcessor.ensureMinimumContent(execDef.editContext);
        }
    });
    RTE.commands.CommandRegistry.register('clear', RTE.commands.ClearCommand);
})(window.CUI.rte, window.Class);
