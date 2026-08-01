import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class TimeTrackApp extends JFrame {

    private static final String CONFIG_FILE = "config.properties";

    // Theme Data Structure
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
            this.bodyFont = new Font(fontName, Font.BOLD, 10);
        }
    }

    private final Map<String, Theme> themes = new LinkedHashMap<>();
    private String currentTheme = "BarbiePink"; // Default theme if no saved config exists

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
    private JButton p15Btn, p25Btn, p45Btn;
    private JTextField minutesInput;
    private JComboBox<String> themeSelector;

    public TimeTrackApp() {
        setUndecorated(true);
        setAlwaysOnTop(true);
        setSize(300, 330);
        setBackground(new Color(0, 0, 0, 0));

        // Position near top right of screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setLocation(screenBounds.width - 320, 80);

        initializeThemes();
        loadSavedTheme(); // Restore theme preference from disk
        setupUI();
        setupDragAndDrop();
        applyTheme();

        clockTimer = new Timer(100, e -> updateClock());
        clockTimer.start();
    }

    private void initializeThemes() {
        themes.put("BarbiePink",  new Theme("#ff66b2", "#fff0f5", "#d6006e", "#ff1493", "#c70067", "#ff007f", "Trebuchet MS"));
        themes.put("Cyberpunk",   new Theme("#0f0f1b", "#1a1a2e", "#00fff5", "#ff007f", "#ffe600", "#ff007f", "Consolas"));
        themes.put("Synthwave",   new Theme("#1a002c", "#2d004d", "#ff71ce", "#01cdfe", "#05ffa1", "#b967ff", "Impact"));
        themes.put("Matrix",      new Theme("#020d08", "#051f14", "#00ff66", "#008033", "#00ff66", "#00cc52", "Lucida Console"));
        themes.put("Tokyo Night", new Theme("#1a1b26", "#24283b", "#7aa2f7", "#bb9af7", "#7dcfff", "#f7768e", "Segoe UI"));
        themes.put("Solarized",   new Theme("#002b36", "#073642", "#839496", "#268bd2", "#b58900", "#d33682", "Verdana"));
        themes.put("Dracula",     new Theme("#282a36", "#44475a", "#f8f8f2", "#bd93f9", "#50fa7b", "#ff79c6", "Segoe UI"));
        themes.put("Emerald",     new Theme("#064e3b", "#047857", "#ecfdf5", "#059669", "#34d399", "#10b981", "Trebuchet MS"));
        themes.put("Midnight",    new Theme("#121212", "#1e1e1e", "#ffffff", "#3700b3", "#bb86fc", "#03dac6", "Consolas"));
        themes.put("Nord",        new Theme("#2e3440", "#3b4252", "#eceff4", "#5e81ac", "#88c0d0", "#81a1c1", "Verdana"));
        themes.put("Sunset",      new Theme("#2d132c", "#801336", "#fff0f5", "#c72c41", "#ee4540", "#ffb400", "Georgia"));
        themes.put("Mocha",       new Theme("#2c221e", "#3d3029", "#f5ebe0", "#8d5b4c", "#e0a96d", "#d4a373", "Palatino Linotype"));
    }

    // Persistence: Read Saved Theme Configuration
    private void loadSavedTheme() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (InputStream input = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(input);
                String savedTheme = prop.getProperty("theme");
                if (savedTheme != null && themes.containsKey(savedTheme)) {
                    currentTheme = savedTheme;
                }
            } catch (IOException ignored) {}
        }
    }

    // Persistence: Save Selected Theme Configuration
    private void saveCurrentTheme(String themeName) {
        try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
            Properties prop = new Properties();
            prop.setProperty("theme", themeName);
            prop.store(output, "TimeTrack Preference Config");
        } catch (IOException ignored) {}
    }

    private void setupUI() {
        mainContainer = new JPanel();
        mainContainer.setOpaque(false);
        mainContainer.setLayout(new BorderLayout());
        setContentPane(mainContainer);

        // Crown / Stem Panel
        stemPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                g2.setColor(t.crown);
                g2.setStroke(new BasicStroke(3));
                g2.drawOval(getWidth() / 2 - 12, 2, 24, 18);
                g2.fillRect(getWidth() / 2 - 8, 16, 16, 10);
                g2.dispose();
            }
        };
        stemPanel.setOpaque(false);
        stemPanel.setPreferredSize(new Dimension(300, 26));

        // Circular Body Panel
        bodyPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                g2.setColor(t.bg);
                g2.fill(new Ellipse2D.Double(0, 0, getWidth(), getHeight()));
                g2.setColor(t.crown);
                g2.setStroke(new BasicStroke(3));
                g2.draw(new Ellipse2D.Double(1.5, 1.5, getWidth() - 3, getHeight() - 3));
                g2.dispose();
            }
        };
        bodyPanel.setOpaque(false);
        bodyPanel.setPreferredSize(new Dimension(300, 300));
        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.Y_AXIS));

        // Header Panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setMaximumSize(new Dimension(180, 20));

        JPanel titleBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                g2.setColor(t.accent);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(1, 2, 11, 11);
                g2.drawLine(6, 7, 6, 4);
                g2.drawLine(6, 7, 9, 7);
                g2.dispose();
            }
        };
        titleBox.setOpaque(false);
        titleLabel = new JLabel("   TimeTrack");
        titleBox.add(titleLabel);

        JPanel windowControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 1, 0));
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
        modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        modePanel.setOpaque(false);

        swBtn = createFlatButton("Stopwatch");
        tmBtn = createFlatButton("Timer");

        swBtn.addActionListener(e -> switchMode("Stopwatch"));
        tmBtn.addActionListener(e -> switchMode("Timer"));
        modePanel.add(swBtn);
        modePanel.add(tmBtn);

        // Display Card (Single Click Start/Pause + Double Click Reset)
        cardPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Theme t = themes.get(currentTheme);
                g2.setColor(t.card);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(175, 52));
        cardPanel.setMaximumSize(new Dimension(175, 52));
        cardPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        timeDisplay = new JLabel("00:00:00");
        cardPanel.add(timeDisplay);

        cardPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    toggleStart();
                } else if (e.getClickCount() == 2) {
                    resetTimer();
                }
            }
        });

        // Compact Timer Input & Presets Panel
        inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(190, 26));

        minutesInput = new JTextField("25", 2);
        minutesInput.setHorizontalAlignment(JTextField.CENTER);
        minLabel = new JLabel("m ");

        p15Btn = createCompactPresetButton("15m");
        p25Btn = createCompactPresetButton("25m");
        p45Btn = createCompactPresetButton("45m");

        p15Btn.addActionListener(e -> setPreset(15));
        p25Btn.addActionListener(e -> setPreset(25));
        p45Btn.addActionListener(e -> setPreset(45));

        inputPanel.add(minutesInput);
        inputPanel.add(minLabel);
        inputPanel.add(p15Btn);
        inputPanel.add(p25Btn);
        inputPanel.add(p45Btn);
        inputPanel.setVisible(false);

        // Control Buttons Panel
        ctrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        ctrlPanel.setOpaque(false);
        startBtn = createFlatButton("Start");
        resetBtn = createFlatButton("Reset");

        startBtn.addActionListener(e -> toggleStart());
        resetBtn.addActionListener(e -> resetTimer());
        ctrlPanel.add(startBtn);
        ctrlPanel.add(resetBtn);

        // Footer / Theme Dropdown Selector
        footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        footerPanel.setOpaque(false);

        String[] themeNames = themes.keySet().toArray(new String[0]);
        themeSelector = new JComboBox<>(themeNames);
        themeSelector.setSelectedItem(currentTheme);
        themeSelector.addActionListener(e -> {
            currentTheme = (String) themeSelector.getSelectedItem();
            saveCurrentTheme(currentTheme); // Save preference on change
            applyTheme();
        });
        footerPanel.add(themeSelector);

        // Layout Assembly
        bodyPanel.add(Box.createVerticalStrut(32));
        bodyPanel.add(headerPanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(modePanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(cardPanel);
        bodyPanel.add(Box.createVerticalStrut(4));
        bodyPanel.add(inputPanel);
        bodyPanel.add(Box.createVerticalStrut(6));
        bodyPanel.add(ctrlPanel);
        bodyPanel.add(Box.createGlue());
        bodyPanel.add(footerPanel);
        bodyPanel.add(Box.createVerticalStrut(26));

        mainContainer.add(stemPanel, BorderLayout.NORTH);
        mainContainer.add(bodyPanel, BorderLayout.CENTER);
    }

    private JButton createFlatButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(3, 8, 3, 8));
        return btn;
    }

    private JButton createCompactPresetButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setMargin(new Insets(1, 4, 1, 4));
        return btn;
    }

    private JButton createCustomWindowButton(String symbol) {
        JButton btn = new JButton(symbol);
        btn.setPreferredSize(new Dimension(16, 16));
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

        titleLabel.setFont(t.bodyFont);
        swBtn.setFont(t.bodyFont);
        tmBtn.setFont(t.bodyFont);
        timeDisplay.setFont(t.displayFont);
        startBtn.setFont(t.bodyFont);
        resetBtn.setFont(t.bodyFont);
        minLabel.setFont(t.bodyFont);
        p15Btn.setFont(t.bodyFont);
        p25Btn.setFont(t.bodyFont);
        p45Btn.setFont(t.bodyFont);

        titleLabel.setForeground(t.text);
        closeBtn.setForeground(t.text);
        minBtn.setForeground(t.text);

        swBtn.setBackground(mode.equals("Stopwatch") ? t.btn : t.card);
        swBtn.setForeground(mode.equals("Stopwatch") ? Color.WHITE : t.text);
        tmBtn.setBackground(mode.equals("Timer") ? t.btn : t.card);
        tmBtn.setForeground(mode.equals("Timer") ? Color.WHITE : t.text);

        timeDisplay.setForeground(t.accent);
        minLabel.setForeground(t.text);

        p15Btn.setBackground(t.card);
        p15Btn.setForeground(t.text);
        p25Btn.setBackground(t.card);
        p25Btn.setForeground(t.text);
        p45Btn.setBackground(t.card);
        p45Btn.setForeground(t.text);

        startBtn.setBackground(t.btn);
        startBtn.setForeground(Color.WHITE);
        resetBtn.setBackground(t.card);
        resetBtn.setForeground(t.text);

        themeSelector.setBackground(t.card);
        themeSelector.setForeground(t.text);

        mainContainer.repaint();
    }

    private void setPreset(int minutes) {
        minutesInput.setText(String.valueOf(minutes));
        resetTimer();
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
            try {
                double mins = Double.parseDouble(minutesInput.getText());
                long totalSecs = (long) (mins * 60);
                long m = totalSecs / 60;
                long s = totalSecs % 60;
                timeDisplay.setText(String.format("%02d:%02d", m, s));
            } catch (Exception ex) {
                timeDisplay.setText("25:00");
            }
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
                    playChimeSound();
                    JOptionPane.showMessageDialog(this, "Time's up! Great focus session! 💕", "Timer Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    long totalSecs = remainingMillis / 1000;
                    long mins = totalSecs / 60;
                    long secs = totalSecs % 60;
                    timeDisplay.setText(String.format("%02d:%02d", mins, secs));
                }
            }
        }
    }

    private void playChimeSound() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[8000];
                AudioFormat af = new AudioFormat(8000f, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                for (int i = 0; i < 2000; i++) {
                    double angle = i / (8000f / 880) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 80);
                }
                for (int i = 2000; i < 6000; i++) {
                    double angle = i / (8000f / 1046) * 2.0 * Math.PI;
                    buf[i] = (byte) (Math.sin(angle) * 80);
                }

                sdl.write(buf, 0, 6000);
                sdl.drain();
                sdl.close();
            } catch (Exception ignored) {}
        }).start();
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