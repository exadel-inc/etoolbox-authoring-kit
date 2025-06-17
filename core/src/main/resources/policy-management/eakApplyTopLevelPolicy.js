window.eakApplyTopLevelPolicy = (editable) => {
    const currentLayer = getCurrentLayerName();
    if (currentLayer !== 'edit') {
        return;
    }
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

function getCurrentLayerName() {
    if (!Granite.author || !Granite.author.layerManager || !Granite.author.layerManager.getCurrentLayer) {
        return null;
    }
    return (Granite.author.layerManager.getCurrentLayer() || '').toLowerCase();
}
