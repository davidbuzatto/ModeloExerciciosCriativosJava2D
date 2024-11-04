import br.com.davidbuzatto.jsge.core.engine.EngineFrame;

/**
 * Modelo para desenvolvimento de exercícios criativos em Java 2D.
 * 
 * @author Prof. Dr. David Buzatto
 * @copyright Copyright (c) 2024
 */
public class Main extends EngineFrame {

    // declaração de variáveis

    public Main() {

        // cria a janela do jogo ou simulação
        super( 
            800,                  // 800 pixels de largura
            600,                  // 600 pixels de altura
            "Título da Janela",   // título da janela
            60,                   // 60 quadros por segundo
            true                  // ativa a suavização (antialiasing)
        );

    }

    /**
     * Processa a entrada inicial fornecida pelo usuário e cria
     * e/ou inicializa os objetos/contextos/variáveis do jogo ou simulação.
     */
    @Override
    public void create() {
    }

    /**
     * Atualiza os objetos/contextos/variáveis do jogo ou simulação.
     * O parâmetro delta contém o tempo que passou entre o quadro
     * anterior e o quadro atual.
     */
    @Override
    public void update( double delta ) {
    }

    /**
     * Desenha o estado dos objetos/contextos/variáveis do jogo ou simulação.
     */
    @Override
    public void draw() {
    }

    public static void main( String[] args ) {
        new Main();
    }

}