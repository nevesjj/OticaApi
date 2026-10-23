package api.otica.domain;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes_estoque")
public class MovimentacaoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacao tipo;

    /** Quantidade movimentada; num ajuste é a diferença entre o novo saldo e o anterior. */
    private int quantidade;

    private int saldoAnterior;

    private int saldoAtual;

    @Column(length = 255)
    private String motivo;

    private Long vendaId;

    @Column(length = 60)
    private String usuario;

    private LocalDateTime data = LocalDateTime.now();

    public Long getId() { return id; }
    public Produto getProduto() { return produto; }
    public void setProduto(Produto produto) { this.produto = produto; }
    public TipoMovimentacao getTipo() { return tipo; }
    public void setTipo(TipoMovimentacao tipo) { this.tipo = tipo; }
    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
    public int getSaldoAnterior() { return saldoAnterior; }
    public void setSaldoAnterior(int saldoAnterior) { this.saldoAnterior = saldoAnterior; }
    public int getSaldoAtual() { return saldoAtual; }
    public void setSaldoAtual(int saldoAtual) { this.saldoAtual = saldoAtual; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public Long getVendaId() { return vendaId; }
    public void setVendaId(Long vendaId) { this.vendaId = vendaId; }
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }
    public LocalDateTime getData() { return data; }
}
