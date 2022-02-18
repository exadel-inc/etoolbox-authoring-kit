/**
 * Custom StyleLint plugin to prevent using absolute paths in less @import directive
 *
 * As the less sources can be used by a library consumers "absolute-like" paths that is resolved from the library root
 * e.g. "modules/esl-module/core.less" is not allowed as they are not resolvable from outside without manual configuration
 * only relative paths should be used in bounds of ESL library.
 */

const stylelint = require("stylelint");

const ruleName = "esl-less/import-root";
const messages = stylelint.utils.ruleMessages(ruleName, {
  expected: "Only relative paths should be used in bounds of ESL library modules"
});

module.exports = stylelint.createPlugin(ruleName, function (ruleValue) {
  return function (root, result) {
    const validOptions = stylelint.utils.validateOptions(result, ruleName, {
      actual: ruleValue,
      possible: ["off", "never"]
    });

    if (!validOptions || ruleValue === 'off') return;

    root.walkAtRules("import", decl => {
      const path = decl.params
        .replace(/^\s*(url\()?['"]?/, '')
        .replace(/['"]?\)?\s*$/, '');

      // Referencing file from the same directory
      if (path.indexOf('/') === -1) return;

      // Relative path
      if (path.startsWith('.')) return;

      // Others are not acceptable
      stylelint.utils.report({
        message: messages.expected,
        node: decl,
        result,
        ruleName
      });
    });
  };
});

module.exports.ruleName = ruleName;
module.exports.messages = messages;
