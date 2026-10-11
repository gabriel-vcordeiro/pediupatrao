package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto novoProduto(Produto produto) {
        return produtoRepository.save(produto);
    }

    public List<Produto> listarProdutos() {
        return produtoRepository.findAll();
    }

    // Método para excluir cliente
    public void excluir(String id) {
        produtoRepository.deleteById(id);
    }


    // Método para salvar um novo produto ou atualizar um produto existente
    public Produto salvar(Produto produto) {
        if (produto.getAtivo() == null) {
            produto.setAtivo(true);
        }
        // Se o produto não tem ID (novo produto), salva como novo
        if (produto.getId() == null || produto.getId().isBlank()) {
            produto.setId(null);
            return produtoRepository.save(produto);
        } else {
            // Verifica se o produto existe antes de atualizar
            if (produtoRepository.existsById(produto.getId())) {
                return produtoRepository.save(produto);
            } else {
                throw new IllegalArgumentException("Produto não encontrado para atualização.");
            }
        }
    }
}
