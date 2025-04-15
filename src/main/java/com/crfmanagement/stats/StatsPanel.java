package com.crfmanagement.stats;

import com.crfmanagement.task.*;

import com.crfmanagement.dashboard.*;
import com.crfmanagement.settings.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StatsPanel extends JPanel {
    private final JLabel tasksSummaryLabel;
    private final JLabel pslSummaryLabel;
    private BufferedImage tasksChartImage;
    private BufferedImage pslChartImage;

    public StatsPanel() {
        super(new BorderLayout());
        SettingsManager settingsManager = SettingsManager.getInstance();

        // Top summary labels
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2));
        tasksSummaryLabel = new JLabel("Tasks: 0 completed out of 0");
        pslSummaryLabel = new JLabel("PSLs: 0 completed out of 0");
        summaryPanel.add(tasksSummaryLabel);
        summaryPanel.add(pslSummaryLabel);
        add(summaryPanel, BorderLayout.NORTH);

        // Charts panel
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2));
        chartsPanel.setBackground(Color.WHITE);
        // Generate charts
        JFreeChart taskChart = createTaskPieChart(TaskManagementPanel.class);  // static reference or pass actual data
        JFreeChart pslChart = createPSLBarChart(DashboardPanel.class);         // static reference or pass actual data
        // Convert charts to images and display in labels
        tasksChartImage = taskChart.createBufferedImage(400, 300);
        pslChartImage = pslChart.createBufferedImage(400, 300);
        JLabel tasksChartLabel = new JLabel(new ImageIcon(tasksChartImage));
        JLabel pslChartLabel = new JLabel(new ImageIcon(pslChartImage));
        chartsPanel.add(tasksChartLabel);
        chartsPanel.add(pslChartLabel);
        add(chartsPanel, BorderLayout.CENTER);

        // Bottom panel with "Generate Report" button
        JButton reportButton = new JButton("Generate PDF Report");
        reportButton.addActionListener(e -> generatePdfReport());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(reportButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Background color from settings
        setBackground(settingsManager.getBackgroundColor());
        summaryPanel.setBackground(settingsManager.getBackgroundColor());
        bottomPanel.setBackground(settingsManager.getBackgroundColor());
        settingsManager.addPropertyChangeListener("backgroundColor", evt -> {
            Color newColor = (Color) evt.getNewValue();
            setBackground(newColor);
            summaryPanel.setBackground(newColor);
            bottomPanel.setBackground(newColor);
        });

        // Update summary counts initially
        updateSummaries();
    }

    /** Creates a pie chart of tasks completed vs incomplete using JFreeChart. */
    private JFreeChart createTaskPieChart(Class<TaskManagementPanel> taskPanelClass) {
        // In a real scenario, get actual data from TaskManagementPanel instance.
        // For demonstration, suppose TaskManagementPanel has a static method or data to get tasks list.
        List<Task> tasks = TaskManagementPanel.class.isAssignableFrom(TaskManagementPanel.class) 
                           ? getSampleTasksData() : getSampleTasksData(); // TODO: replace with actual data source
        int completedCount = 0;
        for (Task t : tasks) {
            if (t.isCompleted()) completedCount++;
        }
        int total = tasks.size();
        int pending = total - completedCount;
        // Update summary label
        tasksSummaryLabel.setText("Tasks: " + completedCount + " completed out of " + total);

        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Completed", completedCount);
        dataset.setValue("Pending", pending);
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Tasks Completion", dataset, true, true, false);
        return pieChart;
    }

    /** Creates a bar chart of PSL progress (PSLs vs completed steps) using JFreeChart. */
    private JFreeChart createPSLBarChart(Class<DashboardPanel> dashboardPanelClass) {
        // In real scenario, retrieve PSL data from DashboardPanel or PSLTrackerPanel.
        Map<Integer, PSLData> pslDataMap = getSamplePSLData(); // TODO: replace with actual data
        int totalPSLs = pslDataMap.size();
        int fullyCompleteCount = 0;
        for (PSLData data : pslDataMap.values()) {
            if (data.getCompletedSteps() == data.getTotalSteps()) {
                fullyCompleteCount++;
            }
        }
        pslSummaryLabel.setText("PSLs: " + fullyCompleteCount + " fully completed out of " + totalPSLs);

        // Create dataset for bar chart: category "PSL" with two bars (completed vs incomplete PSLs)
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(fullyCompleteCount, "Fully Complete", "PSLs");
        dataset.addValue(totalPSLs - fullyCompleteCount, "Incomplete", "PSLs");
        JFreeChart barChart = ChartFactory.createBarChart(
                "PSL Completion Status", "PSLs", "Count",
                dataset, PlotOrientation.VERTICAL, true, true, false);
        return barChart;
    }

    /** Updates summary labels for tasks and PSLs (should be called whenever underlying data changes). */
    public void updateSummaries() {
        // Assuming charts are updated at creation; if data changes dynamically, regenerate charts and images.
        // For now, just ensure labels reflect current state (they are set in createChart methods).
    }

    /** Generates a PDF report containing the summary and the charts. */
    private void generatePdfReport() {
        try (PDDocument doc = new PDDocument()) {
        	PDPage page = new PDPage(PDRectangle.A4); // ✅ Correct
            doc.addPage(page);
            PDPageContentStream content = new PDPageContentStream(doc, page);

            // Write summary text
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("CRF Management Tool - Statistics Report");
            content.endText();

            content.beginText();
            content.newLineAtOffset(50, 720);
            content.showText(tasksSummaryLabel.getText());
            content.newLineAtOffset(0, -15);
            content.showText(pslSummaryLabel.getText());
            content.endText();

            // Add charts images
            if (tasksChartImage != null) {
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject tasksImg = LosslessFactory.createFromImage(doc, tasksChartImage);
                content.drawImage(tasksImg, 50, 400, 250, 200);
            }
            if (pslChartImage != null) {
                org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject pslImg = LosslessFactory.createFromImage(doc, pslChartImage);
                content.drawImage(pslImg, 320, 400, 250, 200);
            }

            content.close();
            // Save PDF to file
            doc.save("CRF_Stats_Report.pdf");
            JOptionPane.showMessageDialog(this, "Report generated: CRF_Stats_Report.pdf", "Report Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // TODO: Replace the following sample data methods with actual data access from application:
    private List<Task> getSampleTasksData() {
        // return actual tasks from TaskManagementPanel if accessible, else dummy data for now
        return new ArrayList<>();
    }
    private Map<Integer, PSLData> getSamplePSLData() {
        // return actual PSL data from DashboardPanel if accessible, else dummy data
        return new java.util.HashMap<>();
    }
}