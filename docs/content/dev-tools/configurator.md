<!--
layout: content
title: User-friendly OSGi configs with Exadel Authoring Kit Configurator
navTitle: Configurator
seoTitle: Configurator - Exadel Authoring Kit
order: 6
-->

Configurator is a tool that allows editing OSGi configurations is a user-friendly manner.

<em>Note:</em> As of Exadel Authoring Kit 2.7.0, <em>Configurator</em> is is an experimental feature. You need to specially enable it thorugh your own AEM project bu adding a configuration file like the following:

Path: `ui.config/src/main/content/jcr_root/apps/your_app/osgiconfig/config/com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener.xml`
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jcr:root
    xmlns:sling="http://sling.apache.org/jcr/sling/1.0"
    xmlns:jcr="http://www.jcp.org/jcr/1.0"
    jcr:primaryType="sling:OsgiConfig"
    enabled="{Boolean}true"
/>

```

To edit a configuration, navigate to _http://<your AEM address>:4502/etoolbox/config.html/<your configuration PID>_, e.g.`http://localhost:4502/etoolbox/config.html/com.adobe.cq.wcm.core.components.internal.form.FormHandlerImpl`. View and edit configuration as necessary, then press _Save_. The new config will be applied immediately (the corresponding OSGi component restarts if necessary). Additionally, the config will be stored under `/conf/etoolbox/authoring-kit/configurator` while the config that existed before save will be preserved for backup.

Now even after the AEM instance is restarted or recreated (as it goes with AEMaaCS instances), the Toolkit will re-read the stored config from the resource under `/conf/etoolbox/authoring-kit` and re-apply it.

However, if you press the _Reset_ button, the stored config will be erased and the backup config will be reinstalled.

A common development pattern is to allow editing a config online for the sake of immediate changes but the "transfer" it to the code base. In this case, it makes sense to tell the Configurator to clean up a config stored under `/conf/etoolbox/authoring-kit` immediately as it sees it and not apply since the same config is now declared elsewhere in the code. You can do it by setting the property _cleanUp_ in the `com.exadel.aem.toolkit.core.configurator.services.ConfigChangeListener.xml` next to the _enabled_ property. The value of _cleanUp_ is an array of configuration PIDs that you want the Configurator to clean up on sight.

If you are satisfied with the change of an OSGi config in an author instance, you can populate it to the publishers by pressing the "Publish" button. It will replicate the config asynchronously. Press "Unpublish" to reset config in the publishers. You will then be offered to reset it in the author as well.
