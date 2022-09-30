/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function (RTE, Class) {
    'use strict';

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
