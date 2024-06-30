/**
 * Modelo para desenvolvimento de exercícios criativos Java 2D.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Main extends JFrame {

    // configurações iniciais da janela
    private static int LARGURA_JANELA = 800;
    private static int ALTURA_JANELA = 600;
    private static String TITULO_JANELA = "Título da Janela";
    private static boolean ATIVAR_SUAVIZACAO = true;

    // declaração de variáveis
    int variavel = 0;
    
    private void processarEntrada() {

        // entrada de dados e processamento adicional
        Scanner scan = new Scanner( System.in );

        System.out.print( "Entre com um valor: " );
        variavel = Integer.parseInt( scan.nextLine() );

        scan.close();

    }

    private void desenhar( Graphics2D g2d ) {

        // lógica do desenho
        
        g2d.setColor( Color.BLACK );
        g2d.drawLine( 0, 0, variavel, variavel );

    }

    private class PainelDesenho extends JPanel {

        @Override
        public void paintComponent( Graphics g ) {
            g.setColor( Color.WHITE );
            g.clearRect( 0, 0, getWidth(), getHeight() );
            g.fillRect( 0, 0, getWidth(), getHeight() );
            Graphics2D g2d = (Graphics2D) g;
            if ( ATIVAR_SUAVIZACAO ) {
                g2d.setRenderingHint( 
                    RenderingHints.KEY_ANTIALIASING, 
                    RenderingHints.VALUE_ANTIALIAS_ON );
            }
            desenhar( (Graphics2D) g );
        }

    }

    public Main() {
        processarEntrada();
        setDefaultCloseOperation( EXIT_ON_CLOSE );
        setSize( LARGURA_JANELA, ALTURA_JANELA );
        setTitle( TITULO_JANELA );
        setLocationRelativeTo( null );
        add( new PainelDesenho(), BorderLayout.CENTER );
        setVisible( true );
    }

    public static void main( String[] args ) {
        new Main();
    }

}