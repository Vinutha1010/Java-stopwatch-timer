import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.LinkedHashMap;
import java.util.Map;

public class TimeTrackApp extends JFrame {

    // Theme Data Structure with Custom Font Specifications
    static class Theme {
        Color bg, card, text, btn, accent, crown;
        Font displayFont, bodyFont;

        Theme(String bg, String card, String text, String btn, String accent, String crown, String fontName) {
            this.bg = Color.decode(bg);
            this.card = Color.decode(card);
            this.text = Color.decode(text);
            this.btn = Color.decode(btn);
            this.accent = Color.decode(accent);
            this.crown = Color.decode(crown);

            this.displayFont = new Font(fontName, Font.BOLD, 26);
            this.bodyFont = new Font(fontName, Font.BOLD, 11);
        }
    }

    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private String currentTheme = "Cyberpunk";

    // Application State
    private String mode = "Stopwatch"; 
    private boolean running = false;
    private long startTime = 0;
    private long elapsedTime = 0; 
    private long timerDuration = 0; 
    private Timer clockTimer;

    // Window Drag Coordinates
    private Point dragOffset;

    // UI Components
    private JPanel mainContainer, stemPanel, bodyPanel, headerPanel, modePanel, cardPanel, inputPanel, ctrlPanel, footerPanel;
    private JLabel titleLabel, timeDisplay, minLabel;
    private JButton swBtn, tmBtn, startBtn, resetBtn, closeBtn, minBtn;
    private JTextField minutesInput;
    private JComboBox<String> themeSelector;

    public TimeTrackApp() {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setSize(280, 310); // Perfectly proportioned circular layout
        setBackground(new Color(0, 0, 0, 0)); // Transparent window background

        // Position near top right of screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setLocation(screenBounds.width - 300, 80);

        initializeThemes();
        setupUI();
        setupDragAndDrop();
        applyTheme();

        clockTimer = new Timer(100, e -> updateClock());
        clockTimer.start();
    }

    private void initializeThemes() {
        // Theme palette along with theme-specific font families
        themes.put("Cyberpunk", new Theme("#0f0f1b", "#1a1a2e", "#00fff5", "#ff007f", "#ffe600", "#ff007f", "Consolas"));
        themes.put("Dracula",   new Theme("#282a36", "#44475a", "#f8f8f2", "#bd93f9", "#50fa7b", "#ff79c6", "Segoe UI"));
        themes.put("Emerald",   new Theme("#064e3b", "#047857", "#ecfdf5", "#059669", "#34d399", "#10b981", "Trebuchet MS"));
        themes.put("Midnight",  new Theme("#121212", "#1e1e1e", "#ffffff", "#3700b3", "#bb86fc", "#03dac6", "Lucida Console"));
        themes.put("Nord",      new Theme("#2e3440", "#3b4252", "#eceff4", "#5e81ac", "#88c0d0", "#81a1c1", "Verdana"));
        themes.put("Sunset",    new Theme("#2d132c", "#801336", "#fff0f5", "#c72c41", "#ee4540", "#ffb400", "Georgia"));
        themes.put("Amethyst",  new Theme("#2a0845", "#4b1248", "#f3e5f5", "#7b1fa2", "#ea80fc", "#ba68c8", "Impact"));
        themes.put("Mocha",     new Theme("#2c221e", "#3d3029", "#f5ebe0", "#8d5b4c", "#e0a96d", "#d4a373", "Palatino Linotype"));
    }

