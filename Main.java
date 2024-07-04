import java.util.Scanner;

/**
 * Modelo para desenvolvimento de exercícios criativos Java 2D.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public class Main extends Desenhista {

    // declaração de variáveis

    public Main() {
        // cria uma janela de 800 pixels de largura por 600 de altura com suavização
        super( 800, 600, "Título da Janela", true );
    }

    @Override
    public void processarEntrada() {

        Scanner scan = new Scanner( System.in );

        // entrada de dados e processamento adicional

        scan.close();

    }

    @Override
    public void desenhar() {

        // lógica do desenho

    }

    public static void main( String[] args ) {
        new Main();
    }

}