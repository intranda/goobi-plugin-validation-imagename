package de.intranda.goobi.plugins;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.goobi.beans.Process;
import org.goobi.beans.Step;
import org.goobi.production.enums.PluginType;
import org.goobi.production.plugin.interfaces.IPlugin;
import org.goobi.production.plugin.interfaces.IValidatorPlugin;

import de.sub.goobi.config.ConfigPlugins;
import de.sub.goobi.helper.Helper;
import de.sub.goobi.helper.exceptions.DAOException;
import de.sub.goobi.helper.exceptions.SwapException;
import lombok.extern.log4j.Log4j2;
import net.xeoh.plugins.base.annotations.PluginImplementation;

@Log4j2
@PluginImplementation
public class FilenameValidationPlugin implements IValidatorPlugin, IPlugin {

    private static final String PLUGIN_NAME = "intranda_validation_filename";
    private Step step;

    @Override
    public boolean validate() {

        Process p = step.getProzess();

        List<String> regexList = Arrays.asList(ConfigPlugins.getPluginConfig(PLUGIN_NAME).getStringArray("validation.pattern"));

        boolean validateMasterFolder = ConfigPlugins.getPluginConfig(PLUGIN_NAME).getBoolean("validateMasterFolder", false);
        boolean validateMediaFolder = ConfigPlugins.getPluginConfig(PLUGIN_NAME).getBoolean("validateMediaFolder", false);

        boolean validateOrder = ConfigPlugins.getPluginConfig(PLUGIN_NAME).getBoolean("validateOrder", false);

        if (validateMasterFolder) {
            try {
                String master = p.getImagesOrigDirectory(false);

                if (!validateFolder(regexList, validateOrder, master)) {
                    return false;
                }

            } catch (SwapException | DAOException | IOException e) {
                log.error(e);
            }
        }

        if (validateMediaFolder) {
            try {
                String media = p.getImagesTifDirectory(false);

                if (!validateFolder(regexList, validateOrder, media)) {
                    return false;
                }

            } catch (SwapException | IOException e) {
                log.error(e);
            }
        }

        return true;
    }

    private boolean validateFolder(List<String> regexList, boolean validateOrder, String foldername) {
        File folder = new File(foldername);
        if (!folder.exists()) {
            Helper.setFehlerMeldung("plugin_validation_filenames_missingMasterFolder");
            return false;
        }
        String[] filenames = folder.list();
        if (filenames == null || filenames.length == 0) {
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
            List<String> filenameList = Arrays.asList(filenames);
            Collections.sort(filenameList);
            Integer ancestor = null;
            Integer current = null;
            for (String filename : filenameList) {
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
