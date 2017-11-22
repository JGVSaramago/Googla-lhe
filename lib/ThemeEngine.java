package Project;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class ThemeEngine extends UIManager {

    private static final Color DARK1 = new Color(77, 77, 77);
    private static final Color DARK2 = new Color(60, 63, 65);
    private static final Color DARK3 = new Color(43, 43, 43);
    private static final Color DARKTEXT = new Color(222,223,225);

    private static final Color LIGHT1 = new Color(214,217,223);
    private static final Color LIGHT2 = new Color(60, 63, 65);
    private static final Color LIGHT3 = new Color(255, 255, 255);
    private static final Color LIGHTTEXT = new Color(0,0,0);

    public ThemeEngine() {
        setNimbus();
        setLightTheme();
    }

    private static void setNimbus() {
        for (LookAndFeelInfo info : getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                try {
                    setLookAndFeel(info.getClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        put("ScrollBarUI", MyScrollbarUI.class.getName());
        put("ScrollBar.width", 7);

    }

    public static void setDarkTheme(){

        put("control", DARK2); //Window background
        put("text", DARKTEXT);
        put("nimbusBase", DARK3);
        put("nimbusFocus", DARK2);
        put("nimbusLightBackground", DARK3);

        //ScrollBar
        put("ScrollBar.track", DARK3);
    }

    public static void setLightTheme() {

        put("control", new Color(245, 246, 248)); //Window background
        put("text", LIGHTTEXT);
        put("nimbusBase", new Color(230, 230, 230));
        put("nimbusFocus", new Color(245, 246, 248));
        put("nimbusLightBackground", new Color(255, 255, 255));

        //ScrollBar
        put("ScrollBar.track", LIGHT3);
    }

    public static class MyScrollbarUI extends BasicScrollBarUI {

        public MyScrollbarUI() {
        }

        protected JButton createZeroButton() {
            JButton button = new JButton();
            Dimension zeroDim = new Dimension(0,0);
            button.setPreferredSize(zeroDim);
            button.setMinimumSize(zeroDim);
            button.setMaximumSize(zeroDim);
            return button;
        }

        @Override
        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            g.translate(thumbBounds.x, thumbBounds.y);
            g.setColor(new Color(130, 130 ,130));
            g.fillRect( 0, 0, thumbBounds.width - 2, thumbBounds.height - 1 );
            g.translate( -thumbBounds.x, -thumbBounds.y );
        }

        public static ComponentUI createUI(JComponent c){
            return new MyScrollbarUI();
        }
    }

}
