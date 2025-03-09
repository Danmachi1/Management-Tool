package com.crfmanagement.dashboard;

public class PSLData {
    private final int psl;
    private final Integer crf;
    private final String description;
    private final String utDate;
    private final String uaDate;
    private int totalSteps;
    private int completedSteps;
    private final StringBuilder stepDetails;
    private String firstIncompleteStep;
    private final StringBuilder assignees;

    public PSLData(int psl, Integer crf, String description, String utDate, String uaDate) {
        this.psl = psl;
        this.crf = crf;
        this.description = description != null ? description : "No Description";
        this.utDate = utDate != null ? utDate : "No UT Date";
        this.uaDate = uaDate != null ? uaDate : "No UA Date";
        this.stepDetails = new StringBuilder();
        this.firstIncompleteStep = "None";
        this.assignees = new StringBuilder();
    }

    public int getPsl() {
        return psl;
    }

    public Integer getCrf() {
        return crf;
    }

    public String getDescription() {
        return description;
    }

    public String getUtDate() {
        return utDate;
    }

    public String getUaDate() {
        return uaDate;
    }

    public int getTotalSteps() {
        return totalSteps;
    }

    public int getCompletedSteps() {
        return completedSteps;
    }

    public String getStepDetails() {
        return stepDetails.toString();
    }

    public String getFirstIncompleteStep() {
        return firstIncompleteStep;
    }

    public String getAssignees() {
        return assignees.toString();
    }

    public void addStep(String step, boolean isComplete, String assignee) {
        totalSteps++;
        if (isComplete) {
            completedSteps++;
        } else if (firstIncompleteStep.equals("None")) {
            firstIncompleteStep = step;
        }
        stepDetails.append(step).append(isComplete ? " (Complete)" : " (Incomplete)").append("; ");
        if (!assignees.toString().contains(assignee)) {
            assignees.append(assignee).append(", ");
        }
    }
}
