import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;

public class TimeTrackApp extends JFrame {

    // Theme Data Structure
    static class Theme {
        Color bg, card, text, btn, accent;
        Theme(String bg, String card, String text, String btn, String accent) {
            this.bg = Color.decode(bg);
            this.card = Color.decode(card);
            this.text = Color.decode(text);
            this.btn = Color.decode(btn);
            this.accent = Color.decode(accent);
        }
    }

    private final Map<String, Theme> themes = new HashMap<>();
    private String currentTheme = "Cyberpunk";

    // Application State
    private String mode = "Stopwatch"; // "Stopwatch" or "Timer"
    private boolean running = false;
    private long startTime = 0;
    private long elapsedTime = 0; // in milliseconds
    private long timerDuration = 0; // in milliseconds
    private Timer clockTimer;

    // Window Drag Coordinates
    private Point dragOffset;

    // UI Components
    private JPanel mainPanel, headerPanel, modePanel, cardPanel, inputPanel, ctrlPanel, footerPanel;
    private JLabel titleLabel, closeLabel, timeDisplay, minLabel;
    private JButton swBtn, tmBtn, startBtn, resetBtn;
    private JTextField minutesInput;
    private JComboBox<String> themeSelector;

    public TimeTrackApp() {
        // Window Configuration
        setUndecorated(true);              // Remove standard OS title bar
        setAlwaysOnTop(true);               // Keep sidebar always visible
        setSize(260, 340);
        
        // Position window near the top-right of the screen
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Rectangle screenBounds = ge.getMaximumWindowBounds();
        setLocation(screenBounds.width - 280, 100);

        initializeThemes();
        setupUI();
        setupDragAndDrop();
        applyTheme();

        // 100ms UI Update Interval
        clockTimer = new Timer(100, e -> updateClock());
        clockTimer.start();
    }

    private void initializeThemes() {
        themes.put("Cyberpunk", new Theme("#0f0f1b", "#1a1a2e", "#00fff5", "#ff007f", "#ffe600"));
        themes.put("Midnight",  new Theme("#121212", "#1e1e1e", "#ffffff", "#3700b3", "#bb86fc"));
        themes.put("Nord",      new Theme("#2e3440", "#3b4252", "#eceff4", "#5e81ac", "#88c0d0"));
        themes.put("Sunset",    new Theme("#2d132c", "#801336", "#fff0f5", "#c72c41", "#ee4540"));
        themes.put("Light",     new Theme("#f4f6f8", "#ffffff", "#1a1a2e", "#4a90e2", "#357abd"));
    }

    private void setupUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        setContentPane(mainPanel);

        // Header Panel
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setMaximumSize(new Dimension(260, 30));
        titleLabel = new JLabel(" ⏱ TimeTrack");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        closeLabel = new JLabel("✕ ");
        closeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        closeLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.exit(0);
            }
        });
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(closeLabel, BorderLayout.EAST);

        // Mode Switcher Panel
        modePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        swBtn = createFlatButton("Stopwatch");
        tmBtn = createFlatButton("Timer");

        swBtn.addActionListener(e -> switchMode("Stopwatch"));
        tmBtn.addActionListener(e -> switchMode("Timer"));
        modePanel.add(swBtn);
        modePanel.add(tmBtn);

        // Time Card Panel
        cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setPreferredSize(new Dimension(220, 80));
        cardPanel.setMaximumSize(new Dimension(220, 80));

        timeDisplay = new JLabel("00:00:00");
        timeDisplay.setFont(new Font("Consolas", Font.BOLD, 28));
        cardPanel.add(timeDisplay);

        // Timer Input Panel (Minutes)
        inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        minutesInput = new JTextField("25", 4);
        minutesInput.setHorizontalAlignment(JTextField.CENTER);
        minLabel = new JLabel("min");
        inputPanel.add(minutesInput);
        inputPanel.add(minLabel);
        inputPanel.setVisible(false);

        // Control Buttons Panel
        ctrlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        startBtn = createFlatButton("Start");
        resetBtn = createFlatButton("Reset");

        startBtn.addActionListener(e -> toggleStart());
        resetBtn.addActionListener(e -> resetTimer());
        ctrlPanel.add(startBtn);
        ctrlPanel.add(resetBtn);

        // Footer / Theme Dropdown Panel
        footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        String[] themeNames = themes.keySet().toArray(new String[0]);
        themeSelector = new JComboBox<>(themeNames);
        themeSelector.setSelectedItem(currentTheme);
        themeSelector.addActionListener(e -> {
            currentTheme = (String) themeSelector.getSelectedItem();
            applyTheme();
        });
        footerPanel.add(themeSelector);

        // Assemble Panels
        mainPanel.add(headerPanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(modePanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(cardPanel);
        mainPanel.add(inputPanel);
        mainPanel.add(Box.createVerticalStrut(5));
        mainPanel.add(ctrlPanel);
        mainPanel.add(Box.createGlue());
        mainPanel.add(footerPanel);
    }

    private JButton createFlatButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }

    private void applyTheme() {
        Theme t = themes.get(currentTheme);

        mainPanel.setBackground(t.bg);
        headerPanel.setBackground(t.bg);
        titleLabel.setForeground(t.text);
        closeLabel.setForeground(t.text);

        modePanel.setBackground(t.bg);
        swBtn.setBackground(mode.equals("Stopwatch") ? t.btn : t.card);
        swBtn.setForeground(t.text);
        tmBtn.setBackground(mode.equals("Timer") ? t.btn : t.card);
        tmBtn.setForeground(t.text);

        cardPanel.setBackground(t.card);
        timeDisplay.setForeground(t.accent);

        inputPanel.setBackground(t.bg);
        minLabel.setForeground(t.text);

        ctrlPanel.setBackground(t.bg);
        startBtn.setBackground(t.btn);
        startBtn.setForeground(t.text);
        resetBtn.setBackground(t.card);
        resetBtn.setForeground(t.text);

        footerPanel.setBackground(t.bg);

        repaint();
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