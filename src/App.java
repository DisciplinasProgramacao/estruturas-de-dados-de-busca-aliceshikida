import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    static String nomeArquivoDados;
    static Scanner teclado;
    static int quantosProdutos = 0;

    // Árvores Binárias de Busca (Padrão)
    static ABB<String, Produto> produtosCadastradosPorNome;
    static ABB<Integer, Produto> produtosCadastradosPorId;

    // Árvores AVL (Balanceadas)
    static AVL<String, Produto> produtosBalanceadosPorNome;
    static AVL<Integer, Produto> produtosBalanceadosPorId;

    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("\nDigite enter para continuar...");
        teclado.nextLine();
    }

    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }

    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        T valor;
        System.out.println(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (Exception e) {
            return null;
        }
        return valor;
    }

    static int menu() {
        cabecalho();
        System.out.println("1 - Carregar produtos por nome (ABB apenas)");
        System.out.println("2 - Carregar e COMPARAR criação por ID (ABB vs AVL)");
        System.out.println("3 - Procurar produto, por nome");
        System.out.println("4 - Procurar e COMPARAR busca por ID (ABB vs AVL)");
        System.out.println("5 - Remover produto, por nome");
        System.out.println("6 - Remover produto, por id");
        System.out.println("7 - Recortar (Filtro) por nome");
        System.out.println("8 - Recortar (Filtro) por id");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        try {
            return Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /**
     * Método auxiliar para ler o arquivo e retornar uma Lista crua de produtos.
     */
    static List<Produto> lerProdutosParaBuffer(String nomeArquivoDados) {
        Scanner arquivo = null;
        List<Produto> buffer = new ArrayList<>();
        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            int numProdutos = Integer.parseInt(arquivo.nextLine());
            for (int i = 0; i < numProdutos; i++) {
                buffer.add(Produto.criarDoTexto(arquivo.nextLine()));
            }
            quantosProdutos = numProdutos;
        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo: " + e.getMessage());
            buffer = null;
        } finally {
            if (arquivo != null) arquivo.close();
        }
        return buffer;
    }

    /**
     * Carrega os dados do arquivo, insere na ABB e na AVL, e compara os tempos.
     */
    static void carregarECompararArvoresId() {
        cabecalho();
        System.out.println("Lendo arquivo para memória temporária...");

        List<Produto> buffer = lerProdutosParaBuffer(nomeArquivoDados);

        if (buffer == null) return;

        System.out.println("Arquivo lido. Iniciando construção das árvores...");
        System.out.println("--------------------------------------------------");


        produtosCadastradosPorId = new ABB<>();
        long inicio = System.nanoTime();

        for (Produto p : buffer) {

            produtosCadastradosPorId.inserir(p.idProduto, p);
        }

        long fim = System.nanoTime();
        long tempoABB = fim - inicio;
        System.out.println("Tempo de inserção na ABB: " + tempoABB + " ns (" + (tempoABB/1_000_000) + " ms)");

        // 3. Construção e Medição da AVL
        produtosBalanceadosPorId = new AVL<>();
        inicio = System.nanoTime();

        for (Produto p : buffer) {

            produtosBalanceadosPorId.inserir(p.idProduto, p);
        }

        fim = System.nanoTime();
        long tempoAVL = fim - inicio;
        System.out.println("Tempo de inserção na AVL: " + tempoAVL + " ns (" + (tempoAVL/1_000_000) + " ms)");

        System.out.println("--------------------------------------------------");
        if(tempoAVL > tempoABB) {
            System.out.println("A AVL demorou mais na construção (esperado, devido às rotações).");
        } else {
            System.out.println("A AVL foi mais rápida (incomum para inserção, verifique a ordem dos dados).");
        }
    }


    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        List<Produto> buffer = lerProdutosParaBuffer(nomeArquivoDados);
        ABB<K, Produto> arvore = new ABB<>();
        if(buffer != null) {
            for(Produto p : buffer) {
                arvore.inserir(extratorDeChave.apply(p), p);
            }
        }
        return arvore;
    }

    // --- MÉTODOS DE BUSCA E REMOÇÃO ---

    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
        Produto produto;
        cabecalho();
        try {
            produto = produtosCadastrados.pesquisar(procurado);
        } catch (NoSuchElementException excecao) {
            produto = null;
        }
        return produto;
    }

    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        return localizarProduto(produtosCadastrados, idProduto);
    }

    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.println("Digite o nome ou a descrição do produto desejado:");
        String descricao = teclado.nextLine();
        return localizarProduto(produtosCadastrados, descricao);
    }

    private static void mostrarProduto(Produto produto) {
        cabecalho();
        String mensagem = "Produto não encontrado!";
        if (produto != null) mensagem = String.format("Dados do produto:\n%s", produto);
        System.out.println(mensagem);
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave){
        cabecalho();
        try {
            return produtosCadastrados.remover(chave);
        } catch(NoSuchElementException e) {
            return null;
        }
    }

    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
        int id = lerOpcao("Digite o id do produto a remover:", Integer.class);
        return removerProduto(produtosCadastrados, id);
    }

    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
        System.out.print("Digite a descrição a remover: ");
        String descricao = teclado.nextLine();
        return removerProduto(produtosCadastrados, descricao);
    }

    /**
     * Busca o ID nas duas árvores e compara desempenho
     */
    static void compararBuscaPorID() {
        if (produtosCadastradosPorId == null || produtosBalanceadosPorId == null) {
            System.out.println("Você precisa carregar as árvores por ID (Opção 2) antes de buscar.");
            return;
        }

        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        cabecalho();
        System.out.println("Comparando busca pelo ID: " + idProduto);
        System.out.println("--------------------------------------");

        // Busca na ABB
        Produto pAbb = null;
        long inicio = System.nanoTime();
        try {
            pAbb = produtosCadastradosPorId.pesquisar(idProduto);
        } catch (Exception e) { pAbb = null; }
        long fim = System.nanoTime();
        long tempoABB = fim - inicio;
        System.out.println("[ABB] Tempo: " + tempoABB + " ns");

        // Busca na AVL
        Produto pAvl = null;
        inicio = System.nanoTime();
        try {
            pAvl = produtosBalanceadosPorId.pesquisar(idProduto);
        } catch (Exception e) { pAvl = null; }
        fim = System.nanoTime();
        long tempoAVL = fim - inicio;
        System.out.println("[AVL] Tempo: " + tempoAVL + " ns");

        System.out.println("--------------------------------------");

        if (pAbb != null) {
            System.out.println("Produto encontrado: " + pAbb);
        } else {
            System.out.println("Produto não encontrado em nenhuma das árvores.");
        }
    }

    // --- MÉTODOS DE RECORTE ---

    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
        cabecalho();

        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("Erro: A árvore de nomes está vazia ou não foi carregada (Use a Opção 1).");
            return;
        }

        System.out.println("--- Recorte (Filtro) por Nome/Descrição ---");
        System.out.println("Digite o termo INICIAL do intervalo (ex: 'Caixa'): ");
        String inicio = teclado.nextLine();

        System.out.println("Digite o termo FINAL do intervalo (ex: 'Dado'): ");
        String fim = teclado.nextLine();

        System.out.println("\nProcessando recorte na árvore...");
        long tInicio = System.nanoTime();


        Object resultado = produtosCadastrados.recortar(inicio, fim);

        long tFim = System.nanoTime();

        System.out.println("Resultado do filtro [" + inicio + " ... " + fim + "]:");
        System.out.println(resultado);

        System.out.println("\nTempo de operação: " + (tFim - tInicio) + " ns");
    }

    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
        cabecalho();

        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("Erro: A árvore de IDs está vazia ou não foi carregada (Use a Opção 2).");
            return;
        }

        System.out.println("--- Recorte (Filtro) por ID ---");

        Integer idInicio = lerOpcao("Digite o ID INICIAL do intervalo:", Integer.class);
        Integer idFim = lerOpcao("Digite o ID FINAL do intervalo:", Integer.class);

        if (idInicio == null || idFim == null) {
            System.out.println("IDs inválidos.");
            return;
        }

        System.out.println("\nProcessando recorte na árvore...");
        long tInicio = System.nanoTime();


        Object resultado = produtosCadastrados.recortar(idInicio, idFim);

        long tFim = System.nanoTime();

        System.out.println("Resultado do filtro [ID " + idInicio + " ... " + idFim + "]:");
        System.out.println(resultado);

        System.out.println("\nTempo de operação: " + (tFim - tInicio) + " ns");
    }

    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";

        int opcao = -1;

        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
                case 2 -> carregarECompararArvoresId();
                case 3 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 4 -> compararBuscaPorID();
                case 5 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
                case 6 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
                case 7 -> recortarProdutosNome(produtosCadastradosPorNome);
                case 8 -> recortarProdutosId(produtosCadastradosPorId);
            }
            pausa();
        } while(opcao != 0);

        teclado.close();
    }
}