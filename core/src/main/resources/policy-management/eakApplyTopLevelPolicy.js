window.eakApplyTopLevelPolicy = (editable) => {
    const rules = JSON.parse('%s').rules;
    const containers = rules.flatMap(rule => rule.containers);
    const resName = containers.find(container => editable.config.path.endsWith(container));
    if (!resName) {
        editable.config.eakIsRoot = false;
        return;
    }

    const filteredRules = rules.filter(rule => rule.containers).filter(rule => rule.containers.includes(resName));
    if (!filteredRules || !filteredRules.length) {
        return;
    }
    const listenerJson = {isEditConfig: true, rules: filteredRules};
    editable.config.editConfig.listeners.updatecomponentlist = Granite.PolicyResolver.build(JSON.stringify(listenerJson));
    editable.config.eakIsRoot = true;
    editable.config.eakResourceName = resName;
    editable.config.policyPath = 'fake-policies';
};
