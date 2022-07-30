package spriteanimation;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author M S I
 */
public class Animation extends javax.swing.JFrame implements KeyListener {

    public void addImageToPanel(javax.swing.JPanel panel, String path, int imgWidth, int imgHeight,
            Point position, boolean isMiddle) throws IOException {
        JLabel picLabel = new JLabel();
        picLabel.setOpaque(true);
        picLabel.setIcon(new ImageIcon(getClass().getResource(path)));
        int pnlWidth = panel.getWidth();
        int pnlHeight = panel.getHeight();
        if (isMiddle) {
            picLabel.setBounds(pnlWidth / 2 - imgWidth / 2, pnlHeight / 2 - imgHeight / 2, imgWidth, imgHeight);
        } else {
            picLabel.setBounds((int) position.getX(), (int) position.getY(), imgWidth, imgHeight);
        }
        panel.add(picLabel);
        panel.setBackground(null);
        panel.repaint();
    }

    Thread thread;
    boolean newLoop;
    int speed = 15;

    private boolean moveable(Point position, JPanel panel, int imgWidth, int imgHeight) {
        int x = (int) position.getX();
        int y = (int) position.getY();
        return x >= 1 && y >= 1 && x + imgWidth <= panel.getWidth() - 1 && y + imgHeight <= panel.getHeight() - 1;
    }

    private static final int DOWN = 0;
    private static final int UP = 1;
    private static final int LEFT = 2;
    private static final int RIGHT = 3;
    private static final int STATIONARY = -1;
    int horizontalDirection;
    int verticalDirection;

