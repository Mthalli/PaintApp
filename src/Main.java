import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    private static DrawingPanel drawingPanel;


    public static void main(String[] args) {
        //Run GUI
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame("Example Application");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

            //Drawing Panel
            drawingPanel = new DrawingPanel();
            frame.add(drawingPanel, BorderLayout.CENTER);

            JMenuBar menuBar = new JMenuBar();

            // FILE Menu
            JMenu fileMenu = new JMenu("File");

            // File menu items
            JMenuItem newMenuItem = new JMenuItem("New");
            fileMenu.add(newMenuItem);


            fileMenu.addSeparator();
            JMenuItem saveasMenuItem = new JMenuItem("Save as");
            fileMenu.add(saveasMenuItem);
            saveasMenuItem.addActionListener(e -> drawingPanel.saveas()); // Save to file
            fileMenu.addSeparator();

            JMenuItem exitMenuItem = new JMenuItem("Exit");
            fileMenu.add(exitMenuItem);
            exitMenuItem.addActionListener(e -> System.exit(0)); // Exit

            menuBar.add(fileMenu);

            frame.setJMenuBar(menuBar);

            //  toolbar z funkcjami
            JToolBar toolBar = new JToolBar();

            // brush
            JButton brushButton = new JButton(" Square Brush");
            toolBar.add(brushButton);

            JButton brushroundButton = new JButton(" Round Brush");
            toolBar.add(brushroundButton);

            // Brush Click
            brushButton.addActionListener(e -> {
                drawingPanel.setTool("brush");
                drawingPanel.setBrushShape(false);
            });

            brushroundButton.addActionListener(e -> {
                drawingPanel.setTool("brush");
                drawingPanel.setBrushShape(true);
            });

            JButton eraserButton = new JButton("Eraser");
            toolBar.add(eraserButton);
            eraserButton.addActionListener(e -> drawingPanel.setTool("eraser"));//gumka

            // Color selection button
            JButton colorButton = new JButton("Select Color");
            toolBar.add(colorButton);

            // Brush size slider
            JSlider brushSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, 1);
            brushSizeSlider.setMajorTickSpacing(1);
            brushSizeSlider.setPaintTicks(true);
            brushSizeSlider.setPaintLabels(true);
            toolBar.add(new JLabel("Brush Size:"));
            toolBar.add(brushSizeSlider);

            // Rectangle
            JButton rectangleButton = new JButton("Rectangle");
            toolBar.add(rectangleButton);
            rectangleButton.addActionListener(e -> drawingPanel.setTool("rectangle"));

            //TEXT
            JButton textButton = new JButton("Text");
            toolBar.add(textButton);
            textButton.addActionListener(e -> drawingPanel.setTool("text"));

            // Font SIZE ComboBox
            Integer[] rozmiary = {12, 16, 20, 24, 28};
            JComboBox fontSizeComboBox = new JComboBox(rozmiary);
            fontSizeComboBox.setSelectedIndex(0); // Default font size
            toolBar.add(new JLabel("Font Size:"));
            toolBar.add(fontSizeComboBox);

            // Add toolbar
            frame.add(toolBar, BorderLayout.NORTH);



            // new
            newMenuItem.addActionListener(e -> drawingPanel.clearCanvas());


            // Color Chooser
            colorButton.addActionListener(e -> {
                Color selectedColor = JColorChooser.showDialog(null, "Choose a color", Color.BLACK);
                if (selectedColor != null) {
                    drawingPanel.setDrawingColor(selectedColor);
                }
            });

            // Change Brush Size
            brushSizeSlider.addChangeListener(e -> {
                int size = brushSizeSlider.getValue();
                drawingPanel.setBrushSize(size);
            });


            // Change Font Size
            fontSizeComboBox.addActionListener(e -> {
                int fontSize = (int) fontSizeComboBox.getSelectedItem();
                drawingPanel.setFontSize(fontSize);
            });

            frame.setVisible(true);
        });
    }
}

class DrawingPanel extends JPanel {
    private BufferedImage canvas;
    private Graphics2D g2d;
    private int prevX, prevY;
    private boolean erasing;
    private Color drawingColor = Color.BLACK; // Default color
    private int brushSize = 1; // Default brush size
    private String tool = "brush"; // Default tool
    private int startX, startY, endX, endY;
    private boolean drawingRectangle = false;
    private boolean roundBrush = false;
    private int fontSize = 12; // Default font size

    public DrawingPanel() {
        setDoubleBuffered(false);
        addMouseListener(new MouseAdapter() {

            public void mousePressed(MouseEvent e) {
                prevX = e.getX();
                prevY = e.getY();
                startX = prevX;
                startY = prevY;
                if (tool.equals("rectangle")) {
                    drawingRectangle = true;
                } else if (tool.equals("text")) {
                    String text = JOptionPane.showInputDialog("Enter text:");
                    if (text != null) {
                        g2d.setColor(drawingColor);
                        g2d.setFont(new Font("Arial", Font.PLAIN, fontSize)); // Adjust font size
                        g2d.drawString(text, prevX, prevY);
                        repaint();
                    }
                }
            }

            public void mouseReleased(MouseEvent e) {
                if (drawingRectangle) {
                    endX = e.getX();
                    endY = e.getY();
                    g2d.setColor(drawingColor);
                    g2d.setStroke(new BasicStroke(brushSize));
                    g2d.drawRect(Math.min(startX, endX), Math.min(startY, endY),
                            Math.abs(endX - startX), Math.abs(endY - startY));
                    drawingRectangle = false;
                    repaint();
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (g2d != null && !drawingRectangle) {
                    if (tool.equals("eraser")) {
                        g2d.setColor(Color.WHITE); // White for Eraser
                    } else {
                        g2d.setColor(drawingColor);
                    }
                    if (roundBrush) {
                        g2d.setStroke(new BasicStroke(brushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)); // Round brush stroke
                    } else {
                        g2d.setStroke(new BasicStroke(brushSize)); //Rectangle brush
                    }
                    g2d.drawLine(prevX, prevY, x, y);
                    repaint();
                    prevX = x;
                    prevY = y;
                }
            }
        });
    }

    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (canvas == null) {
            canvas = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
            g2d = canvas.createGraphics();
            clearCanvas();
        }
        g.drawImage(canvas, 0, 0, null);
    }

    public void clearCanvas() {
        g2d.setPaint(Color.white);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.setPaint(Color.black);
        repaint();
    }

    public void setTool(String tool) {
        this.tool = tool;
        if (tool.equals("eraser")) {
            erasing = true;
        } else {
            erasing = false;
        }
    }

    public void setDrawingColor(Color color) {
        this.drawingColor = color;
    }

    public void setBrushSize(int size) {
        this.brushSize = size;
    }

    public void setBrushShape(boolean round) {
        this.roundBrush = round;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public void saveas() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                ImageIO.write(canvas, "png", file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
