import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V> {

    private No<K, V> raiz;
    private Comparator<K> comparador;
    private int tamanho;

    private long comparacoes;
    private long inicio;
    private long termino;


    private void init(Comparator<K> comparador) {
        this.raiz = null;
        this.tamanho = 0;
        this.comparador = comparador;
    }

    @SuppressWarnings("unchecked")
    public ABB() {
        init((Comparator<K>) Comparator.naturalOrder());
    }

    public ABB(Comparator<K> comparador) {
        init(comparador);
    }

    public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
        ABB<K, V> nova = new ABB<>();
        nova = copiarArvore(original.raiz, funcaoChave, nova);
        this.raiz = nova.raiz;
    }

    private <T> ABB<T, V> copiarArvore(No<?, V> raizOrigem, Function<V, T> geradorChave,
                                      ABB<T, V> destino) {

        if (raizOrigem != null) {
            destino = copiarArvore(raizOrigem.getEsquerda(), geradorChave, destino);

            V item = raizOrigem.getItem();
            T chave = geradorChave.apply(item);
            destino.inserir(chave, item);

            destino = copiarArvore(raizOrigem.getDireita(), geradorChave, destino);
        }
        return destino;
    }


    public boolean vazia() {
        return raiz == null;
    }

    @Override
    public V pesquisar(K chave) {
        comparacoes = 0;
        inicio = System.nanoTime();

        V resultado = pesquisar(raiz, chave);

        termino = System.nanoTime();
        return resultado;
    }

    private V pesquisar(No<K, V> atual, K chave) {
        comparacoes++;

        if (atual == null)
            throw new NoSuchElementException("O item não foi localizado na árvore!");

        int cmp = comparador.compare(chave, atual.getChave());

        if (cmp == 0)
            return atual.getItem();

        return (cmp < 0)
                ? pesquisar(atual.getEsquerda(), chave)
                : pesquisar(atual.getDireita(), chave);
    }

    @Override
    public int inserir(K chave, V item) { // exercicio feito em aula
        comparacoes = 0;
        inicio = System.nanoTime();

        raiz = inserir(raiz, chave, item);

        termino = System.nanoTime();
        return tamanho;
    }

    private No<K, V> inserir(No<K, V> atual, K chave, V item) {

        if (atual == null) {
            tamanho++;
            return new No<>(chave, item);
        }

        comparacoes++;

        int cmp = comparador.compare(chave, atual.getChave());

        if (cmp < 0) {
            atual.setEsquerda(inserir(atual.getEsquerda(), chave, item));
        } else if (cmp > 0) {
            atual.setDireita(inserir(atual.getDireita(), chave, item));
        } else {
            // chave igual → substitui o item
            atual.setItem(item);
        }

        return atual;
    }


    @Override
    public String toString() {
        return percorrer();
    }

    @Override
    public String percorrer() {
        return caminhamentoEmOrdem();
    }

    public String caminhamentoEmOrdem() { // exercicio feito em aula
        StringBuilder sb = new StringBuilder();
        caminhamentoEmOrdem(raiz, sb);
        return sb.toString();
    }

    private void caminhamentoEmOrdem(No<K, V> no, StringBuilder sb) {
        if (no == null) return;

        caminhamentoEmOrdem(no.getEsquerda(), sb);
        sb.append("(")
          .append(no.getChave())
          .append(", ")
          .append(no.getItem())
          .append(") ");
        caminhamentoEmOrdem(no.getDireita(), sb);
    }

    @Override
    public V remover(K chave) { // exercicio feito em laboratorio
        comparacoes = 0;
        inicio = System.nanoTime();

        V valor = pesquisar(chave);

        raiz = remover(raiz, chave);
        tamanho--;

        termino = System.nanoTime();
        return valor;
    }

    private No<K, V> remover(No<K, V> atual, K chave) {

        if (atual == null)
            return null;

        comparacoes++;

        int cmp = comparador.compare(chave, atual.getChave());

        if (cmp < 0) {
            atual.setEsquerda(remover(atual.getEsquerda(), chave));
        }
        else if (cmp > 0) {
            atual.setDireita(remover(atual.getDireita(), chave));
        }
        else { 
          
            if (atual.getEsquerda() == null && atual.getDireita() == null)
                return null;

            if (atual.getEsquerda() == null)
                return atual.getDireita();
            if (atual.getDireita() == null)
                return atual.getEsquerda();

            No<K, V> sucessor = encontrarMinimo(atual.getDireita());
            atual.setChave(sucessor.getChave());
            atual.setItem(sucessor.getItem());

            atual.setDireita(remover(atual.getDireita(), sucessor.getChave()));
        }

        return atual;
    }

    private No<K, V> encontrarMinimo(No<K, V> no) {
        while (no.getEsquerda() != null)
            no = no.getEsquerda();
        return no;
    }


    @Override
    public int tamanho() {
        return tamanho;
    }

    @Override
    public long getComparacoes() {
        return comparacoes;
    }

    @Override
    public double getTempo() {
        return (termino - inicio) / 1_000_000.0;
    }
}