    private void setupUI() {
        mainContainer = new JPanel();
        mainContainer.setOpaque(false);
        mainContainer.setLayout(new BorderLayout());
        setContentPane(mainContainer);

        // Top Stem/Ring of Stopwatch
        stemPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                
                // Ring loop
                g2.setColor(t.crown);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(getWidth() / 2 - 12, 2, 24, 18);
                
                // Stem button
                g2.fillRect(getWidth() / 2 - 8, 16, 16, 10);
                g2.dispose();
            }
        };
        stemPanel.setOpaque(false);
        stemPanel.setPreferredSize(new Dimension(280, 26));

        // Perfect Circular Body
        bodyPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                
                // Circular Body Fill
                g2.setColor(t.bg);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                
                // Outer Ring Border
                g2.setColor(t.crown);
                g2.setStroke(new BasicStroke(3));
                g2.draw(new Ellipse2D.Double(1.5, 1.5, getWidth() - 3, getHeight() - 3));
                g2.dispose();
            }
        };
        bodyPanel.setOpaque(false);
        bodyPanel.setPreferredSize(new Dimension(280, 280));
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        // Header Panel with Vector Clock Icon
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(200, 25));

        // Left Header: Drawn Clock Symbol + Title
        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                
                // Custom Vector Clock Icon
                g2.setColor(t.accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(2, 4, 12, 12); // Clock face
                g2.drawLine(8, 10, 8, 6);  // Hour hand
                g2.drawLine(8, 10, 11, 10); // Minute hand
                g2.dispose();
            }
        };
        titleBox.setOpaque(false);
        
        titleLabel = new JLabel("   TimeTrack");
        titleBox.add(titleLabel);

        // Right Header: Window Controls
        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        windowControls.setOpaque(false);

        minBtn = createCustomWindowButton("─");
        minBtn.addActionListener(e -> setState(Frame.ICONIFIED));

        closeBtn = createCustomWindowButton("✕");
        closeBtn.addActionListener(e -> System.exit(0));

        windowControls.add(minBtn);
        windowControls.add(closeBtn);

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(windowControls, BorderLayout.EAST);

        // Mode Switcher Panel
        modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 2));
        modePanel.setOpaque(false);

        swBtn = createFlatButton("Stopwatch");
        tmBtn = createFlatButton("Timer");

        swBtn.addActionListener(e -> switchMode("Stopwatch"));
        tmBtn.addActionListener(e -> switchMode("Timer"));
        modePanel.add(swBtn);
        modePanel.add(tmBtn);

        // Digital Time Display Panel
        cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                g2.setColor(t.card);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(190, 60));
        cardPanel.setMaximumSize(new Dimension(190, 60));

        timeDisplay = new JLabel("00:00:00");
        cardPanel.add(timeDisplay);

        // Timer Input Panel
        inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        inputPanel.setOpaque(false);
        minutesInput = new JTextField("25", 3);
        minutesInput.setHorizontalAlignment(JTextField.CENTER);
        minLabel = new JLabel("min");
        inputPanel.add(minutesInput);
        inputPanel.add(minLabel);
        inputPanel.setVisible(false);

        // Control Buttons Panel
        ctrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 2));
        ctrlPanel.setOpaque(false);
        startBtn = createFlatButton("Start");
        resetBtn = createFlatButton("Reset");

        startBtn.addActionListener(e -> toggleStart());
        resetBtn.addActionListener(e -> resetTimer());
        ctrlPanel.add(startBtn);
        ctrlPanel.add(resetBtn);

        // Footer / Theme Dropdown
        footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setOpaque(false);

        String[] themeNames = themes.keySet().toArray(new String[0]);
        themeSelector = new JComboBox<>(themeNames);
        themeSelector.setSelectedItem(currentTheme);
        themeSelector.addActionListener(e -> {
            currentTheme = (String) themeSelector.getSelectedItem();
            applyTheme();
        });
        footerPanel.add(themeSelector);

        // Assemble Layout Inside Circle
        bodyPanel.add(Box.createVerticalStrut(28));
        bodyPanel.add(headerPanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(modePanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(cardPanel);
        bodyPanel.add(inputPanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(ctrlPanel);
        bodyPanel.add(Box.createGlue());
        bodyPanel.add(footerPanel);
        bodyPanel.add(Box.createVerticalStrut(22));

        mainContainer.add(stemPanel, BorderLayout.NORTH);
        mainContainer.add(bodyPanel, BorderLayout.CENTER);
    }

    private JButton createFlatButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        return btn;
    }

    private JButton createCustomWindowButton(String symbol) {
        JButton btn = new JButton(symbol);
        btn.setPreferredSize(new Dimension(18, 18));
        btn.setMargin(new Insets(0, 0, 0, 0));
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void applyTheme() {
        Theme t = themes.get(currentTheme);

        // Update Fonts dynamically according to selected Theme
        titleLabel.setFont(t.bodyFont);
        swBtn.setFont(t.bodyFont);
        tmBtn.setFont(t.bodyFont);
        timeDisplay.setFont(t.displayFont);
        startBtn.setFont(t.bodyFont);
        resetBtn.setFont(t.bodyFont);
        minLabel.setFont(t.bodyFont);

        // Apply Color Palette
        titleLabel.setForeground(t.text);
        closeBtn.setForeground(t.text);
        minBtn.setForeground(t.text);

        swBtn.setBackground(mode.equals("Stopwatch") ? t.btn : t.card);
        swBtn.setForeground(t.text);
        tmBtn.setBackground(mode.equals("Timer") ? t.btn : t.card);
        tmBtn.setForeground(t.text);

        timeDisplay.setForeground(t.accent);
        minLabel.setForeground(t.text);

        startBtn.setBackground(t.btn);
        startBtn.setForeground(t.text);
        resetBtn.setBackground(t.card);
        resetBtn.setForeground(t.text);

        mainContainer.repaint();
    }

    private void switchMode(String newMode) {
        if (!mode.equals(newMode)) {
            running = false;
            startBtn.setText("Start");
            mode = newMode;
            elapsedTime = 0;

            if (mode.equals("Timer")) {
                inputPanel.setVisible(true);
                timeDisplay.setText("25:00");
            } else {
                inputPanel.setVisible(false);
                timeDisplay.setText("00:00:00");
            }
            applyTheme();
            revalidate();
        }
    }

    private void toggleStart() {
        if (running) {
            running = false;
            startBtn.setText("Start");
        } else {
            if (mode.equals("Timer") && elapsedTime == 0) {
                try {
                    double mins = Double.parseDouble(minutesInput.getText());
                    timerDuration = (long) (mins * 60 * 1000);
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Enter a valid number of minutes.", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            running = true;
            startTime = System.currentTimeMillis() - elapsedTime;
            startBtn.setText("Pause");
        }
    }

    private void resetTimer() {
        running = false;
        elapsedTime = 0;
        startBtn.setText("Start");
        if (mode.equals("Stopwatch")) {
            timeDisplay.setText("00:00:00");
        } else {
            timeDisplay.setText("25:00");
        }
    }

    private void updateClock() {
        if (running) {
            elapsedTime = System.currentTimeMillis() - startTime;

            if (mode.equals("Stopwatch")) {
                long totalSecs = elapsedTime / 1000;
                long hrs = totalSecs / 3600;
                long mins = (totalSecs % 3600) / 60;
                long secs = totalSecs % 60;
                timeDisplay.setText(String.format("%02d:%02d:%02d", hrs, mins, secs));
            } else {
                long remainingMillis = timerDuration - elapsedTime;
                if (remainingMillis <= 0) {
                    running = false;
                    timeDisplay.setText("00:00");
                    startBtn.setText("Start");
                    Toolkit.getDefaultToolkit().beep();
                    JOptionPane.showMessageDialog(this, "Time's up!", "Timer Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    long totalSecs = remainingMillis / 1000;
                    long mins = totalSecs / 60;
                    long secs = totalSecs % 60;
                    timeDisplay.setText(String.format("%02d:%02d", mins, secs));
                }
            }
        }
    }

    private void setupDragAndDrop() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point location = getLocation();
                setLocation(location.x + e.getX() - dragOffset.x, location.y + e.getY() - dragOffset.y);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TimeTrackApp().setVisible(true);
        });
    }
}