    private void startThread() {
        thread = new Thread() {
            @Override
            public void run() {
                int animationStart = 0;
                Point position = new Point(1, 1);
                horizontalDirection = STATIONARY;
                verticalDirection = STATIONARY;
                repaintImage(position);
                Dimension size = resizePanelToParent();
                int i = 0;
                while (true) {
                    if (!size.equals(resizePanelToParent())) {
                        Dimension newSize = resizePanelToParent();
                        position = getPositionClosestToCurrent(position, size, newSize);
                        size = newSize;
                        repaintImage(animationStart, position);
                    }
                    animationStart = changeSpriteDirection(animationStart);
                    if (!(horizontalDirection == STATIONARY && verticalDirection == STATIONARY)) {
                        for (i = animationStart; i < animationStart + 3; i++) {

                            newLoop = false;
                            boolean skip = false;
                            repaintImage(i, position);

                            animationStart = changeSpriteDirection(animationStart);

                            position = changePosition(position);
                            if (skip) {
                                animationPane.removeAll();
                                break;
                            }
                            try {
                                Thread.currentThread().sleep(100);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(Animation.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            animationPane.removeAll();
                        }

                    }
                    
                }
            }

            private Point getPositionClosestToCurrent(Point oldPosition, Dimension oldSize, Dimension newSize) {
                return new Point(newSize.width / oldSize.width * (int) oldPosition.getX(),
                        newSize.height / oldSize.height * (int) oldPosition.getY());
            }

            private void repaintImage(int i, Point position) {
                try {
                    addImageToPanel(animationPane, "/Images/tile" + String.format("%03d", i) + "." + "png", 96, 96, position, false);
                } catch (IOException ex) {
                    Logger.getLogger(Animation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            private void repaintImage(Point position) {
                repaintImage(0, position);
            }

            private Point changePosition(Point position) {
                Point newPosition;
                int newX = (int) position.getX(), newY = (int) position.getY();
                newX = moveHorizontally(newX, speed);
                newPosition = new Point(newX, newY);
                if (moveable(newPosition, animationPane, 96, 96)) {
                    position = newPosition;
                }
                newY = moveVertically(newY, speed);
                newPosition = new Point(newX, newY);
                if (moveable(newPosition, animationPane, 96, 96)) {
                    position = newPosition;
                }
                return position;
            }

            private int moveVertically(int newY, int speed) {
                switch (verticalDirection) {
                    case UP ->
                        newY -= speed;
                    case DOWN ->
                        newY += speed;
                }
                return newY;
            }

            private int moveHorizontally(int newX, int speed) {
                switch (horizontalDirection) {
                    case LEFT ->
                        newX -= speed;
                    case RIGHT ->
                        newX += speed;
                }
                return newX;
            }

            private int changeSpriteDirection(int animationStart) {
                for (int j = 0; j < keys.size(); j++) {
                    switch (keys.get(j)) {
                        case KeyEvent.VK_S, KeyEvent.VK_DOWN -> {
                            if (verticalDirection == UP || horizontalDirection == STATIONARY) {
                                animationStart = 0;//DOWN
                            }
                            verticalDirection = DOWN;
                        }
                        case KeyEvent.VK_UP, KeyEvent.VK_W -> {
                            if (verticalDirection == DOWN || horizontalDirection == STATIONARY) {
                                animationStart = 9;//UP
                            }
                            verticalDirection = UP;
                        }
                        case KeyEvent.VK_LEFT, KeyEvent.VK_A -> {
                            if (horizontalDirection == RIGHT || verticalDirection == STATIONARY) {
                                animationStart = 3;//LEFT
                            }
                            horizontalDirection = LEFT;
                        }
                        case KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                            if (horizontalDirection == LEFT || verticalDirection == STATIONARY) {
                                animationStart = 6;//RIGHT
                            }
                            horizontalDirection = RIGHT;
                        }
                    }
                }
                return animationStart;
            }

        };
        thread.start();
    }

    private Dimension resizePanelToParent() {
        Dimension result = animationPane.getParent().getSize();
        animationPane.setSize(result);
        return result;
    }

    /**
     * Creates new form Animation
     */
    public Animation() {
        initComponents();
        this.setLocationRelativeTo(null);
        this.keys = new ArrayList<>();
        this.addKeyListener(this);
        try {
            java.awt.Image img = ImageIO.read(getClass().getResource("/Images/field.png"));
        } catch (Exception ex) {
            Logger.getLogger(Animation.class.getName()).log(Level.SEVERE, null, ex);
        }
        animationPane.repaint();
        startThread();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        animationPane = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jPanel1.setLayout(null);

        animationPane.setBackground(new java.awt.Color(255, 255, 255));
        animationPane.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        animationPane.setPreferredSize(new java.awt.Dimension(100, 100));
        animationPane.setLayout(null);
        jPanel1.add(animationPane);
        animationPane.setBounds(0, 0, 770, 530);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 766, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Animation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Animation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Animation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Animation.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Animation().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel animationPane;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
    public ArrayList<Integer> keys;

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent k) {
        if (!keys.contains(k.getKeyCode())) {
            keys.add(k.getKeyCode());
//            newLoop = true;
        }
    }

    public void setDirection(int horizontal, int vertical) {
        setHorizontalDirection(horizontal);
        setVerticalDirection(vertical);
    }

    public void setHorizontalDirection(int direction) {
        horizontalDirection = direction;
    }

    public void setVerticalDirection(int direction) {
        verticalDirection = direction;
    }

//    public boolean containsMovingKey(int keyCode){
//    }
    @Override
    public void keyReleased(KeyEvent k) {
        if (keys.contains(k.getKeyCode())) {
            keys.remove(keys.indexOf(k.getKeyCode()));
            switch (k.getKeyCode()) {
                case KeyEvent.VK_S, KeyEvent.VK_DOWN,KeyEvent.VK_UP, KeyEvent.VK_W -> {
                    setVerticalDirection(STATIONARY);
                }
                case KeyEvent.VK_LEFT, KeyEvent.VK_A,KeyEvent.VK_RIGHT, KeyEvent.VK_D -> {
                    setHorizontalDirection(STATIONARY);
                }
            }
        }
    }
}
