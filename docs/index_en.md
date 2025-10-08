---
title: Filename Validation
identifier: intranda_validation_filename
description: Validation Plugin to validate file names and number of files
published: false
keywords:
    - Goobi workflow
    - Plugin
    - Validation Plugin
---

## Introduction
This documentation explains the plugin for to validate file names.


## Installation
To be able to use the plugin, the following files must be installed:

```bash
/opt/digiverso/goobi/plugins/validation/plugin-validation-imagename-base.jar
/opt/digiverso/goobi/config/plugin_intranda_validation_filename.xml
```

Once the plugin has been installed, it can be selected within the workflow for the respective work steps and thus automatically executed when a task is completed by the user. A workflow where this plugin is used could look like the following example:

![Example of a workflow structure](screen1_en.png)

To use the plugin, it must be selected in one step:

![Configuration of the work step for using the plugin](screen2_en.png)


## Overview and functionality

When validation is started, the first thing that is checked is whether the configured folders exist in the process and are not empty.
If this is the case, the file names are checked. For each file, the base name without extension is formed and compared with the configured regular expressions. The name must match at least one of the patterns.

If all file names contain only numbers, the order can be checked optionally. This checks that all files are sorted in ascending order and that there are no gaps.

It is also possible to check whether the same number of files exists in all configured folders.


## Configuration
The plugin is configured in the file `plugin_intranda_validation_ZZZ.xml` as shown here:

{{CONFIG_CONTENT}}

The following table contains a summary of the parameters and their descriptions:

Parameter               | Explanation
------------------------|------------------------------------
`pattern`               | Used to define regular expressions for checking file names. Can be repeated as often as desired.
`folder`                | Contains the folder name, e.g. `master` or `media` for master and derivative folders. Any number of folders can be specified, provided that they have been declared in the `goobi_config.properties` file.
`validateOrder`         | `true`/`false`, controls whether the order of numbers should also be checked if files only contain numerical values
`validateImageNumbers`  | `true`/`false`, controls whether to check that all folders contain the same number of files
