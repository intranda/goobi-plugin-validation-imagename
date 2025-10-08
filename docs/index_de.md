---
title: Filename Validation
identifier: intranda_validation_filename
description: Validation Plugin zur Validierung von Dateinamen und Anzahl an Dateien
published: false
keywords:
    - Goobi workflow
    - Plugin
    - Validation Plugin
---

## Einführung
Diese Dokumentation erläutert das Plugin zur Validierung von Dateinamen.


## Installation
Um das Plugin nutzen zu können, müssen folgende Dateien installiert werden:

```bash
/opt/digiverso/goobi/plugins/validation/plugin-validation-imagename-base.jar
/opt/digiverso/goobi/config/plugin_intranda_validation_filename.xml
```

Nach der Installation des Plugins kann dieses innerhalb des Workflows für die jeweiligen Arbeitsschritte ausgewählt und somit automatisch beim Abschluss einer Aufgabe durch den Nutzer ausgeführt werden. Ein Workflow wo dieses Plugin verwendet wird, könnte dabei beispielhaft wie folgt aussehen:

![Beispielhafter Aufbau eines Workflows](screen1_de.png)

Für die Verwendung des Plugins muss dieses in einem Arbeitsschritt ausgewählt sein:

![Konfiguration des Arbeitsschritts für die Nutzung des Plugins](screen2_de.png)


## Überblick und Funktionsweise

Wenn die Validierung gestartet wird, wird als erstes wird geprüft, ob die konfigurierten Ordner im Vorgang existieren und nicht leer sind.
Wenn dies der Fall ist, werden die Dateinamen überprüft. Dabei wird für jede Datei der Basisname ohne Extension gebildet und mit den konfigurierten regulären Ausdrücken verglichen. Der Name muss mit mindestens einem der Pattern übereinstimmen.

Wenn alle Dateienmanen nur Zahlen enthalten, kann optional die Reihenfolge überprüft werden. Dabei wird geprüft, dass alle Dateien aufsteigend sortiert sind und keine Lücken existieren.

Ebenfalls optional lässt sich überprüfen, ob in allen konfigurierten Ordnern die gleiche Anzahl an Dateien existiert.


## Konfiguration
Die Konfiguration des Plugins erfolgt in der Datei `plugin_intranda_validation_filename.xml` wie hier aufgezeigt:

{{CONFIG_CONTENT}}

Die folgende Tabelle enthält eine Zusammenstellung der Parameter und ihrer Beschreibungen:

Parameter               | Erläuterung
------------------------|------------------------------------
`pattern`               | Dient zur Definition von regulären Ausdrücken zur Überprüfung der Dateinamen. Beliebig oft wiederholbar.
`folder`                | Enthält den Ordnernamen, z.B. `master` oder `media` für Master- und Derivate-Ordner. Es können beliebig viele Ordner angegeben werden, Voraussetzung ist, dass diese in der Datei `goobi_config.properties` bekannt gemacht wurden
`validateOrder`         | `true`/`false`, steuert, ob bei Dateien, die nur numerische Werte enthalten, auch die Reihenfolge der Nummern geprüft werden soll
`validateImageNumbers`  | `true`/`false`, steuert, ob geprüft werden soll, dass alle Ordner die gleiche Anzahl an Dateien enthalten
