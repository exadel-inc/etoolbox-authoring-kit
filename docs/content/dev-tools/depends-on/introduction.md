<!--
layout: content
title: 'Programming dynamic dialog behavior: DependsOn'
navTitle: Introduction
seoTitle: Introduction to DependsOn - Exadel Authoring Kit
order: 1
-->

The DependsOn Plug-in is a clientlib that executes defined actions on dependent fields.

The DependsOn Plug-in uses data attributes to get the expected configuration.
To define data attributes from JCR, use the _**granite:data**_ sub-node under the widget node.
EToolbox Authoring Kit provides a set of annotations to use DependsOn from Java code.

The DependsOn workflow consists of the following steps:

**ObservedReference**  ─┐

**ObservedReference**  ─── **QueryObserver[Query]***  ─── **Action***

**ObservedReference**  ─┘

**QueryObserver** and **Action** are always a part of the DependsOn workflow.

**Action** defines what the plug-in should do with the dependent field (show/hide, set value, etc).

The **Query** always goes with the **Action** and defines an expression that should be used as the Action's input.

**QueryObserver** holds and processes **Query** and initiates **Action** on **ObservedReferences** changes.

**ObservedReferences** are external elements or groups of elements whose values can be used inside the **Query**.

More details on the structure of DependsOn are presented below:

![DependsOn Structure](../../../img/dependson-structure.jpg)

