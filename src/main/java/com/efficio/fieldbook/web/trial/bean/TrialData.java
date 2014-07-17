package com.efficio.fieldbook.web.trial.bean;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 7/9/2014
 * Time: 5:04 PM
 */
public class TrialData {
    private BasicDetails basicDetails;
    private TrialSettingsBean trialSettings;
    private EnvironmentData environments;
    private Map treatmentFactors;

    public TrialData() {
    }

    public BasicDetails getBasicDetails() {
        return basicDetails;
    }

    public void setBasicDetails(BasicDetails basicDetails) {
        this.basicDetails = basicDetails;
    }

    public TrialSettingsBean getTrialSettings() {
        return trialSettings;
    }

    public void setTrialSettings(TrialSettingsBean trialSettings) {
        this.trialSettings = trialSettings;
    }

    public EnvironmentData getEnvironments() {
        return environments;
    }

    public void setEnvironments(EnvironmentData environments) {
        this.environments = environments;
    }

    public Map getTreatmentFactors() {
        return treatmentFactors;
    }

    public void setTreatmentFactors(Map treatmentFactors) {
        this.treatmentFactors = treatmentFactors;
    }
}
