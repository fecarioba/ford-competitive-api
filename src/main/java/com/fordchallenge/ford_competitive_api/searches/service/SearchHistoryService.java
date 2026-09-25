package com.fordchallenge.ford_competitive_api.searches.service;

import com.fordchallenge.ford_competitive_api.searches.dto.SearchHistoryResponse;
import com.fordchallenge.ford_competitive_api.searches.entity.SearchHistory;
import com.fordchallenge.ford_competitive_api.searches.repository.SearchHistoryRepository;
import com.fordchallenge.ford_competitive_api.security.AuthenticatedUserProvider;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public SearchHistoryService(
            SearchHistoryRepository searchHistoryRepository,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.searchHistoryRepository = searchHistoryRepository;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    /**
     * Retorna apenas o histórico do usuário autenticado (perfil USER ou ADMIN).
     * Antes, este método (via bug em VehicleService) sempre gravava e listava
     * o histórico do usuário de id fixo 1L, independentemente de quem pesquisou.
     */
    public List<SearchHistoryResponse> findMyHistory() {
        User currentUser = authenticatedUserProvider.getCurrentUser();

        return searchHistoryRepository.findAllByUserIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Retorna o histórico de todos os usuários. Restrito ao perfil ADMIN
     * (ver @PreAuthorize no SearchHistoryController), demonstrando o controle
     * de acesso por diferentes perfis exigido na entrega.
     */
    public List<SearchHistoryResponse> findAllHistory() {
        return searchHistoryRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SearchHistoryResponse mapToResponse(SearchHistory history) {
        return new SearchHistoryResponse(
                history.getId(),
                history.getTermoBusca(),
                history.getUser() != null ? history.getUser().getId() : null,
                history.getVehicle() != null ? history.getVehicle().getId() : null,
                history.getVehicle() != null ? history.getVehicle().getMarca() : null,
                history.getVehicle() != null ? history.getVehicle().getModelo() : null,
                history.getVehicle() != null ? history.getVehicle().getAno() : null,
                history.getVehicle() != null ? history.getVehicle().getVersao() : null,
                history.getCreatedAt()
        );
    }
}
