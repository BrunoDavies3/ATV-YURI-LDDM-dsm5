package com.example.atv1
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale

// ==========================================
// 1. MODELAGEM DE DADOS E CAMADA DE DOMÍNIO
// ==========================================

interface Pagavel {
    fun calcularTotal(): Double
}

data class Produto(
    val nome: String,
    val preco: Double,
    val descricao: String? = null,
    val descontoPercentual: Double = 0.0
)

data class ItemCarrinho(
    val produto: Produto,
    val quantidade: Int
) : Pagavel {
    fun calcularSubtotalBruto(): Double = produto.preco * quantidade

    fun calcularValorDesconto(): Double = calcularSubtotalBruto() * (produto.descontoPercentual / 100.0)

    override fun calcularTotal(): Double = calcularSubtotalBruto() - calcularValorDesconto()
}

class Carrinho(val itens: List<ItemCarrinho>) : Pagavel {
    fun calcularSubtotalBruto(): Double = itens.sumOf { it.calcularSubtotalBruto() }

    fun calcularTotalDescontos(): Double = itens.sumOf { it.calcularValorDesconto() }

    override fun calcularTotal(): Double = itens.sumOf { it.calcularTotal() }
}

fun formatarMoeda(valor: Double): String {
    return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(valor)
}

// ==========================================
// 2. CATÁLOGO E REGRAS DE NEGÓCIO
// ==========================================

fun obterCatalogoDeTeste(): Carrinho {
    // 1. Criamos o nosso catálogo com pelo menos 6 produtos para cumprir as regras
    val produto1 = Produto("Notebook Dell Inspiron", 3499.00, "Um notebook rápido para suas tarefas diárias com processador de última geração.", 5.0)
    val produto2 = Produto("Mouse sem fio", 89.90, null, 0.0)
    val produto3 = Produto("Teclado mecânico RGB", 349.90, "Switch azul, ABNT2", 0.0)

    // Produtos extras apenas para compor o catálogo (com nomes longos, sem descrição, descontos, etc.)
    val produto4 = Produto("Monitor Ultrawide 29 polegadas Resolução 4K", 1200.00, "Monitor excelente para produtividade.", 10.0)
    val produto5 = Produto("Cabo USB-C", 45.0, null, 0.0)
    val produto6 = Produto("Headset Gamer Sem Fio", 450.00, "Áudio 7.1 surround.", 15.0)

    // 2. Adicionamos AO CARRINHO apenas os 3 produtos do Cenário de Validação com as respetivas quantidades
    val itensDoCarrinho = listOf(
        ItemCarrinho(produto1, 2), // 2 Notebooks
        ItemCarrinho(produto2, 1), // 1 Mouse
        ItemCarrinho(produto3, 1)  // 1 Teclado
    )

    return Carrinho(itensDoCarrinho)
}

// ==========================================
// 3. PROCESSAMENTO DE COLEÇÕES (LOGCAT)
// ==========================================

fun imprimirRelatorioLogcat(carrinho: Carrinho) {
    carrinho.itens
        .filter { it.produto.descontoPercentual > 0.0 }
        .sortedByDescending { it.calcularTotal() }
        .forEach { item ->
            Log.d(
                "RELATORIO_CARRINHO",
                "Produto: ${item.produto.nome} | Valor Final: ${formatarMoeda(item.calcularTotal())}"
            )
        }
}

// ==========================================
// 4. INTERFACE GRÁFICA (UI)
// ==========================================

@Composable
fun TelaCarrinhoDeCompras() {
    val carrinho = remember { obterCatalogoDeTeste() }

    // Dispara a impressão do relatório no Logcat ao carregar a tela
    LaunchedEffect(Unit) {
        imprimirRelatorioLogcat(carrinho)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Meu Carrinho",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(carrinho.itens) { item ->
                LinhaProduto(item = item)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        ResumoCarrinho(carrinho = carrinho)
    }
}

@Composable
fun LinhaProduto(item: ItemCarrinho) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(12.dp)
    ) {
        Text(
            text = item.produto.nome,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = item.produto.descricao ?: "Sem descrição",
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatarMoeda(item.produto.preco),
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "x${item.quantidade}",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = formatarMoeda(item.calcularTotal()),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun ResumoCarrinho(carrinho: Carrinho) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Subtotal", style = MaterialTheme.typography.bodyLarge)
            Text(formatarMoeda(carrinho.calcularSubtotalBruto()), style = MaterialTheme.typography.bodyLarge)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Descontos", style = MaterialTheme.typography.bodyLarge)
            Text("-${formatarMoeda(carrinho.calcularTotalDescontos())}", style = MaterialTheme.typography.bodyLarge)
        }

        Divider(modifier = Modifier.padding(vertical = 12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("TOTAL", style = MaterialTheme.typography.titleLarge)
            Text(formatarMoeda(carrinho.calcularTotal()), style = MaterialTheme.typography.titleLarge)
        }
    }
}