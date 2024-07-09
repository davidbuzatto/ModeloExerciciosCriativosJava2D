import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.Scanner;

/**
 * Modelo para desenvolvimento de exercícios criativos Java 2D.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public class Main extends Engine {

    // declaração de variáveis

    public Main() {
        // cria uma janela de 800 pixels de largura por 600 de altura
        // com suavização e que atualiza o desenho a uma taxa de 60 quadros
        // por segundo
        super( 800, 600, "Título da Janela", true, 60 );
    }

    @Override
    public void processarEntrada() {

        Scanner scan = new Scanner( System.in );

        // entrada de dados e processamento adicional

        scan.close();

    }

    @Override
    public void criar() {

        // cria todos os objetos e define o valor das variáveis do jogo ou 
        // simulação

    }

    @Override
    public void atualizar() {

        // atualiza os dados do jogo ou simulação

    }

    @Override
    public void desenhar() {

        // lógica do desenho

    }

    @Override
    public void tratarMouse( MouseEvent e, MouseEventType met ) {

        // tratamento dos eventos do mouse
        switch ( met ) {

            // clicou
            case CLICKED: {
                break;
            }

            // pressionou
            case PRESSED: {
                break;
            }

            // soltou
            case RELEASED: {
                break;
            }

            // entrou
            case ENTERED: {
                break;
            }

            // saiu
            case EXITED: {
                break;
            }

            // arrastou
            case DRAGGED: {
                break;
            }

            // moveu
            case MOVED: {
                break;
            }

        }

    }

    @Override
    public void tratarRodaRolagemMouse( MouseWheelEvent e ) {
        
        // tratamento caso a roda de rolagem do mouse girou

    }

    @Override
    public void tratarTeclado( KeyEvent e, KeyboardEventType ket ) {

        // tratamento dos eventos do teclado
        switch ( ket ) {

            // tecla pressionada
            case PRESSED: {
                break;
            }

            // tecla liberada
            case RELEASED: {
                break;
            }

        }

    }

    public static void main( String[] args ) {
        new Main();
    }

}