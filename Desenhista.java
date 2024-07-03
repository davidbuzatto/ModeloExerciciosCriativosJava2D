import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Classe que encapsula o processo de desenho.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public abstract class Desenhista extends JFrame {

    private PainelDesenho painelDesenho;
    private Graphics2D g2d;

    protected int larguraJanela;
    protected int alturaJanela;
    protected String tituloJanela;
    protected boolean ativarSuavizacao;

    public abstract void preparar();
    public abstract void processarEntrada();
    public abstract void desenhar();

    public Desenhista() {
        setDefaultCloseOperation( EXIT_ON_CLOSE );
    }

    public void iniciarComponentes() {

        setTitle( tituloJanela );
        
        painelDesenho = new PainelDesenho();
        painelDesenho.setPreferredSize( new Dimension( larguraJanela, alturaJanela ) );

        add( painelDesenho, BorderLayout.CENTER );
        pack();

        setLocationRelativeTo( null );
        setVisible( true );

    }

    private class PainelDesenho extends JPanel {

        @Override
        public void paintComponent( Graphics g ) {
            super.paintComponent( g );
            g2d = (Graphics2D) g.create();
            g2d.clearRect( 0, 0, getWidth(), getHeight() );
            if ( ativarSuavizacao ) {
                g2d.setRenderingHint( 
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON );
            }
            desenhar();
            g2d.dispose();
        }

    }

    public void setGraphics2D( Graphics2D g2d ) {
        this.g2d = g2d;
    }

    public void drawPixel( int posX, int posY, Color color ) {
        g2d.setColor( color );
        g2d.drawLine( posX, posY, posX, posY );
    }

    public void drawLine( int startPosX, int startPosY, int endPosX, int endPosY, Color color ) {
        g2d.setColor( color );
        g2d.drawLine( startPosX, startPosY, endPosX, endPosY );
    }

    public void drawRectangle( int posX, int posY, int width, int height, Color color ) {
        g2d.setColor( color );
        g2d.drawRect( posX, posY, width, height );
    }

    public void fillRectangle( int posX, int posY, int width, int height, Color color ) {
        g2d.setColor( color );
        g2d.fillRect( posX, posY, width, height );
    }

    public void drawCircle( int centerX, int centerY, int radius, Color color ) {
        g2d.setColor( color );
        g2d.drawOval( centerX - radius, centerY - radius, radius * 2, radius * 2 );
    }

    public void fillCircle( int centerX, int centerY, int radius, Color color ) {
        g2d.setColor( color );
        g2d.fillOval( centerX - radius, centerY - radius, radius * 2, radius * 2 );
    }

    public void drawEllipse( int centerX, int centerY, int radiusH, int radiusV, Color color ) {
        g2d.setColor( color );
        g2d.drawOval( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 );
    }

    public void fillEllipse( int centerX, int centerY, int radiusH, int radiusV, Color color ) {
        g2d.setColor( color );
        g2d.fillOval( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 );
    }

    public void drawText( String text, int posX, int posY, int fontSize, Color color ) {
        g2d.setColor( color );
        g2d.setFont( new Font( Font.MONOSPACED, Font.BOLD, fontSize ) );
        g2d.drawString( text, posX, posY );
    }

    public String textFormat( String text, Object... args  ) {
        return String.format( text, args );
    }

    public int getScreenWidth() {
        return painelDesenho.getWidth();
    }

    public int getScreenHeight() {
        return painelDesenho.getHeight();
    }

    public void fillRectangleGradientH( int posX, int posY, int width, int height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( posX, posY + height / 2, color1, posX + width, posY + height / 2, color2 ) );
        g2d.fillRect( posX, posY, width, height );
    }

    public void fillRectangleGradientV( int posX, int posY, int width, int height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( posX + width / 2, posY, color1, posX + width / 2, posY + height, color2 ) );
        g2d.fillRect( posX, posY, width, height );
    }

    public int measureText( String text, int fontSize ) {
        g2d.setFont( new Font( Font.MONOSPACED, Font.PLAIN, fontSize ) );
        return g2d.getFontMetrics().stringWidth( text );
    }

    protected static final Color LIGHTGRAY  = new Color( 200, 200, 200 );
    protected static final Color GRAY       = new Color( 130, 130, 130 );
    protected static final Color DARKGRAY   = new Color( 80, 80, 80 );
    protected static final Color YELLOW     = new Color( 253, 249, 0 );
    protected static final Color GOLD       = new Color( 255, 203, 0 );
    protected static final Color ORANGE     = new Color( 255, 161, 0 );
    protected static final Color PINK       = new Color( 255, 109, 194 );
    protected static final Color RED        = new Color( 230, 41, 55 );
    protected static final Color MAROON     = new Color( 190, 33, 55 );
    protected static final Color GREEN      = new Color( 0, 228, 48 );
    protected static final Color LIME       = new Color( 0, 158, 47 );
    protected static final Color DARKGREEN  = new Color( 0, 117, 44 );
    protected static final Color SKYBLUE    = new Color( 102, 191, 255 );
    protected static final Color BLUE       = new Color( 0, 121, 241 );
    protected static final Color DARKBLUE   = new Color( 0, 82, 172 );
    protected static final Color PURPLE     = new Color( 200, 122, 255 );
    protected static final Color VIOLET     = new Color( 135, 60, 190 );
    protected static final Color DARKPURPLE = new Color( 112, 31, 126 );
    protected static final Color BEIGE      = new Color( 211, 176, 131 );
    protected static final Color BROWN      = new Color( 127, 106, 79 );
    protected static final Color DARKBROWN  = new Color( 76, 63, 47 );
    protected static final Color WHITE      = new Color( 255, 255, 255 );
    protected static final Color BLACK      = new Color( 0, 0, 0 );
    protected static final Color MAGENTA    = new Color( 255, 0, 255 );

}