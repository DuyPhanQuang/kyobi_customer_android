package com.kyobi.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import coil.compose.AsyncImage

@Composable
fun HomeTab(viewModel: HomeTabViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                ProductsSection(uiState.productsResult)
            }
        }
    }
}

@Composable
fun ProductsSection(productsResult: DomainNetworkResult<List<Product>>) {
    when (productsResult) {
        is DomainNetworkResult.Success -> {
            ProductList(products = productsResult.data)
        }
        is DomainNetworkResult.Error -> {
            Text(
                text = "Lỗi: ${productsResult.exception.message}",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }
        is DomainNetworkResult.Loading -> {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }
    }
}

@Composable
fun ProductList(products: List<Product>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(products) { product ->
            ProductItem(product = product)
        }
    }
}

@Composable
fun ProductItem(product: Product) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.title,
                modifier = Modifier
                    .size(80.dp)
                    .padding(end = 16.dp)
            )
            Column {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}