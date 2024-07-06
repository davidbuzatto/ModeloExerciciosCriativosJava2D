import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

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

    private boolean ativarSuavizacao;

    public abstract void processarEntrada();
    public abstract void desenhar();

    public abstract void tratarMouse( MouseEvent e, MouseEventType met );
    public abstract void tratarRodaRolagemMouse( MouseWheelEvent e );
    public abstract void tratarTeclado( KeyEvent e, KeyboardEventType ket );

    public Desenhista( int larguraJanela, int alturaJanela, String tituloJanela, boolean ativarSuavizacao ) {

        this.ativarSuavizacao = ativarSuavizacao;

        processarEntrada();

        setTitle( tituloJanela );
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        
        painelDesenho = new PainelDesenho();
        painelDesenho.setPreferredSize( new Dimension( larguraJanela, alturaJanela ) );
        painelDesenho.setFocusable( true );
        prepararEventosPainel( painelDesenho );

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

    protected class Point2D {
    
        final double x;
        final double y;
    
        public Point2D( double x, double y ) {
            this.x = x;
            this.y = y;
        }
    
        @Override
        public String toString() {
            return String.format( "[%.2f, %.2f]" , x, y );
        }
    
    }

    protected enum MouseEventType {

        CLICKED,
        PRESSED,
        RELEASED,
        ENTERED,
        EXITED,
        DRAGGED,
        MOVED
    
    }

    protected enum KeyboardEventType {
    
        PRESSED,
        RELEASED
    
    }

    public void drawPixel( double posX, double posY, Color color ) {
        g2d.setColor( color );
        g2d.draw( new Line2D.Double( posX, posY, posX, posY ) );
    }

    public void drawLine( double startPosX, double startPosY, double endPosX, double endPosY, Color color ) {
        g2d.setColor( color );
        g2d.draw( new Line2D.Double( startPosX, startPosY, endPosX, endPosY ) );
    }

    public void drawRectangleLines( double posX, double posY, double width, double height, Color color ) {
        g2d.setColor( color );
        g2d.draw( new Rectangle2D.Double( posX, posY, width, height ) );
    }

    public void drawRectangle( double posX, double posY, double width, double height, Color color ) {
        g2d.setColor( color );
        g2d.fill( new Rectangle2D.Double( posX, posY, width, height ) );
    }

    public void drawCircleLines( double centerX, double centerY, double radius, Color color ) {
        g2d.setColor( color );
        g2d.draw( new Ellipse2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2 ) );
    }

    public void drawCircle( double centerX, double centerY, double radius, Color color ) {
        g2d.setColor( color );
        g2d.fill( new Ellipse2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2 ) );
    }

    public void drawEllipseLines( double centerX, double centerY, double radiusH, double radiusV, Color color ) {
        g2d.setColor( color );
        g2d.draw( new Ellipse2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 ) );
    }

    public void drawEllipse( double centerX, double centerY, double radiusH, double radiusV, Color color ) {
        g2d.setColor( color );
        g2d.fill( new Ellipse2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 ) );
    }

    public void drawText( String text, double posX, double posY, int fontSize, Color color ) {
        g2d.setColor( color );
        g2d.setFont( new Font( Font.MONOSPACED, Font.BOLD, fontSize ) );
        g2d.drawString( text, (int) posX, (int) posY );
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

    public void drawRectangleGradientH( double posX, double posY, double width, double height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( (int) posX, (int) (posY + height / 2), color1, (int) (posX + width), (int) (posY + height / 2), color2 ) );
        g2d.fill( new Rectangle2D.Double( posX, posY, width, height ) );
    }

    public void drawRectangleGradientV( double posX, double posY, double width, double height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( (int) (posX + width / 2), (int) posY, color1, (int) (posX + width / 2), (int) (posY + height), color2 ) );
        g2d.fill( new Rectangle2D.Double( posX, posY, width, height ) );
    }

    public void clearBackground( Color color ) {
        drawRectangle( 0, 0, getScreenWidth(), getScreenHeight(), color );
    }

    public int measureText( String text, int fontSize ) {
        g2d.setFont( new Font( Font.MONOSPACED, Font.PLAIN, fontSize ) );
        return g2d.getFontMetrics().stringWidth( text );
    }

    public void drawCircleSectorLines( double centerX, double centerY, double radius, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.draw( new Arc2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, extent, Arc2D.PIE ) );
    }

    public void drawCircleSector( double centerX, double centerY, double radius, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.fill( new Arc2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, extent, Arc2D.PIE ) );
    }

    public void drawRingLines( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        processRing( centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, segments, color, true );
    }

    public void drawRing( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        processRing( centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, segments, color, false );
    }

    private void processRing( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color, boolean draw ) {

        g2d.setColor( color );

        Path2D path = new Path2D.Double();
        double currentAngle = -startAngle;
        double angleIncrement = Math.abs( endAngle - startAngle ) / segments;

        double rad = Math.toRadians( currentAngle );
        double x = centerX + innerRadius * Math.cos( rad );
        double y = centerY + innerRadius * Math.sin( rad );
        path.moveTo( x, y );

        for ( int i = 0; i < segments; i++ ) {

            currentAngle -= angleIncrement;

            rad = Math.toRadians( currentAngle );
            x = centerX + innerRadius * Math.cos( rad );
            y = centerY + innerRadius * Math.sin( rad );

            path.lineTo( x, y );

        }

        rad = Math.toRadians( currentAngle );
        x = centerX + outerRadius * Math.cos( rad );
        y = centerY + outerRadius * Math.sin( rad );
        path.lineTo( x, y );

        for ( int i = 0; i < segments; i++ ) {

            currentAngle += angleIncrement;

            rad = Math.toRadians( currentAngle );
            x = centerX + outerRadius * Math.cos( rad );
            y = centerY + outerRadius * Math.sin( rad );

            path.lineTo( x, y );

        }

        path.closePath();

        if ( draw ) {
            g2d.draw( path );
        } else {
            g2d.fill( path );
        }

    }

    public void drawRectanglePro( double posX, double posY, double width, double height, double originX, double originY, double rotation, Color color ) {

        Graphics2D gc = (Graphics2D) g2d.create();
        gc.setColor( color );

        gc.rotate( Math.toRadians( -rotation ), originX, originY );
        gc.fill( new Rectangle2D.Double( posX, posY, width, height ) );

        gc.dispose();

    }

    public void drawRectangleRoundedLines( double posX, double posY, double width, double height, double roundness, Color color ) {
        g2d.setColor( color );
        g2d.draw( new RoundRectangle2D.Double( posX, posY, width, height, roundness, roundness ) );
    }

    public void drawRectangleRounded( double posX, double posY, double width, double height, double roundness, Color color ) {
        g2d.setColor( color );
        g2d.fill( new RoundRectangle2D.Double( posX, posY, width, height, roundness, roundness ) );
    }

    public void drawTriangleLines( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color ) {
        processTriangle( v1x, v1y, v2x, v2y, v3x, v3y, color, true );
    }

    public void drawTriangle( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color ) {
        processTriangle( v1x, v1y, v2x, v2y, v3x, v3y, color, false );
    }

    private void processTriangle( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color, boolean draw ) {

        g2d.setColor( color );

        Path2D path = new Path2D.Double();
        path.moveTo( v1x, v1y );
        path.lineTo( v2x, v2y );
        path.lineTo( v3x, v3y );
        path.closePath();

        if ( draw ) {
            g2d.draw( path );
        } else {
            g2d.fill( path );
        }

    }

    public void drawPolyLines( double centerX, double centerY, double sides, double radius, double rotation, Color color ) {
        processPoly( centerX, centerY, sides, radius, rotation, color, true );
    }

    public void drawPoly( double centerX, double centerY, double sides, double radius, double rotation, Color color ) {
        processPoly( centerX, centerY, sides, radius, rotation, color, false );
    }

    private void processPoly( double centerX, double centerY, double sides, double radius, double rotation, Color color, boolean draw ) {

        g2d.setColor( color );

        Path2D path = new Path2D.Double();
        double currentAngle = -rotation;
        double angleIncrement = 360.0 / sides;

        for ( int i = 0; i < sides; i++ ) {

            double rad = Math.toRadians( currentAngle );
            double x = centerX + radius * Math.cos( rad );
            double y = centerY + radius * Math.sin( rad );

            if ( i == 0 ) {
                path.moveTo( x, y );
            } else {
                path.lineTo( x, y );
            }

            currentAngle -= angleIncrement;

        }

        path.closePath();

        if ( draw ) {
            g2d.draw( path );
        } else {
            g2d.fill( path );
        }

    }

    public void drawSplineSegmentLinear( double p1x, double p1y, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new Line2D.Double( p1x, p1y, p2x, p2y ) );
        g2d.dispose();
    }

    public Point2D getSplinePointLinear( double p1x, double p1y, double p2x, double p2y, double t ) {

        double x = p1x * ( 1.0f - t ) + p2x * t;
        double y = p1y * ( 1.0f - t ) + p2y * t;

        return new Point2D( x, y );
        
    }

    public void drawSplineSegmentBezierQuadratic( double p1x, double p1y, double c1x, double c1y, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new QuadCurve2D.Double( p1x, p1y, c1x, c1y, p2x, p2y ) );
        g2d.dispose();
    }

    public Point2D getSplinePointBezierQuad( double p1x, double p1y, double c1x, double c1y, double p2x, double p2y, double t ) {

        double a = Math.pow( 1.0 - t, 2 );
        double b = 2.0 * ( 1.0 - t ) * t;
        double c = Math.pow( t, 2 );

        double x = a * p1x + b * c1x + c * p2x;
        double y = a * p1y + b * c1y + c * p2y;

        return new Point2D( x, y );

    }

    public void drawSplineSegmentBezierCubic( double p1x, double p1y, double c1x, double c1y, double c2x, double c2y, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new CubicCurve2D.Double( p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y ) );
        g2d.dispose();
    }

    public Point2D getSplinePointBezierCubic( double p1x, double p1y, double c1x, double c1y, double c2x, double c2y, double p2x, double p2y, double t ) {

        double a = Math.pow( 1.0 - t, 3 );
        double b = 3.0 * Math.pow( 1.0 - t, 2 ) * t;
        double c = 3.0 * ( 1.0 - t ) * Math.pow( t, 2 );
        double d = Math.pow( t, 3 );

        double x = a * p1x + b * c1x + c * c2x + d * p2x;
        double y = a * p1y + b * c1y + c * c2y + d * p2y;

        return new Point2D( x, y );

    }

    public double lerp( double start, double end, double t ) {
        return start + ( end - start ) * t;
    }

    private void prepararEventosPainel( PainelDesenho painelDesenho ) {

        painelDesenho.addMouseListener( new MouseListener() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                tratarMouse( e, MouseEventType.CLICKED );
            }

            @Override
            public void mousePressed( MouseEvent e ) {
                tratarMouse( e, MouseEventType.PRESSED );
            }

            @Override
            public void mouseReleased( MouseEvent e ) {
                tratarMouse( e, MouseEventType.RELEASED );
            }

            @Override
            public void mouseEntered( MouseEvent e ) {
                tratarMouse( e, MouseEventType.ENTERED );
            }

            @Override
            public void mouseExited( MouseEvent e ) {
                tratarMouse( e, MouseEventType.EXITED );
            }
            
        });

        painelDesenho.addMouseMotionListener( new MouseMotionListener() {

            @Override
            public void mouseDragged( MouseEvent e ) {
                tratarMouse( e, MouseEventType.DRAGGED );
            }

            @Override
            public void mouseMoved( MouseEvent e ) {
                tratarMouse( e, MouseEventType.MOVED );
            }
            
        });

        painelDesenho.addMouseWheelListener( new MouseWheelListener() {

            @Override
            public void mouseWheelMoved( MouseWheelEvent e ) {
                tratarRodaRolagemMouse( e );
            }
            
        });

        painelDesenho.addKeyListener( new KeyAdapter() {

            @Override
            public void keyPressed( KeyEvent e ) {
                tratarTeclado( e, KeyboardEventType.PRESSED );
            }

            @Override
            public void keyReleased( KeyEvent e ) {
                tratarTeclado( e, KeyboardEventType.RELEASED );
            }
            
        });

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