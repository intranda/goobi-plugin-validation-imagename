package de.intranda.goobi.plugins;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.apache.commons.configuration.XMLConfiguration;
import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.StorageProvider;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Log4j2
@PluginImplementation
public class FilenameValidationPlugin implements IValidatorPlugin, IPlugin {

    private static final long serialVersionUID = 4370324323535912244L;
    private static final String PLUGIN_NAME = "intranda_validation_filename";
    private Step step;

    @Override
    public boolean validate() {

        Process p = step.getProzess();

        XMLConfiguration config = ConfigPlugins.getPluginConfig(PLUGIN_NAME);

        List<String> folderList = Arrays.asList(config.getStringArray("folder"));

        List<String> regexList = Arrays.asList(config.getStringArray("validation.pattern"));

        boolean validateImageNumbers = config.getBoolean("validateImageNumbers", false);

        boolean validateOrder = config.getBoolean("validateOrder", false);

        for (String configuredFolder : folderList) {
            try {
                String folder = p.getConfiguredImageFolder(configuredFolder);
                if (!validateFolder(regexList, validateOrder, folder)) {
                    return false;
                }
            } catch (IOException | SwapException | DAOException e) {
                log.error(e);
                Helper.setFehlerMeldung("plugin_validation_filenames_missingMasterFolder");
                return false;
            }
        }

        if (validateImageNumbers) {
            Map<Integer, String> folderMap = new HashMap<>();
            for (String configuredFolder : folderList) {
                try {
                    String folder = p.getConfiguredImageFolder(configuredFolder);
                    folderMap.put(StorageProvider.getInstance().getNumberOfFiles(folder), configuredFolder);
                } catch (IOException | SwapException | DAOException e) {
                    log.error(e);
                    Helper.setFehlerMeldung("plugin_validation_filenames_missingMasterFolder");
                    return false;
                }
            }
            // folderMap should contain only one entry, otherwise the number of files differ
            if (folderMap.size() > 1) {
                StringBuilder errorText = new StringBuilder();
                for (Entry<Integer, String> folder : folderMap.entrySet()) {
                    if (!errorText.isEmpty()) {
                        errorText.append(", ");
                    }
                    errorText.append(folder.getValue()).append(" (").append(folder.getKey()).append(")");
                }
                Helper.setFehlerMeldung("plugin_validation_filenames_fileSizeDiffer", errorText.toString());
                return false;
            }

        }

        return true;
    }

    private boolean validateFolder(List<String> regexList, boolean validateOrder, String foldername) {
        Path folder = Paths.get(foldername);
        if (!StorageProvider.getInstance().isDirectory(folder)) {
            Helper.setFehlerMeldung("plugin_validation_filenames_missingMasterFolder");
            return false;
        }
        List<String> filenames = StorageProvider.getInstance().list(folder.toString());
        if (filenames == null || filenames.isEmpty()) {
            Helper.setFehlerMeldung("plugin_validation_filenames_emptyMasterFolder");
            return false;
        }
        boolean allFilesContainsDigits = true;
        for (String filename : filenames) {
            String filepart = filename;
            if (filename.contains(".")) {
                filepart = filename.substring(0, filename.indexOf("."));
            }
            boolean validName = false;
            boolean containsNonDigit = false;
            for (String pattern : regexList) {
                validName = Pattern.matches(pattern, filepart);
                containsNonDigit = Pattern.matches(".*\\D+.*", filepart); //NOSONAR, regex is save here
                if (validName) {
                    break;
                }
            }

            if (!validName) {
                String s = Helper.getTranslation("plugin_validation_filenames_wrongName");
                Helper.setFehlerMeldungUntranslated(s + " " + filename);
                return false;
            }

            if (containsNonDigit) {
                allFilesContainsDigits = false;
            }
        }
        if (validateOrder && allFilesContainsDigits) {
            Integer ancestor = null;
            Integer current = null;
            for (String filename : filenames) {
                String filepart = filename;
                if (filename.contains(".")) {
                    filepart = filename.substring(0, filename.indexOf("."));
                }
                try {
                    current = Integer.parseInt(filepart);
                } catch (Exception e) {
                    String s = Helper.getTranslation("plugin_validation_filenames_notANumber");
                    Helper.setFehlerMeldungUntranslated(s + " " + filename);
                    return false;
                }
                if (ancestor == null || ancestor + 1 == current) {
                    ancestor = current;
                } else {
                    String s = Helper.getTranslation("plugin_validation_filenames_wrongOrder");
                    Helper.setFehlerMeldungUntranslated(s + " " + filename);
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public PluginType getType() {
        return PluginType.Validation;
    }

    @Override
    public String getTitle() {
        return PLUGIN_NAME;
    }

    public String getDescription() {
        return getTitle();
    }

    @Override
    public void initialize(Process inProcess) {
        log.trace("initialize");
    }

    @Override
    public Step getStep() {
        return step;
    }

    @Override
    public void setStep(Step step) {
        this.step = step;
    }

    @Override
    public Step getStepObject() {
        return getStep();
    }

    @Override
    public void setStepObject(Step so) {
        this.step = so;
    }

}
