window.eakApplyTopLevelPolicy = (editable) => {
    const {config} = editable;
    const rules = JSON.parse('%s').rules;
    const containers = rules.flatMap((rule) => rule.containers);
    const resName = containers.find((container) => config.path.endsWith(container));

    const filteredRules = rules.filter((rule) => (rule.containers || []).includes(resName));
    if (!filteredRules || !filteredRules.length) {
        return;
    }
    const listenerJson = {isEditConfig: true, rules: filteredRules};
    const updatecomponentlist = Granite.PolicyResolver.build(JSON.stringify(listenerJson));
    config.editConfig = config.editConfig || {};
    config.editConfig.listeners = Object.assign({}, config.editConfig.listeners, {updatecomponentlist});
    config.eakResourceName = resName;
};

