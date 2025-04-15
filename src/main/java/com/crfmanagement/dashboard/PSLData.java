package com.crfmanagement.dashboard;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class PSLData implements Serializable {
    private static final long serialVersionUID = 1L;
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

    // NEW: List of Step objects for each step in the PSL, to allow easier updates
    private final List<Step> steps;

    public PSLData(int psl, Integer crf, String description, String utDate, String uaDate) {
        this.psl = psl;
        this.crf = crf;
        this.description = (description != null ? description : "No Description");
        this.utDate = (utDate != null ? utDate : "No UT Date");
        this.uaDate = (uaDate != null ? uaDate : "No UA Date");
        this.stepDetails = new StringBuilder();
        this.firstIncompleteStep = "None";
        this.assignees = new StringBuilder();
        this.steps = new ArrayList<>();  // NEW: initialize step list
    }

    public int getPsl() { return psl; }
    public Integer getCrf() { return crf; }
    public String getDescription() { return description; }
    public String getUtDate() { return utDate; }
    public String getUaDate() { return uaDate; }
    public int getTotalSteps() { return totalSteps; }
    public int getCompletedSteps() { return completedSteps; }
    public String getStepDetails() { return stepDetails.toString(); }
    public String getFirstIncompleteStep() { return firstIncompleteStep; }
    public String getAssignees() { return assignees.toString(); }
    public List<Step> getSteps() { return steps; } // NEW: expose step list

    /**
     * Add a step entry to this PSLData. This method is called when reading from Excel.
     * @param step the step name or document stage.
     * @param isComplete whether the step is marked complete.
     * @param assignee the assignee for this step (or "Unassigned").
     */
    public void addStep(String step, boolean isComplete, String assignee) {
        totalSteps++;
        if (isComplete) {
            completedSteps++;
        } else if (firstIncompleteStep.equals("None")) {
            // firstIncompleteStep is the first encountered incomplete step
            firstIncompleteStep = step;
        }
        // Append to textual details
        stepDetails.append(step)
                   .append(isComplete ? " (Complete)" : " (Incomplete)")
                   .append("; ");
        // Append assignee (avoid duplicates)
        if (assignee != null && !assignees.toString().contains(assignee)) {
            assignees.append(assignee).append(", ");
        }
        // NEW: Also store as Step object in list for easier manipulation
        steps.add(new Step(step, assignee, isComplete));
    }

    /**
     * Mark a specific step as completed (if not already) and update counts and detail strings.
     * This method is used in the enhanced UI when a user marks a step complete.
     * @param stepName the name of the step to mark as complete.
     */
    public void markStepComplete(String stepName) {
        for (Step s : steps) {
            if (s.name.equals(stepName) && !s.completed) {
                s.completed = true;
                completedSteps++;
                // Rebuild stepDetails string
                rebuildStepDetails();
                // Update first incomplete step if necessary
                updateFirstIncomplete();
                break;
            }
        }
    }

    // Rebuild the stepDetails and assignees strings from the steps list (called after an update).
    private void rebuildStepDetails() {
        stepDetails.setLength(0);
        assignees.setLength(0);
        completedSteps = 0;
        totalSteps = steps.size();
        for (Step s : steps) {
            stepDetails.append(s.name)
                       .append(s.completed ? " (Complete)" : " (Incomplete)")
                       .append("; ");
            if (!s.completed && firstIncompleteStep.equals("None")) {
                firstIncompleteStep = s.name;
            }
            if (s.completed) {
                completedSteps++;
            }
            if (s.assignee != null && !assignees.toString().contains(s.assignee)) {
                assignees.append(s.assignee).append(", ");
            }
        }
    }

    // Update the firstIncompleteStep field based on current steps list.
    private void updateFirstIncomplete() {
        firstIncompleteStep = "None";
        for (Step s : steps) {
            if (!s.completed) {
                firstIncompleteStep = s.name;
                break;
            }
        }
    }

    /** Inner class representing an individual step of a PSL (name, assignee, status). */
    public static class Step implements Serializable {
        public String name;
        public String assignee;
        public boolean completed;
        public Step(String name, String assignee, boolean completed) {
            this.name = name;
            this.assignee = (assignee != null ? assignee : "Unassigned");
            this.completed = completed;
        }
    }
}
