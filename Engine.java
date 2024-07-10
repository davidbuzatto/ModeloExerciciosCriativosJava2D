import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Engine simples para criação de jogos ou simulações usando Java 2D.
 * Grande parte da sua API é baseada na engine de jogos Raylib (www.raylib.com).
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public abstract class Engine extends JFrame {

    /*
     * painel de desenho onde todas as operações de desenho e de registro
     * de eventos ocorretá.
     */
    private PainelDesenho painelDesenho;

    // referência ao contexto gráfico do painel de desenho.
    private Graphics2D g2d;

    // flag que sinaliza o uso da suavização (antialiasing) para o contexto gráfico
    private boolean suavizar;

    // tempo antes de iniciar os processos de atualização e desenho
    private long tempoAntes;

    // tempo depois de realizar os processos de atualização e desenho
    private long tempoDepois;

    // tempo de um frame
    private long tempoFrame;

    // tempo esperado baseado na quantidade de quadros por segundo
    private long tempoEsperadoFps;

    // quadros por segundo
    private int fps;

    // flag para controle de execução da thread de desenho
    private boolean executando;

    /**
     * Processa a entrada inicial fornecida pelo usuário e cria
     * e/ou inicializa os objetos/contextos/variáveis do jogo ou simulação.
     * 
     * É executado apenas UMA VEZ.
     */
    public abstract void criar();

    /**
     * Atualiza os objetos/contextos/variáveis do jogo ou simulação.
     * 
     * É executado uma vez a cada frame, sempre antes do método de desenho.
     */
    public abstract void atualizar();

    /**
     * Desenha o estado dos objetos/contextos/variáveis do jogo ou simulação.
     * 
     * É executado uma vez a cada frame, sempre após o método de atualização.
     */
    public abstract void desenhar();

    /**
     * Trata os eventos do mouse.
     * 
     * @param e Objeto que contém os dados do evento que foi capturado.
     * @param met Tipo de evento que foi capturado.
     */
    public void tratarMouse( MouseEvent e, MouseEventType met ) {
    }

    /**
     * Trata os eventos relacionaos a roda de rolagem do mouse.
     * 
     * @param e Objeto que contém os dados do evento que foi capturado.
     */
    public void tratarRodaRolagemMouse( MouseWheelEvent e ) {
    }

    /**
     * Trata os eventos relacionados ao teclado.
     * 
     * @param e Objeto que contém os dados do evento que foi capturado.
     * @param ket Tipo de evento que foi capturado.
     */
    public void tratarTeclado( KeyEvent e, KeyboardEventType ket ) {
    }

    /**
     * Cria uma instância da engine e inicia sua execução.
     * 
     * @param larguraJanela Largura da janela.
     * @param alturaJanela Altura da janela.
     * @param tituloJanela Título de janela.
     * @param suavizar Flag que indica se deve ou não usar suavização para o desenho no contexto gráfico.
     * @param fps A quantidade máxima de frames por segundo que se deseja que o processo de atualização e desenho mantenha.
     */
    public Engine( int larguraJanela, int alturaJanela, String tituloJanela, boolean suavizar, int fps ) {

        if ( larguraJanela <= 0 ) {
            throw new IllegalArgumentException( "largura precisa ser positiva!" );
        }

        if ( alturaJanela <= 0 ) {
            throw new IllegalArgumentException( "altura precisa ser positiva!" );
        }

        if ( fps <= 0 ) {
            throw new IllegalArgumentException( "fps precisa ser positivo!" );
        }

        this.suavizar = suavizar;
        this.executando = true;
        this.fps = fps;

        // cria e configura o painel de desenho
        painelDesenho = new PainelDesenho();
        painelDesenho.setPreferredSize( new Dimension( larguraJanela, alturaJanela ) );
        painelDesenho.setFocusable( true );
        prepararEventosPainel( painelDesenho );

        // configura a engine
        setTitle( tituloJanela );
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        add( painelDesenho, BorderLayout.CENTER );
        pack();
        setLocationRelativeTo( null );

        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosing( WindowEvent e ) {
                executando = false;
            }
        });

        // inicializa os objetos/contexto/variáveis do jogo atual
        criar();

        // inicia o processo de execução do jogo ou simulação
        tempoEsperadoFps = (long) ( 1000.0 / this.fps );  // quanto se espera que cada frame demore

        new Thread( new Runnable() {
            public void run() {
                
                // tempo a esperar antes de iniciar o próximo ciclo
                long tempoEsperar;

                while ( executando ) {

                    tempoAntes = System.currentTimeMillis();
                    atualizar();
                    painelDesenho.repaint();
                    tempoDepois = System.currentTimeMillis();

                    // quanto um frame demorou?
                    tempoFrame = tempoDepois - tempoAntes;

                    // quanto se deve esperar?
                    tempoEsperar = tempoEsperadoFps - tempoFrame;

                    // se o tempo a esperar for negativo, quer dizer que não
                    // houve tempo suficiente, baseado no tempo esperado
                    // para todo o frame ser atualizado e desenhado
                    if ( tempoEsperar < 0 ) {
                        tempoEsperar = 0;      // não espera
                    }

                    // se o tempo do frame é menor que o tempo se que deve esperar,
                    // quer dizer que sobrou tempo para executar o frame, ou seja
                    // o frame foi atualizado e desenhado em menos tempo do que 
                    // é esperado baseado na quantidade de frames por segundo
                    if ( tempoFrame < tempoEsperar ) {
                        tempoFrame = tempoEsperar;  // o tempo que o frame demorou para executar
                    }

                    try {
                        Thread.sleep( tempoEsperar );
                        painelDesenho.repaint();
                    } catch ( InterruptedException exc ) {
                        exc.printStackTrace();
                    }

                }
                
            }
        }).start();

        setVisible( true );

    }

    /**
     * Classe inerna que encapsula o processo de desenho.
     */
    private class PainelDesenho extends JPanel {

        @Override
        public void paintComponent( Graphics g ) {

            super.paintComponent( g );
            g2d = (Graphics2D) g.create();
            g2d.clearRect( 0, 0, getWidth(), getHeight() );

            if ( suavizar ) {
                g2d.setRenderingHint( 
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON );
            }

            desenhar();
            g2d.dispose();

        }

    }

    /**
     * Classe estática para representação de um ponto em duas dimensões.
     */
    public static class Point2D {
    
        double x;
        double y;
    
        public Point2D( double x, double y ) {
            this.x = x;
            this.y = y;
        }
    
        @Override
        public String toString() {
            return String.format( "Point2D[%.2f, %.2f]", x, y );
        }
    
    }

    /**
     * Classe estática para representação de uma linha em duas dimensões.
     */
    public static class Line2D {
    
        double x1;
        double y1;
        double x2;
        double y2;
    
        public Line2D( double x1, double y1, double x2, double y2 ) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }
    
        @Override
        public String toString() {
            return String.format( "Line2D[%.2f, %.2f, %.2f, %.2f]", x1, y1, x2, y2 );
        }
    
    }

    /**
     * Classe estática para representação de um triângulo em duas dimensões.
     */
    public static class Triangle2D {
    
        double x1;
        double y1;
        double x2;
        double y2;
        double x3;
        double y3;
    
        public Triangle2D( double x1, double y1, double x2, double y2, double x3, double y3 ) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.x3 = x3;
            this.y3 = y3;
        }
    
        @Override
        public String toString() {
            return String.format( "Triangle2D[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]", x1, y1, x2, y2, x3, y3 );
        }
    
    }

    /**
     * Classe estática para representação de um retângulo em duas dimensões.
     */
    public static class Rectangle2D {
    
        double x;
        double y;
        double width;
        double height;
    
        public Rectangle2D( double x, double y, double width, double height ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    
        @Override
        public String toString() {
            return String.format( "Rectangle2D[%.2f, %.2f, %.2f, %.2f]", x, y, width, height );
        }
    
    }

    /**
     * Classe estática para representação de um retângulo com cantos arrendondados em duas dimensões.
     */
    public static class RoundRectangle2D {
    
        double x;
        double y;
        double width;
        double height;
        double roundness;
    
        public RoundRectangle2D( double x, double y, double width, double height, double roundness ) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.roundness = roundness;
        }
    
        @Override
        public String toString() {
            return String.format( "RoundRectangle2D[%.2f, %.2f, %.2f, %.2f, %.2f]", x, y, width, height, roundness );
        }
    
    }

    /**
     * Classe estática para representação de um círculo em duas dimensões.
     */
    public static class Circle2D {
    
        double x;
        double y;
        double radius;
    
        public Circle2D( double x, double y, double radius ) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    
        @Override
        public String toString() {
            return String.format( "Circle2D[%.2f, %.2f, %.2f]", x, y, radius );
        }
    
    }

    /**
     * Classe estática para representação de uma elipse em duas dimensões.
     */
    public static class Ellipse2D {
        
        double x;
        double y;
        double radiusH;
        double radiusV;
    
        public Ellipse2D( double x, double y, double radiusH, double radiusV ) {
            this.x = x;
            this.y = y;
            this.radiusH = radiusH;
            this.radiusV = radiusV;
        }
    
        @Override
        public String toString() {
            return String.format( "Ellipse2D[%.2f, %.2f, %.2f, %.2f]", x, y, radiusH, radiusV );
        }
    
    }

    /**
     * Classe estática para representação de um arco em duas dimensões.
     */
    public static class Arc2D {
        
        double x;
        double y;
        double radiusH;
        double radiusV;
        double startAngle;
        double endAngle;

        public Arc2D( double x, double y, double radiusH, double radiusV, double startAngle, double endAngle ) {
            this.x = x;
            this.y = y;
            this.radiusH = radiusH;
            this.radiusV = radiusV;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
        }

        public Arc2D( double x, double y, double radius, double startAngle, double endAngle ) {
            this( x, y, radius, radius, startAngle, endAngle );
        }
    
        @Override
        public String toString() {
            return String.format( "Arc2D[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]", x, y, radiusH, radiusV, startAngle, endAngle );
        }
    
    }

    /**
     * Classe estática para representação de um anel em duas dimensões.
     */
    public static class Ring2D {
        
        double x;
        double y;
        double innerRadius;
        double outerRadius;
        double startAngle;
        double endAngle;
        int segments;
    
        public Ring2D( double x, double y, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments ) {
            this.x = x;
            this.y = y;
            this.innerRadius = innerRadius;
            this.outerRadius = outerRadius;
            this.startAngle = startAngle;
            this.endAngle = endAngle;
            this.segments = segments;
        }

        public Ring2D( double x, double y, double innerRadius, double outerRadius, double startAngle, double endAngle ) {
            this( x, y, innerRadius, outerRadius, startAngle, endAngle, 30 );
        }
    
        @Override
        public String toString() {
            return String.format( "Ring2D[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %d]", x, y, innerRadius, outerRadius, startAngle, endAngle, segments );
        }
    
    }

    /**
     * Classe estática para representação de um polígono regular em duas dimensões.
     */
    public static class Polygon2D {
    
        double x;
        double y;
        int sides;
        double radius;
        double rotation;
    
        public Polygon2D( double x, double y, int sides, double radius, double rotation ) {
            this.x = x;
            this.y = y;
            this.sides = sides;
            this.radius = radius;
            this.rotation = rotation;
        }

        public Polygon2D( double x, double y, int sides, double radius ) {
            this( x, y, sides, radius, 0.0 );
        }
    
        @Override
        public String toString() {
            return String.format( "Polygon2D[%.2f, %.2f, %d, %.2f, %.2f]", x, y, sides, radius, rotation );
        }
    
    }

    /**
     * Classe estática para representação de uma curva Bézier quadrática.
     */
    public static class QuadCurve2D {
        
        double x1;
        double y1;
        double cx;
        double cy;
        double x2;
        double y2;
    
        public QuadCurve2D( double x1, double y1, double cx, double cy, double x2, double y2 ) {
            this.x1 = x1;
            this.y1 = y1;
            this.cx = cx;
            this.cy = cy;
            this.x2 = x2;
            this.y2 = y2;
        }
    
        @Override
        public String toString() {
            return String.format( "QuadCurve2D[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]", x1, y1, cx, cy, x2, x2 );
        }
    
    }

    /**
     * Classe estática para representação de uma curva Bézier cúbica.
     */
    public static class CubicCurve2D {
        
        double x1;
        double y1;
        double c1x;
        double c1y;
        double c2x;
        double c2y;
        double x2;
        double y2;
    
        public CubicCurve2D( double x1, double y1, double c1x, double c1y, double c2x, double c2y, double x2, double y2 ) {
            this.x1 = x1;
            this.y1 = y1;
            this.c1x = c1x;
            this.c1y = c1y;
            this.c2x = c2x;
            this.c2y = c2y;
            this.x2 = x2;
            this.y2 = y2;
        }
    
        @Override
        public String toString() {
            return String.format( "CubicCurve2D[%.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f, %.2f]", x1, y1, c1x, c1y, c2x, c2y, x2, x2 );
        }
    
    }

    /**
     * Classe estática para representação de um caminho em duas dimensões.
     */
    public static class Path2D {
        
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();

        public void moveTo( double x, double y ) {
            path.moveTo( x, y );
        }

        public void lineTo( double x, double y ) {
            path.lineTo( x, y );
        }

        public void quadTo( double cx, double cy, double x, double y ) {
            path.quadTo( cx, cy, x, y );
        }

        public void cubicTo( double c1x, double c1y, double c2x, double c2y, double x, double y ) {
            path.curveTo( c1x, c1y, c2x, c2y, x, y );
        }

        public void closePath() {
            path.closePath();
        }
    
        @Override
        public String toString() {
            return path.toString();
        }
    
    }



    /**
     * Enumeração para representar os tipos de eventos do mouse.
     */
    public static enum MouseEventType {
        CLICKED,    // clicou (pressionar e soltar)
        PRESSED,    // pressionou
        RELEASED,   // soltou
        ENTERED,    // entrou
        EXITED,     // saiu
        DRAGGED,    // arrastou (botão pressionado + movimento)
        MOVED       // moveu
    }

    /**
     * Enumeração para representar os tipos de eventos do teclado.
     */
    public static enum KeyboardEventType {
        PRESSED,    // pressionou
        RELEASED    // soltou
    }

    /***************************************************************************
     * Métodos de desenho
     **************************************************************************/

    /**
     * Desenha um pixel.
     * 
     * @param posX coordenada x do pixel.
     * @param posY coordenada y do pixel.
     * @param color cor de desenho.
     */
    public void drawPixel( double posX, double posY, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.Line2D.Double( posX, posY, posX, posY ) );
    }

    /**
     * Desenha um pixel.
     * 
     * @param point ponto do pixel.
     * @param color cor de desenho.
     */
    public void drawPixel( Point2D point, Color color ) {
        drawPixel( point.x, point.y, color );
    }

    /**
     * Desenha uma linha.
     * 
     * @param startPosX coordenada x do ponto inicial.
     * @param startPosY coordenada y do ponto inicial.
     * @param endPosX coordenada x do ponto final.
     * @param endPosY coordenada y do ponto final.
     * @param color cor de desenho.
     */
    public void drawLine( double startPosX, double startPosY, double endPosX, double endPosY, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.Line2D.Double( startPosX, startPosY, endPosX, endPosY ) );
    }

    /**
     * Desenha uma linha.
     * 
     * @param startPoint ponto inicial.
     * @param endPoint ponto final.
     * @param color cor de desenho.
     */
    public void drawLine( Point2D startPoint, Point2D endPoint, Color color ) {
        drawLine( startPoint.x, startPoint.y, endPoint.x, endPoint.y, color );
    }

    /**
     * Desenha uma linha.
     * 
     * @param line uma linha.
     * @param color cor de desenho.
     */
    public void drawLine( Line2D line, Color color ) {
        drawLine( line.x1, line.y1, line.x2, line.y2, color );
    }

    /**
     * Desenha um retângulo.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param color cor de desenho.
     */
    public void drawRectangleLines( double posX, double posY, double width, double height, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.Rectangle2D.Double( posX, posY, width, height ) );
    }

    /**
     * Desenha um retângulo.
     * 
     * @param pos vértice superior esquerdo.
     * @param width largura.
     * @param height altura.
     * @param color cor de desenho.
     */
    public void drawRectangleLines( Point2D pos, double width, double height, Color color ) {
        drawRectangleLines( pos.x, pos.y, width, height, color );
    }

    /**
     * Desenha um retângulo.
     * 
     * @param rectangle um retângulo.
     * @param color cor de desenho.
     */
    public void drawRectangleLines( Rectangle2D rectangle, Color color ) {
        drawRectangleLines( rectangle.x, rectangle.y, rectangle.width, rectangle.height, color );
    }

    /**
     * Pinta um retângulo.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param color cor de desenho.
     */
    public void drawRectangle( double posX, double posY, double width, double height, Color color ) {
        g2d.setColor( color );
        g2d.fill( new java.awt.geom.Rectangle2D.Double( posX, posY, width, height ) );
    }

    /**
     * Pinta um retângulo.
     * 
     * @param pos vértice superior esquerdo.
     * @param width largura.
     * @param height altura.
     * @param color cor de desenho.
     */
    public void drawRectangle( Point2D pos, double width, double height, Color color ) {
        drawRectangle( pos.x, pos.y, width, height, color );
    }

    /**
     * Pinta um retângulo.
     * 
     * @param rectangle um retângulo.
     * @param color cor de desenho.
     */
    public void drawRectangle( Rectangle2D rectangle, Color color ) {
        drawRectangle( rectangle.x, rectangle.y, rectangle.width, rectangle.height, color );
    }

    /**
     * Pinta um retângulo rotacionado.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param originX coordenada x do pivô da rotação.
     * @param originY coordenada y do pivô da rotação.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawRectanglePro( double posX, double posY, double width, double height, double originX, double originY, double rotation, Color color ) {

        Graphics2D gc = (Graphics2D) g2d.create();
        gc.setColor( color );

        gc.rotate( Math.toRadians( -rotation ), originX, originY );
        gc.fill( new java.awt.geom.Rectangle2D.Double( posX, posY, width, height ) );

        gc.dispose();

    }

    /**
     * Pinta um retângulo rotacionado.
     * 
     * @param pos vértice superior esquerdo.
     * @param width largura.
     * @param height algura.
     * @param origin pivô da rotação.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawRectanglePro( Point2D pos, double width, double height, Point2D origin, double rotation, Color color ) {
        drawRectanglePro( pos.x, pos.y, width, height, origin.x, origin.y, rotation, color );
    }

    /**
     * Pinta um retângulo rotacionado.
     * 
     * @param rectangle um retângulo.
     * @param origin pivô da rotação.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawRectanglePro( Rectangle2D rectangle, Point2D origin, double rotation, Color color ) {
        drawRectanglePro( rectangle.x, rectangle.y, rectangle.width, rectangle.height, origin.x, origin.y, rotation, color );
    }

    /**
     * Desenha um retângulo com cantos arredondados.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param roundness arredondamento dos cantos.
     * @param color cor de desenho.
     */
    public void drawRectangleRoundedLines( double posX, double posY, double width, double height, double roundness, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.RoundRectangle2D.Double( posX, posY, width, height, roundness, roundness ) );
    }

    /**
     * Desenha um retângulo com cantos arredondados.
     * 
     * @param pos ponto superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param roundness arredondamento dos cantos.
     * @param color cor de desenho.
     */
    public void drawRectangleRoundedLines( Point2D pos, double width, double height, double roundness, Color color ) {
        drawRectangleRoundedLines( pos.x, pos.y, width, height, roundness, color );
    }

    /**
     * Desenha um retângulo com cantos arredondados.
     * 
     * @param roundRectangle um retângulo com os cantos arredondados.
     * @param color cor de desenho.
     */
    public void drawRectangleRoundedLines( RoundRectangle2D roundRectangle, Color color ) {
        drawRectangleRoundedLines( roundRectangle.x, roundRectangle.y, roundRectangle.width, roundRectangle.height, roundRectangle.roundness, color );
    }

    /**
     * Pinta um retângulo com cantos arredondados.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param roundness arredondamento dos cantos.
     * @param color cor de desenho.
     */
    public void drawRectangleRounded( double posX, double posY, double width, double height, double roundness, Color color ) {
        g2d.setColor( color );
        g2d.fill( new java.awt.geom.RoundRectangle2D.Double( posX, posY, width, height, roundness, roundness ) );
    }

    /**
     * Pinta um retângulo com cantos arredondados.
     * 
     * @param pos ponto superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param roundness arredondamento dos cantos.
     * @param color cor de desenho.
     */
    public void drawRectangleRounded( Point2D pos, double width, double height, double roundness, Color color ) {
        drawRectangleRounded( pos.x, pos.y, width, height, roundness, color );
    }

    /**
     * Pinta um retângulo com cantos arredondados.
     * 
     * @param roundRectangle um retângulo com os cantos arredondados.
     * @param color cor de desenho.
     */
    public void drawRectangleRounded( RoundRectangle2D roundRectangle, Color color ) {
        drawRectangleRounded( roundRectangle.x, roundRectangle.y, roundRectangle.width, roundRectangle.height, roundRectangle.roundness, color );
    }

    /**
     * Pinta um retângulo com um gradiente horizontal.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientH( double posX, double posY, double width, double height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( (int) posX, (int) (posY + height / 2), color1, (int) (posX + width), (int) (posY + height / 2), color2 ) );
        g2d.fill( new java.awt.geom.Rectangle2D.Double( posX, posY, width, height ) );
    }

    /**
     * Pinta um retângulo com um gradiente horizontal.
     * 
     * @param pos vértice superior esquerdo.
     * @param width largura.
     * @param height altura.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientH( Point2D pos, double width, double height, Color color1, Color color2 ) {
        drawRectangleGradientH( pos.x, pos.y, width, height, color1, color2 );
    }

    /**
     * Pinta um retângulo com um gradiente horizontal.
     * 
     * @param rectangle um retângulo.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientH( Rectangle2D rectangle, Color color1, Color color2 ) {
        drawRectangleGradientH( rectangle.x, rectangle.y, rectangle.width, rectangle.height, color1, color2 );
    }

    /**
     * Pinta um retângulo com um gradiente vertical.
     * 
     * @param posX coordenada x do vértice superior esquerdo do retângulo.
     * @param posY coordenada y do vértice superior esquerdo do retângulo.
     * @param width largura.
     * @param height algura.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientV( double posX, double posY, double width, double height, Color color1, Color color2 ) {
        g2d.setPaint( new GradientPaint( (int) (posX + width / 2), (int) posY, color1, (int) (posX + width / 2), (int) (posY + height), color2 ) );
        g2d.fill( new java.awt.geom.Rectangle2D.Double( posX, posY, width, height ) );
    }

    /**
     * Pinta um retângulo com um gradiente vertical.
     * 
     * @param pos vértice superior esquerdo.
     * @param width largura.
     * @param height altura.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientV( Point2D pos, double width, double height, Color color1, Color color2 ) {
        drawRectangleGradientV( pos.x, pos.y, width, height, color1, color2 );
    }

    /**
     * Pinta um retângulo com um gradiente vertical.
     * 
     * @param rectangle um retângulo.
     * @param color1 cor inicial do gradiente.
     * @param color2 cor final do gradiente.
     */
    public void drawRectangleGradientV( Rectangle2D rectangle, Color color1, Color color2 ) {
        drawRectangleGradientV( rectangle.x, rectangle.y, rectangle.width, rectangle.height, color1, color2 );
    }

    /**
     * Desenha um círculo.
     * 
     * @param centerX coordenada x do centro do círculo.
     * @param centerY coordenada y do centro do círculo.
     * @param radius raio.
     * @param color cor de desenho.
     */
    public void drawCircleLines( double centerX, double centerY, double radius, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.Ellipse2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2 ) );
    }

    /**
     * Desenha um círculo.
     * 
     * @param center centro do círculo.
     * @param radius raio.
     * @param color cor de desenho.
     */
    public void drawCircleLines( Point2D center, double radius, Color color ) {
        drawCircleLines( center.x, center.y, radius, color );
    }

    /**
     * Desenha um círculo.
     * 
     * @param circle um círculo.
     * @param color cor de desenho.
     */
    public void drawCircleLines( Circle2D circle, Color color ) {
        drawCircleLines( circle.x, circle.y, circle.radius, color );
    }

    /**
     * Pinta um círculo.
     * 
     * @param centerX coordenada x do centro do círculo.
     * @param centerY coordenada y do centro do círculo.
     * @param radius raio.
     * @param color cor de desenho.
     */
    public void drawCircle( double centerX, double centerY, double radius, Color color ) {
        g2d.setColor( color );
        g2d.fill( new java.awt.geom.Ellipse2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2 ) );
    }

    /**
     * Pinta um círculo.
     * 
     * @param center centro do círculo.
     * @param radius raio.
     * @param color cor de desenho.
     */
    public void drawCircle( Point2D center, double radius, Color color ) {
        drawCircle( center.x, center.y, radius, color );
    }

    /**
     * Pinta um círculo.
     * 
     * @param circle um círculo.
     * @param color cor de desenho.
     */
    public void drawCircle( Circle2D circle, Color color ) {
        drawCircle( circle.x, circle.y, circle.radius, color );
    }

    /**
     * Desenha uma elipse.
     * 
     * @param centerX coordenada x do centro da elipse.
     * @param centerY coordenada y do centro da elipse.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param color cor de desenho.
     */
    public void drawEllipseLines( double centerX, double centerY, double radiusH, double radiusV, Color color ) {
        g2d.setColor( color );
        g2d.draw( new java.awt.geom.Ellipse2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 ) );
    }

    /**
     * Desenha uma elipse.
     * 
     * @param center centro da elipse.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param color cor de desenho.
     */
    public void drawEllipseLines( Point2D center, double radiusH, double radiusV, Color color ) {
        drawEllipseLines( center.x, center.y, radiusH, radiusV, color );
    }

    /**
     * Desenha uma elipse.
     * 
     * @param ellipse uma elipse.
     * @param color cor de desenho.
     */
    public void drawEllipseLines( Ellipse2D ellipse, Color color ) {
        drawEllipseLines( ellipse.x, ellipse.y, ellipse.radiusH, ellipse.radiusV, color );
    }

    /**
     * Pinta uma elipse.
     * 
     * @param centerX coordenada x do centro da elipse.
     * @param centerY coordenada y do centro da elipse.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param color cor de desenho.
     */
    public void drawEllipse( double centerX, double centerY, double radiusH, double radiusV, Color color ) {
        g2d.setColor( color );
        g2d.fill( new java.awt.geom.Ellipse2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2 ) );
    }

    /**
     * Pinta uma elipse.
     * 
     * @param center centro da elipse.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param color cor de desenho.
     */
    public void drawEllipse( Point2D center, double radiusH, double radiusV, Color color ) {
        drawEllipse( center.x, center.y, radiusH, radiusV, color );
    }

    /**
     * Pinta uma elipse.
     * 
     * @param ellipse uma elipse.
     * @param color cor de desenho.
     */
    public void drawEllipse( Ellipse2D ellipse, Color color ) {
        drawEllipse( ellipse.x, ellipse.y, ellipse.radiusH, ellipse.radiusV, color );
    }

    /**
     * Desenha um setor circular.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param radius raio.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSectorLines( double centerX, double centerY, double radius, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.draw( new java.awt.geom.Arc2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, extent, java.awt.geom.Arc2D.PIE ) );
    }

    /**
     * Desenha um setor circular.
     * 
     * @param center ponto do centro.
     * @param radius raio.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSectorLines( Point2D center, double radius, double startAngle, double endAngle, Color color ) {
        drawCircleSectorLines( center.x, center.y, radius, startAngle, endAngle, color );
    }

    /**
     * Desenha um setor circular.
     * 
     * @param circle um circulo.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSectorLines( Circle2D circle, double startAngle, double endAngle, Color color ) {
        drawCircleSectorLines( circle.x, circle.y, circle.radius, startAngle, endAngle, color );
    }

    /**
     * pinta um setor circular.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param radius raio.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSector( double centerX, double centerY, double radius, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.fill( new java.awt.geom.Arc2D.Double( centerX - radius, centerY - radius, radius * 2, radius * 2, startAngle, extent, java.awt.geom.Arc2D.PIE ) );
    }

    /**
     * Pinta um setor circular.
     * 
     * @param center ponto do centro.
     * @param radius raio.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSector( Point2D center, double radius, double startAngle, double endAngle, Color color ) {
        drawCircleSector( center.x, center.y, radius, startAngle, endAngle, color );
    }

    /**
     * Pinta um setor circular.
     * 
     * @param circle um circulo.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawCircleSector( Circle2D circle, double startAngle, double endAngle, Color color ) {
        drawCircleSector( circle.x, circle.y, circle.radius, startAngle, endAngle, color );
    }

    /**
     * Desenha um setor de uma elipse.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSectorLines( double centerX, double centerY, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.draw( new java.awt.geom.Arc2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2, startAngle, extent, java.awt.geom.Arc2D.PIE ) );
    }

    /**
     * Desenha um setor de uma elipse.
     * 
     * @param center ponto do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSectorLines( Point2D center, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        drawEllipseSectorLines( center.x, center.y, radiusH, radiusV, startAngle, endAngle, color );
    }

    /**
     * Desenha um setor de uma elipse.
     * 
     * @param ellipse uma elipse.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSectorLines( Ellipse2D ellipse, double startAngle, double endAngle, Color color ) {
        drawEllipseSectorLines( ellipse.x, ellipse.y, ellipse.radiusH, ellipse.radiusV, startAngle, endAngle, color );
    }

    /**
     * Pinta um setor de uma elipse.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSector( double centerX, double centerY, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.fill( new java.awt.geom.Arc2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2, startAngle, extent, java.awt.geom.Arc2D.PIE ) );
    }

    /**
     * Pinta um setor de uma elipse.
     * 
     * @param center ponto do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSector( Point2D center, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        drawEllipseSector( center.x, center.y, radiusH, radiusV, startAngle, endAngle, color );
    }

    /**
     * Pinta um setor de uma elipse.
     * 
     * @param ellipse uma elipse.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawEllipseSector( Ellipse2D ellipse, double startAngle, double endAngle, Color color ) {
        drawEllipseSector( ellipse.x, ellipse.y, ellipse.radiusH, ellipse.radiusV, startAngle, endAngle, color );
    }

    /**
     * Desenha um arco.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawArcLines( double centerX, double centerY, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        g2d.setColor( color );
        double extent = endAngle - startAngle;
        g2d.draw( new java.awt.geom.Arc2D.Double( centerX - radiusH, centerY - radiusV, radiusH * 2, radiusV * 2, startAngle, extent, java.awt.geom.Arc2D.CHORD ) );
    }

    /**
     * Desenha um arco.
     * 
     * @param center ponto do centro.
     * @param radiusH raio horizontal.
     * @param radiusV raio vertical.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawArcLines( Point2D center, double radiusH, double radiusV, double startAngle, double endAngle, Color color ) {
        drawArcLines( center.x, center.y, radiusH, radiusV, startAngle, endAngle, color );
    }

    /**
     * Desenha um arco
     * 
     * @param arc um arco.
     * @param startAngle ângulo inicial.
     * @param endAngle ângulo final.
     * @param color cor de desenho.
     */
    public void drawArcLines( Arc2D arc, double startAngle, double endAngle, Color color ) {
        drawArcLines( arc.x, arc.y, arc.radiusH, arc.radiusV, arc.startAngle, arc.endAngle, color );
    }

    /**
     * Desenha um anel.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param innerRadius raio interno.
     * @param outerRadius raio externo.
     * @param startAngle ângulo inicial em graus.
     * @param endAngle ângulo final em graus.
     * @param segments quantidade de segmentos.
     * @param color cor de desenho.
     */
    public void drawRingLines( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        processRing( centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, segments, color, true );
    }

    /**
     * Desenha um anel.
     * 
     * @param center centro do anel.
     * @param innerRadius raio interno.
     * @param outerRadius raio externo.
     * @param startAngle ângulo inicial em graus.
     * @param endAngle ângulo final em graus.
     * @param segments quantidade de segmentos.
     * @param color cor de desenho.
     */
    public void drawRingLines( Point2D center, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        drawRingLines( center.x, center.y, innerRadius, outerRadius, startAngle, endAngle, segments, color );
    }

    /**
     * Desenha um anel.
     * 
     * @param ring um anel.
     * @param color cor de desenho.
     */
    public void drawRingLines( Ring2D ring, Color color ) {
        drawRingLines( ring.x, ring.y, ring.innerRadius, ring.outerRadius, ring.startAngle, ring.endAngle, ring.segments, color );
    }

    /**
     * Pinta um anel.
     * 
     * @param centerX coordenada x do centro.
     * @param centerY coordenada y do centro.
     * @param innerRadius raio interno.
     * @param outerRadius raio externo.
     * @param startAngle ângulo inicial em graus.
     * @param endAngle ângulo final em graus.
     * @param segments quantidade de segmentos.
     * @param color cor de desenho.
     */
    public void drawRing( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        processRing( centerX, centerY, innerRadius, outerRadius, startAngle, endAngle, segments, color, false );
    }

    /**
     * Pinta um anel.
     * 
     * @param center centro do anel.
     * @param innerRadius raio interno.
     * @param outerRadius raio externo.
     * @param startAngle ângulo inicial em graus.
     * @param endAngle ângulo final em graus.
     * @param segments quantidade de segmentos.
     * @param color cor de desenho.
     */
    public void drawRing( Point2D center, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color ) {
        drawRing( center.x, center.y, innerRadius, outerRadius, startAngle, endAngle, segments, color );
    }

    /**
     * Pinta um anel.
     * 
     * @param ring um anel.
     * @param color cor de desenho.
     */
    public void drawRing( Ring2D ring, Color color ) {
        drawRing( ring.x, ring.y, ring.innerRadius, ring.outerRadius, ring.startAngle, ring.endAngle, ring.segments, color );
    }

    /*
     * Gera o desenho do anel.
     */
    private void processRing( double centerX, double centerY, double innerRadius, double outerRadius, double startAngle, double endAngle, int segments, Color color, boolean draw ) {

        g2d.setColor( color );

        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
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

    /**
     * Desenha um triângulo.
     * 
     * @param v1x coordenada x do primeiro vértice.
     * @param v1y coordenada y do primeiro vértice.
     * @param v2x coordenada x do segundo vértice.
     * @param v2y coordenada y do segundo vértice.
     * @param v3x coordenada x do terceiro vértice.
     * @param v3y coordenada y do terceiro vértice.
     * @param color cor de desenho.
     */
    public void drawTriangleLines( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color ) {
        processTriangle( v1x, v1y, v2x, v2y, v3x, v3y, color, true );
    }

    /**
     * Desenha um triângulo.
     * 
     * @param v1 primeiro vértice.
     * @param v2 segundo vértice.
     * @param v3 terceiro vértice.
     * @param color cor de desenho.
     */
    public void drawTriangleLines( Point2D v1, Point2D v2, Point2D v3, Color color ) {
        drawTriangleLines( v1.x, v1.y, v2.x, v2.y, v3.x, v3.y, color );
    }

    /**
     * Desenha um triângulo.
     * 
     * @param triangle um triângulo.
     * @param color cor de desenho.
     */
    public void drawTriangleLines( Triangle2D triangle, Color color ) {
        drawTriangleLines( triangle.x1, triangle.y1, triangle.x2, triangle.y2, triangle.x3, triangle.y3, color );
    }

    /**
     * Pinta um triângulo.
     * 
     * @param v1x coordenada x do primeiro vértice.
     * @param v1y coordenada y do primeiro vértice.
     * @param v2x coordenada x do segundo vértice.
     * @param v2y coordenada y do segundo vértice.
     * @param v3x coordenada x do terceiro vértice.
     * @param v3y coordenada y do terceiro vértice.
     * @param color cor de desenho.
     */
    public void drawTriangle( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color ) {
        processTriangle( v1x, v1y, v2x, v2y, v3x, v3y, color, false );
    }

    /**
     * Pinta um triângulo.
     * 
     * @param v1 primeiro vértice.
     * @param v2 segundo vértice.
     * @param v3 terceiro vértice.
     * @param color cor de desenho.
     */
    public void drawTriangle( Point2D v1, Point2D v2, Point2D v3, Color color ) {
        drawTriangle( v1.x, v1.y, v2.x, v2.y, v3.x, v3.y, color );
    }

    /**
     * Pinta um triângulo.
     * 
     * @param triangle um triângulo.
     * @param color cor de desenho.
     */
    public void drawTriangles( Triangle2D triangle, Color color ) {
        drawTriangle( triangle.x1, triangle.y1, triangle.x2, triangle.y2, triangle.x3, triangle.y3, color );
    }

    /*
     * Gera o desenho do triângulo.
     */
    private void processTriangle( double v1x, double v1y, double v2x, double v2y, double v3x, double v3y, Color color, boolean draw ) {

        g2d.setColor( color );

        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
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

    /**
     * Desenha um polígono regular.
     * 
     * @param centerX coordenada x do centro do polígono.
     * @param centerY coordenada y do centro do polígono.
     * @param sides quantidade de lados.
     * @param radius raio.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawPolyLines( double centerX, double centerY, double sides, double radius, double rotation, Color color ) {
        processPoly( centerX, centerY, sides, radius, rotation, color, true );
    }

    /**
     * Desenha um polígono regular.
     * 
     * @param center centro do polígono.
     * @param sides quantidade de lados.
     * @param radius raio.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawPolyLines( Point2D center, double sides, double radius, double rotation, Color color ) {
        drawPolyLines( center.x, center.y, sides, radius, rotation, color );
    }

    /**
     * Desenha um polígono regular.
     * 
     * @param plygon um polígono regular.
     * @param color cor de desenho.
     */
    public void drawPolyLines( Polygon2D polygon, Color color ) {
        drawPolyLines( polygon.x, polygon.y, polygon.sides, polygon.radius, polygon.rotation, color );
    }

    /**
     * Pinta um polígono regular.
     * 
     * @param centerX coordenada x do centro do polígono.
     * @param centerY coordenada y do centro do polígono.
     * @param sides quantidade de lados.
     * @param radius raio.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawPoly( double centerX, double centerY, double sides, double radius, double rotation, Color color ) {
        processPoly( centerX, centerY, sides, radius, rotation, color, false );
    }

    /**
     * Pinta um polígono regular.
     * 
     * @param center centro do polígono.
     * @param sides quantidade de lados.
     * @param radius raio.
     * @param rotation rotação em graus.
     * @param color cor de desenho.
     */
    public void drawPoly( Point2D center, double sides, double radius, double rotation, Color color ) {
        drawPoly( center.x, center.y, sides, radius, rotation, color );
    }

    /**
     * Pinta um polígono regular.
     * 
     * @param polygon um polígono regular.
     * @param color cor de desenho.
     */
    public void drawPoly( Polygon2D polygon, Color color ) {
        drawPoly( polygon.x, polygon.y, polygon.sides, polygon.radius, polygon.rotation, color );
    }

    /*
     * Gera o desenho do polígono regular.
     */
    private void processPoly( double centerX, double centerY, double sides, double radius, double rotation, Color color, boolean draw ) {

        g2d.setColor( color );

        java.awt.geom.Path2D path = new java.awt.geom.Path2D.Double();
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

    /**
     * Desenha um caminho.
     * 
     * @param path caminho a ser desenhado.
     * @param color cor de desenho.
     */
    public void drawPathLines( Path2D path, Color color ) {
        g2d.setColor( color );
        g2d.draw( path.path );
    }

    /**
     * Ponta um caminho.
     * 
     * @param path caminho a ser desenhado.
     * @param color cor de desenho.
     */
    public void drawPath( Path2D path, Color color ) {
        g2d.setColor( color );
        g2d.fill( path.path );
    }

    /***************************************************************************
     * Métodos de desenhos de splines.
     **************************************************************************/

    /**
     * Desenha uma spline linear.
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentLinear( double p1x, double p1y, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new java.awt.geom.Line2D.Double( p1x, p1y, p2x, p2y ) );
        g2d.dispose();
    }

    /**
     * Desenha uma spline linear.
     * 
     * @param p1 ponto inicial.
     * @param p2 ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentLinear( Point2D p1, Point2D p2, double thick, Color color ) {
        drawSplineSegmentLinear( p1.x, p1.y, p2.x, p2.y, thick, color );
    }

    /**
     * Desenha uma spline linear.
     * 
     * @param line uma linha.
     * @param p2 ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentLinear( Line2D line, double thick, Color color ) {
        drawSplineSegmentLinear( line.x1, line.y1, line.x2, line.y2, thick, color );
    }

    /**
     * Obtém um ponto dentro de uma spline linear.
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointLinear( double p1x, double p1y, double p2x, double p2y, double t ) {

        double x = p1x * ( 1.0f - t ) + p2x * t;
        double y = p1y * ( 1.0f - t ) + p2y * t;

        return new Point2D( x, y );
        
    }

    /**
     * Obtém um ponto dentro de uma spline linear.
     * 
     * @param p1 ponto final.
     * @param p2 ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointLinear( Point2D p1, Point2D p2, double t ) {
        return getSplinePointLinear( p1.x, p1.y, p2.x, p2.y, t )        ;
    }

    /**
     * Obtém um ponto dentro de uma spline linear.
     * 
     * @param line uma linha.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointLinear( Line2D line, double t ) {
        return getSplinePointLinear( line.x1, line.y1, line.x2, line.y2, t );
    }

    /**
     * Desenha uma spline quadrática (curva Bézier quadrática).
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param cx coordenada x do ponto de controle.
     * @param cy coordenada y do ponto de controle.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierQuadratic( double p1x, double p1y, double cx, double cy, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new java.awt.geom.QuadCurve2D.Double( p1x, p1y, cx, cy, p2x, p2y ) );
        g2d.dispose();
    }

    /**
     * Desenha uma spline quadrática (curva Bézier quadrática).
     * 
     * @param p1 ponto inicial.
     * @param c ponto de controle.
     * @param p2 ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierQuadratic( Point2D p1, Point2D c, Point2D p2, double thick, Color color ) {
        drawSplineSegmentBezierQuadratic( p1.x, p1.y, c.x, c.y, p2.x, p2.y, thick, color );
    }

    /**
     * Desenha uma spline quadrática (curva Bézier quadrática).
     * 
     * @param quadCurve uma curva Bézier quadrática.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierQuadratic( QuadCurve2D quadCurve, double thick, Color color ) {
        drawSplineSegmentBezierQuadratic( quadCurve.x1, quadCurve.y1, quadCurve.cx, quadCurve.cy, quadCurve.x2, quadCurve.y2, thick, color );
    }

    /**
     * Obtém um ponto dentro de uma spline quadrática (curva Bézier quadrática).
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param cx coordenada x do ponto de controle.
     * @param cy coordenada y do ponto de controle.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierQuad( double p1x, double p1y, double cx, double cy, double p2x, double p2y, double t ) {

        double a = Math.pow( 1.0 - t, 2 );
        double b = 2.0 * ( 1.0 - t ) * t;
        double c = Math.pow( t, 2 );

        double x = a * p1x + b * cx + c * p2x;
        double y = a * p1y + b * cy + c * p2y;

        return new Point2D( x, y );

    }

    /**
     * Obtém um ponto dentro de uma spline quadrática (curva Bézier quadrática).
     * 
     * @param p1 ponto inicial.
     * @param c ponto de controle.
     * @param p2 ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierQuad( Point2D p1, Point2D c, Point2D p2, double t ) {
        return getSplinePointBezierQuad( p1.x, p1.y, c.x, c.y, p2.x, p2.y, t );
    }
    
    /**
     * Obtém um ponto dentro de uma spline quadrática (curva Bézier quadrática).
     * 
     * @param quadCurve uma curva Bézier quadrática.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierQuad( QuadCurve2D quadCurve, double t ) {
        return getSplinePointBezierQuad( quadCurve.x1, quadCurve.y1, quadCurve.cx, quadCurve.cy, quadCurve.x2, quadCurve.y2, t );
    }

    /**
     * Desenha uma spline cúbica (curva Bézier cúbica).
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param c1x coordenada x do primeiro ponto de controle.
     * @param c1y coordenada y do primeiro ponto de controle.
     * @param c2x coordenada x do segundo ponto de controle.
     * @param c2y coordenada y do segundo ponto de controle.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierCubic( double p1x, double p1y, double c1x, double c1y, double c2x, double c2y, double p2x, double p2y, double thick, Color color ) {
        this.g2d.setColor( color );
        Graphics2D g2d = (Graphics2D) this.g2d.create();
        g2d.setStroke( new BasicStroke( (float) thick, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) );
        g2d.draw( new java.awt.geom.CubicCurve2D.Double( p1x, p1y, c1x, c1y, c2x, c2y, p2x, p2y ) );
        g2d.dispose();
    }

    /**
     * Desenha uma spline cúbica (curva Bézier cúbica).
     * 
     * @param p1 ponto inicial.
     * @param c1 primeiro ponto de controle.
     * @param c2 segundo ponto de controle.
     * @param p2 ponto final.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierCubic( Point2D p1, Point2D c1, Point2D c2, Point2D p2, double thick, Color color ) {
        drawSplineSegmentBezierCubic( p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y, thick, color );
    }

    /**
     * Desenha uma spline cúbica (curva Bézier cúbica).
     * 
     * @param cubicCurve uma curva Bézier cúbica.
     * @param thick grossura da spline.
     * @param color cor de desenhho.
     */
    public void drawSplineSegmentBezierCubic( CubicCurve2D cubicCurve, double thick, Color color ) {
        drawSplineSegmentBezierCubic( cubicCurve.x1, cubicCurve.y1, cubicCurve.c1x, cubicCurve.c1y, cubicCurve.c2x, cubicCurve.c2y, cubicCurve.x2, cubicCurve.y2, thick, color );
    }

    /**
     * Obtém um ponto dentro de uma spline cúbica (curva Bézier cúbica).
     * 
     * @param p1x coordenada x do ponto inicial.
     * @param p1y coordenada y do ponto inicial.
     * @param c1x coordenada x do primeiro ponto de controle.
     * @param c1y coordenada y do primeiro ponto de controle.
     * @param c2x coordenada x do segundo ponto de controle.
     * @param c2y coordenada y do segundo ponto de controle.
     * @param p2x coordenada x do ponto final.
     * @param p2y coordenada y do ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierCubic( double p1x, double p1y, double c1x, double c1y, double c2x, double c2y, double p2x, double p2y, double t ) {

        double a = Math.pow( 1.0 - t, 3 );
        double b = 3.0 * Math.pow( 1.0 - t, 2 ) * t;
        double c = 3.0 * ( 1.0 - t ) * Math.pow( t, 2 );
        double d = Math.pow( t, 3 );

        double x = a * p1x + b * c1x + c * c2x + d * p2x;
        double y = a * p1y + b * c1y + c * c2y + d * p2y;

        return new Point2D( x, y );

    }

    /**
     * Obtém um ponto dentro de uma spline cúbica (curva Bézier cúbica).
     * 
     * @param p1 ponto inicial.
     * @param c1 primeiro ponto de controle.
     * @param c2 segundo ponto de controle.
     * @param p2 ponto final.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierCubic( Point2D p1, Point2D c1, Point2D c2, Point2D p2, double t ) {
        return getSplinePointBezierCubic( p1.x, p1.y, c1.x, c1.y, c2.x, c2.y, p2.x, p2.y, t );
    }

    /**
     * Obtém um ponto dentro de uma spline cúbica (curva Bézier cúbica).
     * 
     * @param cubicCurve uma curva Bézier cúbica.
     * @param t Um valor de 0 a 1 que representa a posição, em porcentagem, do ponto desejado.
     * @return O ponto dentro da spline.
     */
    public Point2D getSplinePointBezierCubic( CubicCurve2D cubicCurve, double t ) {
        return getSplinePointBezierCubic( cubicCurve.x1, cubicCurve.y1, cubicCurve.c1x, cubicCurve.c1y, cubicCurve.c2x, cubicCurve.c2y, cubicCurve.x2, cubicCurve.y2, t );
    }



    /***************************************************************************
     * Métodos de desenho de texto.
     **************************************************************************/

    /**
     * Desenha um texto.
     * 
     * @param text o texto a ser desenhado.
     * @param posX coordenada x do início do desenho do texto.
     * @param posY coordenada y do início do desenho do texto.
     * @param fontSize o tamanho da fonte.
     * @param color cor de desenho.
     */
    public void drawText( String text, double posX, double posY, int fontSize, Color color ) {
        g2d.setColor( color );
        g2d.setFont( new Font( Font.MONOSPACED, Font.BOLD, fontSize ) );
        g2d.drawString( text, (int) posX, (int) posY );
    }

    /**
     * Desenha um texto.
     * 
     * @param text o texto a ser desenhado.
     * @param point ponto do inicio do desenho do texto.
     * @param fontSize o tamanho da fonte.
     * @param color cor de desenho.
     */
    public void drawText( String text, Point2D point, int fontSize, Color color ) {
        drawText( text, point.x, point.y, fontSize, color );
    }

    /**
     * Mede a largura de um texto.
     * 
     * @param text o texto a ser medido.
     * @param fontSize o tamanho da fonte.
     * @return a largura de um texto.
     */
    public int measureText( String text, int fontSize ) {
        g2d.setFont( new Font( Font.MONOSPACED, Font.PLAIN, fontSize ) );
        return g2d.getFontMetrics().stringWidth( text );
    }

    /**
     * Formata um texto, da mesma forma que System.out.printf ou String.format.
     * 
     * @param text Um padrão que será formatado.
     * @param args Uma série de argumentos que serão inseridos no padrão.
     * @return o texto formatado.
     */
    public String textFormat( String text, Object... args  ) {
        return String.format( text, args );
    }



    /***************************************************************************
     * Métodos utilitários.
     **************************************************************************/

    /**
     * Realiza a interpolação linear entre dois valores.
     * 
     * @param start valor inicial.
     * @param end valor final.
     * @param t quantidade (0 a 1)
     * @return A interpolação linear entre dois valores.
     */
    public double lerp( double start, double end, double t ) {
        return start + ( end - start ) * t;
    }

    /**
     * Realiza a interpolação linear entre dois pontos.
     * 
     * @param start ponto inicial.
     * @param end ponto final.
     * @param t quantidade (0 a 1)
     * @return Um ponto que representa a interpolação linear entre dois pontos.
     */
    public Point2D lerp( Point2D start, Point2D end, double t ) {
        double x = start.x + ( end.x - start.x ) * t;
        double y = start.y + ( end.y - start.y ) * t;
        return new Point2D( x, y );
    }

    /**
     * Limpa o fundo da tela de desenho.
     * 
     * @param color cor a ser usada.
     */
    public void clearBackground( Color color ) {
        drawRectangle( 0, 0, getScreenWidth(), getScreenHeight(), color );
    }



    /***************************************************************************
     * Métodos para obtenção de dados relativos à execução.
     **************************************************************************/

    /**
     * Obtém o tempo que um frame demorou para ser atualizado e desenhado.
     * @return
     */
    public double getFrameTime() {
        return tempoFrame / 1000.0;
    }

    /**
     * Obtém a largura da tela.
     * 
     * @return largura da tela.
     */
    public int getScreenWidth() {
        return painelDesenho.getWidth();
    }

    /**
     * Obtém a altura da tela.
     * 
     * @return altura da tela.
     */
    public int getScreenHeight() {
        return painelDesenho.getHeight();
    }

    /***************************************************************************
     * Controle interno dos eventos.
     **************************************************************************/
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

    /*
     * Cores padrão.
     */
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
