package com.axiom.valuator.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Locale;

public class CompanyDataForm {

    // Class variables to hold company data
    private final JTextField nameField = new JTextField();
    private final JComboBox<Locale> countryField = new JComboBox<>(Locale.getAvailableLocales());
    private final JTextField yearField = new JTextField();

    private final JTextField equityField = new JTextField();
    private final JTextField equityRateField = new JTextField();
    private final JTextField debtField = new JTextField();
    private final JTextField debtRateField = new JTextField();
    private final JTextField cashField = new JTextField();
    private final JCheckBox leaderCheckBox = new JCheckBox("Is Leader");
    private final JTextField comparableStockField = new JTextField();

    // JTable to input revenue, ebitda, and free cash flow
    private final JTable financialTable = new JTable(new DefaultTableModel(new Object[]{"Year", "Revenue", "EBITDA", "Free Cash Flow"}, 0));

    public static void main(String[] args) {
        SwingUtilities.invokeLater(CompanyDataForm::new);
    }

    public CompanyDataForm() {
        // Set up the main frame
        JFrame frame = new JFrame("Company Financial Data Input");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(new BorderLayout());

        // Add company data input fields
        JPanel companyInfoPanel = new JPanel(new GridLayout(8, 2, 5, 5));

        companyInfoPanel.add(new JLabel("Company Name:"));
        companyInfoPanel.add(nameField);

        companyInfoPanel.add(new JLabel("Country:"));
        countryField.setSelectedItem(Locale.US); // Default to US
        companyInfoPanel.add(countryField);

        companyInfoPanel.add(new JLabel("Data First Year:"));
        companyInfoPanel.add(yearField);

        companyInfoPanel.add(new JLabel("Equity:"));
        companyInfoPanel.add(equityField);

        companyInfoPanel.add(new JLabel("Equity Rate:"));
        companyInfoPanel.add(equityRateField);

        companyInfoPanel.add(new JLabel("Debt:"));
        companyInfoPanel.add(debtField);

        companyInfoPanel.add(new JLabel("Debt Rate:"));
        companyInfoPanel.add(debtRateField);

        companyInfoPanel.add(new JLabel("Cash:"));
        companyInfoPanel.add(cashField);

        companyInfoPanel.add(leaderCheckBox);

        companyInfoPanel.add(new JLabel("Comparable Stock Ticker:"));
        companyInfoPanel.add(comparableStockField);

        // Add financial data table
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.add(new JScrollPane(financialTable), BorderLayout.CENTER);

        JButton addRowButton = new JButton("Add Year");
        addRowButton.addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) financialTable.getModel();
            model.addRow(new Object[]{"", "", "", ""}); // Add empty row for user input
        });

        tablePanel.add(addRowButton, BorderLayout.SOUTH);

        // Add save button
        JButton saveButton = new JButton("Save Data");
        saveButton.addActionListener(e -> saveData());

        // Add panels to the frame
        frame.add(companyInfoPanel, BorderLayout.NORTH);
        frame.add(tablePanel, BorderLayout.CENTER);
        frame.add(saveButton, BorderLayout.SOUTH);

        // Display the frame
        frame.setVisible(true);
    }

    // Method to save and print the input data to console (or save it to a file, database, etc.)
    private void saveData() {
        String name = nameField.getText();
        Locale country = (Locale) countryField.getSelectedItem();
        int dataFirstYear = Integer.parseInt(yearField.getText());
        double equity = Double.parseDouble(equityField.getText());
        double equityRate = Double.parseDouble(equityRateField.getText());
        double debt = Double.parseDouble(debtField.getText());
        double debtRate = Double.parseDouble(debtRateField.getText());
        double cash = Double.parseDouble(cashField.getText());
        boolean isLeader = leaderCheckBox.isSelected();
        String comparableStock = comparableStockField.getText();

        System.out.println("Company Name: " + name);
        System.out.println("Country: " + country.getDisplayCountry());
        System.out.println("Data First Year: " + dataFirstYear);
        System.out.println("Equity: " + equity);
        System.out.println("Equity Rate: " + equityRate);
        System.out.println("Debt: " + debt);
        System.out.println("Debt Rate: " + debtRate);
        System.out.println("Cash: " + cash);
        System.out.println("Is Leader: " + isLeader);
        System.out.println("Comparable Stock: " + comparableStock);

        // Get financial data from JTable
        DefaultTableModel model = (DefaultTableModel) financialTable.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            String year = (String) model.getValueAt(i, 0);
            String revenue = (String) model.getValueAt(i, 1);
            String ebitda = (String) model.getValueAt(i, 2);
            String freeCashFlow = (String) model.getValueAt(i, 3);

            System.out.println("Year: " + year + ", Revenue: " + revenue + ", EBITDA: " + ebitda + ", Free Cash Flow: " + freeCashFlow);
        }
    }
}
