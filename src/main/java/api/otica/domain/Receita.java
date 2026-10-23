package api.otica.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Receita oftalmológica (prescrição) de um cliente. OD = olho direito, OE = olho esquerdo. */
@Entity
@Table(name = "receitas")
public class Receita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false)
    private LocalDate dataExame;

    private LocalDate validade;

    @Column(length = 120)
    private String medico;

    @Column(length = 20)
    private String crm;

    @Column(precision = 5, scale = 2) private BigDecimal odEsferico;
    @Column(precision = 5, scale = 2) private BigDecimal odCilindrico;
    private Integer odEixo;
    @Column(precision = 5, scale = 2) private BigDecimal odAdicao;
    @Column(precision = 5, scale = 1) private BigDecimal odDnp;
    @Column(precision = 5, scale = 1) private BigDecimal odAltura;

    @Column(precision = 5, scale = 2) private BigDecimal oeEsferico;
    @Column(precision = 5, scale = 2) private BigDecimal oeCilindrico;
    private Integer oeEixo;
    @Column(precision = 5, scale = 2) private BigDecimal oeAdicao;
    @Column(precision = 5, scale = 1) private BigDecimal oeDnp;
    @Column(precision = 5, scale = 1) private BigDecimal oeAltura;

    @Column(length = 1000)
    private String observacoes;

    private LocalDateTime criadoEm = LocalDateTime.now();

    public Long getId() { return id; }
    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }
    public LocalDate getDataExame() { return dataExame; }
    public void setDataExame(LocalDate dataExame) { this.dataExame = dataExame; }
    public LocalDate getValidade() { return validade; }
    public void setValidade(LocalDate validade) { this.validade = validade; }
    public String getMedico() { return medico; }
    public void setMedico(String medico) { this.medico = medico; }
    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
    public BigDecimal getOdEsferico() { return odEsferico; }
    public void setOdEsferico(BigDecimal v) { this.odEsferico = v; }
    public BigDecimal getOdCilindrico() { return odCilindrico; }
    public void setOdCilindrico(BigDecimal v) { this.odCilindrico = v; }
    public Integer getOdEixo() { return odEixo; }
    public void setOdEixo(Integer v) { this.odEixo = v; }
    public BigDecimal getOdAdicao() { return odAdicao; }
    public void setOdAdicao(BigDecimal v) { this.odAdicao = v; }
    public BigDecimal getOdDnp() { return odDnp; }
    public void setOdDnp(BigDecimal v) { this.odDnp = v; }
    public BigDecimal getOdAltura() { return odAltura; }
    public void setOdAltura(BigDecimal v) { this.odAltura = v; }
    public BigDecimal getOeEsferico() { return oeEsferico; }
    public void setOeEsferico(BigDecimal v) { this.oeEsferico = v; }
    public BigDecimal getOeCilindrico() { return oeCilindrico; }
    public void setOeCilindrico(BigDecimal v) { this.oeCilindrico = v; }
    public Integer getOeEixo() { return oeEixo; }
    public void setOeEixo(Integer v) { this.oeEixo = v; }
    public BigDecimal getOeAdicao() { return oeAdicao; }
    public void setOeAdicao(BigDecimal v) { this.oeAdicao = v; }
    public BigDecimal getOeDnp() { return oeDnp; }
    public void setOeDnp(BigDecimal v) { this.oeDnp = v; }
    public BigDecimal getOeAltura() { return oeAltura; }
    public void setOeAltura(BigDecimal v) { this.oeAltura = v; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
