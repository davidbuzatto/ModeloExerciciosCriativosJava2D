import java.util.Scanner;

/**
 * Modelo para desenvolvimento de exercícios criativos Java 2D.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public class Main extends Desenhista {

    // declaração de variáveis

    @Override
    public void preparar() {

        // configurações iniciais da janela

        larguraJanela = 800;
        alturaJanela = 600;
        tituloJanela = "Título da Janela";
        ativarSuavizacao = true;

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

    public Main() {
        preparar();
        processarEntrada();
        iniciarComponentes();
    }

    public static void main( String[] args ) {
        new Main();
    }

}