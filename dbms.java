package asad;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.sql.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Asad extends JFrame {
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);

    public Asad() {
        setTitle("DBMS Project");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Initialize Panels
        JPanel roleSelectionPanel = new JPanel();
        JButton userButton = new JButton("User");
        JButton adminButton = new JButton("Admin");

        roleSelectionPanel.add(userButton);
        roleSelectionPanel.add(adminButton);

        JPanel userPanel = new UserPanel();
        JPanel adminPanel = new AdminPanel();

        mainPanel.add(roleSelectionPanel, "RoleSelection");
        mainPanel.add(userPanel, "UserPanel");
        mainPanel.add(adminPanel, "AdminPanel");

        add(mainPanel);

        userButton.addActionListener(e -> cardLayout.show(mainPanel, "UserPanel"));
        adminButton.addActionListener(e -> cardLayout.show(mainPanel, "AdminPanel"));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Asad frame = new Asad();
            frame.setVisible(true);
        });
    }

    class UserPanel extends JPanel {
        private JTextField rollNum1Field;
        private JTextField rollNum2Field;
        private JTextField rollNum3Field;
        private JButton uploadButton;
        private JButton viewMarksButton;
        private JButton homeButton; // Home button for UserPanel

        public UserPanel() {
            setLayout(new GridLayout(5, 2));

            rollNum1Field = new JTextField(10);
            rollNum2Field = new JTextField(10);
            rollNum3Field = new JTextField(10);
            uploadButton = new JButton("Upload Project");
            viewMarksButton = new JButton("View Marks");
            homeButton = new JButton("Home"); // Initialize home button

            add(new JLabel("Roll Number 1:"));
            add(rollNum1Field);
            add(new JLabel("Roll Number 2:"));
            add(rollNum2Field);
            add(new JLabel("Roll Number 3:"));
            add(rollNum3Field);
            add(uploadButton);
            add(viewMarksButton);
            add(homeButton); // Add home button

            uploadButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    uploadProject();
                }
            });

            viewMarksButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    viewMarks();
                }
            });

            homeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(mainPanel, "RoleSelection"); // Switch to RoleSelection panel
                }
            });
        }

        private void uploadProject() {
            String rollNum1 = rollNum1Field.getText();
            String rollNum2 = rollNum2Field.getText();
            String rollNum3 = rollNum3Field.getText();

            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                try (FileInputStream fis = new FileInputStream(selectedFile)) {
                    Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asad", "root", "");
                    String sql = "INSERT INTO users (roll_num1, roll_num2, roll_num3, project_file) VALUES (?, ?, ?, ?)";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, rollNum1);
                    pstmt.setString(2, rollNum2);
                    pstmt.setString(3, rollNum3);
                    pstmt.setBinaryStream(4, fis, (int) selectedFile.length()); // Set the length explicitly
                    pstmt.executeUpdate();
                    JOptionPane.showMessageDialog(this, "Project uploaded successfully!");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Failed to upload project.");
                }
            }
        }

        private void viewMarks() {
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asad", "root", "");
                String sql = "SELECT u.roll_num1, u.roll_num2, u.roll_num3, m.mark FROM users u LEFT JOIN marks m ON u.id = m.user_id";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                StringBuilder result = new StringBuilder("<html>Marks:<br>");
                while (rs.next()) {
                    result.append(rs.getString("roll_num1")).append(", ")
                            .append(rs.getString("roll_num2")).append(", ")
                            .append(rs.getString("roll_num3")).append(": ")
                            .append(rs.getDouble("mark")).append("<br>"); // Use getDouble() for decimal points
                }
                result.append("</html>");
                JOptionPane.showMessageDialog(this, result.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to retrieve marks.");
            }
        }
    }

    class AdminPanel extends JPanel {
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JButton loginButton;
        private JPanel adminControls;
        private JButton homeButton; // Home button for AdminPanel

        public AdminPanel() {
            setLayout(new BorderLayout());

            JPanel loginPanel = new JPanel(new GridLayout(3, 2));

            usernameField = new JTextField(10);
            passwordField = new JPasswordField(10);
            loginButton = new JButton("Login");
            homeButton = new JButton("Home"); // Initialize home button

            loginPanel.add(new JLabel("Username:"));
            loginPanel.add(usernameField);
            loginPanel.add(new JLabel("Password:"));
            loginPanel.add(passwordField);
            loginPanel.add(loginButton);

            add(loginPanel, BorderLayout.NORTH);

            adminControls = new JPanel();
            adminControls.setLayout(new BoxLayout(adminControls, BoxLayout.Y_AXIS));

            JScrollPane scrollPane = new JScrollPane(adminControls); // Add adminControls to JScrollPane
            add(scrollPane, BorderLayout.CENTER);

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if ("admin".equals(usernameField.getText()) && "1234".equals(new String(passwordField.getPassword()))) {
                        adminControls.setVisible(true);
                        showProjects();
                    } else {
                        JOptionPane.showMessageDialog(AdminPanel.this, "Invalid login.");
                    }
                }
            });

            homeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cardLayout.show(mainPanel, "RoleSelection"); // Switch to RoleSelection panel
                }
            });

            JPanel buttonPanel = new JPanel();
            buttonPanel.add(homeButton);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private void showProjects() {
            adminControls.removeAll();
            try {
                Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/asad", "root", "");
                String sql = "SELECT u.id, u.roll_num1, u.roll_num2, u.roll_num3, m.mark, u.project_file " +
                             "FROM users u LEFT JOIN marks m ON u.id = m.user_id";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    int userId = rs.getInt("id");
                    String rollNum1 = rs.getString("roll_num1");
                    String rollNum2 = rs.getString("roll_num2");
                    String rollNum3 = rs.getString("roll_num3");
                    Blob projectFile = rs.getBlob("project_file");
                    Double mark = rs.getDouble("mark");

                    JPanel projectPanel = new JPanel(new GridLayout(1, 6));
                    projectPanel.add(new JLabel("Project:"));
                    projectPanel.add(new JLabel(rollNum1 + ", " + rollNum2 + ", " + rollNum3));

                    JButton downloadButton = new JButton("Download Project");
                    projectPanel.add(downloadButton);

                    downloadButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                if (projectFile != null) {
                                    String userHome = System.getProperty("user.home");
                                    File desktopDir = new File(userHome, "Desktop");

                                    // Ensure the Desktop directory exists
                                    if (!desktopDir.exists()) {
                                        desktopDir.mkdirs(); // Create directories if they don't exist
                                    }

                                    File zipFile = new File(desktopDir, "downloaded_project_" + userId + ".zip");
                                    FileOutputStream fos = new FileOutputStream(zipFile);
                                    ZipOutputStream zipOut = new ZipOutputStream(fos);

                                    // Create a zip entry for the project file
                                    ZipEntry zipEntry = new ZipEntry("project_" + userId + ".zip");
                                    zipOut.putNextEntry(zipEntry);

                                    // Write the binary data to the zip output stream
                                    zipOut.write(projectFile.getBytes(1, (int) projectFile.length()));

                                    zipOut.close();
                                    fos.close();

                                    JOptionPane.showMessageDialog(AdminPanel.this, "Project downloaded successfully to Desktop.");
                                } else {
                                    JOptionPane.showMessageDialog(AdminPanel.this, "Project file not found.");
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(AdminPanel.this, "Failed to download project.");
                            }
                        }
                    });

                    JTextField markField = new JTextField(5);
                    projectPanel.add(markField);

                    if (mark != null) {
                        markField.setText(mark.toString());
                    }

                    JButton assignMarkButton = new JButton("Assign Mark");
                    projectPanel.add(assignMarkButton);

                    assignMarkButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                double newMark = Double.parseDouble(markField.getText());
                                String insertMarkSql = "INSERT INTO marks (user_id, mark) VALUES (?, ?) ON DUPLICATE KEY UPDATE mark = ?";
                                PreparedStatement insertMarkStmt = conn.prepareStatement(insertMarkSql);
                                insertMarkStmt.setInt(1, userId);
                                insertMarkStmt.setDouble(2, newMark);
                                insertMarkStmt.setDouble(3, newMark);
                                insertMarkStmt.executeUpdate();

                                JOptionPane.showMessageDialog(AdminPanel.this, "Mark assigned successfully!");
                            } catch (NumberFormatException | SQLException ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(AdminPanel.this, "Failed to assign mark. Please enter a valid number.");
                            }
                        }
                    });

                    adminControls.add(projectPanel);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Failed to load projects.");
            }

            revalidate();
            repaint();
        }
    }
}
