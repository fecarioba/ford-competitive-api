package com.fordchallenge.ford_competitive_api.searches.controller;

import com.fordchallenge.ford_competitive_api.searches.dto.SearchHistoryResponse;
import com.fordchallenge.ford_competitive_api.searches.service.SearchHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/searches")
@Tag(name = "Search History", description = "Histórico de pesquisas (todos os endpoints exigem Bearer Token)")
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    public SearchHistoryController(SearchHistoryService searchHistoryService) {
        this.searchHistoryService = searchHistoryService;
    }

    @Operation(summary = "Histórico de pesquisas do usuário autenticado (perfil USER ou ADMIN)")
    @GetMapping("/history")
    public ResponseEntity<List<SearchHistoryResponse>> findMyHistory() {
        return ResponseEntity.ok(searchHistoryService.findMyHistory());
    }

    @Operation(summary = "Histórico de pesquisas de TODOS os usuários (exclusivo do perfil ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/history/all")
    public ResponseEntity<List<SearchHistoryResponse>> findAllHistory() {
        return ResponseEntity.ok(searchHistoryService.findAllHistory());
    }
}
