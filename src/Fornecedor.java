import java.util.ArrayList;
import java.util.List;


public class Fornecedor {


    private static int _ultimoID = 10_000;


    private final int documento;
    private final String nome;


    private final List<Produto> produtos;



    public Fornecedor(String nome) {


        if (nome == null || nome.trim().isEmpty() || nome.trim().split("\\s+").length < 2) {
            throw new IllegalArgumentException(
                    "O nome do fornecedor deve conter pelo menos nome e sobrenome."
            );
        }
        this.nome = nome.trim();


        Fornecedor._ultimoID++;
        this.documento = Fornecedor._ultimoID;


        this.produtos = new ArrayList<>();
    }



    public void adicionarProduto(Produto novo) {

        if (novo == null) {
            throw new IllegalArgumentException("Não podem ser armazenados produtos nulos.");
        }

        this.produtos.add(novo);
    }



    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("--- Fornecedor ---\n");
        sb.append("Nome: ").append(nome).append("\n");
        sb.append("Documento (ID): ").append(documento).append("\n");

        sb.append("--- Histórico de Produtos (Total: ").append(produtos.size()).append(") ---\n");

        if (produtos.isEmpty()) {
            sb.append("Nenhum produto registrado.\n");
        } else {

            for (Produto produto : produtos) {

                sb.append("* ").append(produto.toString()).append("\n");
            }
        }
        sb.append("---------------------------------\n");
        return sb.toString();
    }



    @Override
    public int hashCode() {
        return this.documento;
    }


    @Override
    public boolean equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fornecedor outro = (Fornecedor) obj;
        return documento == outro.documento;
    }
}