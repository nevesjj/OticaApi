# Ótica — Sistema de gestão

Backend (Spring Boot 4 / Java 17 / MySQL 8) em `C:\Otica\Back\Otica` e frontend (Angular 20 + Tailwind) em `C:\Otica\Front`.

## Como rodar

```bash
# 1) Backend — http://localhost:8080 (MySQL precisa estar rodando)
cd C:\Otica\Back\Otica
./mvnw spring-boot:run

# 2) Frontend — http://localhost:4200
cd C:\Otica\Front
npm install      # só na primeira vez
npm start        # ng serve; /api é redirecionado para a porta 8080 (proxy.conf.json)
```

Primeiro acesso: usuário **admin**, senha **admin123**. Troque a senha em "Minha senha" logo depois de entrar.

## Banco de dados

MySQL, schema `otica`.

**Senhas e segredos não vão para o git.** Crie o arquivo `config/application.properties` na raiz do projeto
(já está no `.gitignore`; o Spring Boot o carrega automaticamente):

```properties
spring.datasource.username=root
spring.datasource.password=SUA_SENHA
app.jwt.secret=UM_TEXTO_ALEATORIO_COM_PELO_MENOS_32_CARACTERES
```
As tabelas são criadas e atualizadas automaticamente a partir das entidades JPA (`spring.jpa.hibernate.ddl-auto=update`):
`usuarios`, `clientes`, `receitas`, `fornecedores`, `produtos`, `movimentacoes_estoque`, `vendas`, `itens_venda`, `lancamentos`.

Variáveis de ambiente para produção: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (mínimo de 32 caracteres),
`ADMIN_LOGIN`, `ADMIN_SENHA`, `CORS_ORIGINS`, `PORT`.

## Módulos

| Módulo | O que faz |
|---|---|
| Painel | Vendido no mês, ticket médio, caixa, contas vencidas, gráfico de 6 meses, próximas entregas, estoque baixo, aniversariantes (com link para WhatsApp) |
| Vendas / OS | Orçamento → Em produção → Pronta → Entregue (ou Cancelada). Itens, desconto, receita vinculada, previsão de entrega, impressão da OS |
| Clientes | Cadastro (CEP preenche o endereço), receitas com OD/OE (esférico, cilíndrico, eixo, adição, DNP, altura) e histórico de compras |
| Produtos | Armação, lente, lente de contato, óculos de sol, acessório e serviço. Custo, venda, margem e estoque mínimo |
| Estoque | Entrada (pode gerar uma conta a pagar), saída e ajuste de inventário, com histórico completo |
| Financeiro | Contas a receber e a pagar, parcelamento, baixa, estorno, cancelamento, resumo do período e totais por categoria |
| Fornecedores / Usuários | Cadastros. Usuários com perfil ADMIN ou FUNCIONARIO (só o ADMIN gerencia usuários) |

## Regras de negócio

- **Confirmar uma venda** dá baixa no estoque (bloqueia se o estoque for insuficiente) e gera o financeiro:
  - Dinheiro, PIX ou débito: um lançamento já pago.
  - Crédito, crediário ou boleto: entrada paga + N parcelas mensais pendentes. Crediário e boleto exigem um cliente cadastrado.
- **Cancelar uma venda** devolve os produtos ao estoque, cancela as parcelas pendentes e lança um estorno (despesa) do que já foi recebido. Uma venda já entregue não pode ser cancelada.
- Só orçamentos podem ser editados ou excluídos.
- Clientes, produtos e fornecedores com histórico são **desativados** em vez de excluídos.
- Serviços (montagem etc.) não controlam estoque.

## Testes

```bash
./mvnw test      # usam H2 em memória; não tocam no MySQL
```
