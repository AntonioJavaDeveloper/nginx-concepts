package br.com.javadeveloper.dslist.service;

import br.com.javadeveloper.dslist.dto.GameListDTO;
import br.com.javadeveloper.dslist.entities.GameList;
import br.com.javadeveloper.dslist.projections.GameMinProjection;
import br.com.javadeveloper.dslist.repositories.GameListRepository;
import br.com.javadeveloper.dslist.repositories.GameRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameListService {
    @Autowired
    private GameListRepository gameListRepository;

    @Autowired
    private GameRepository gameRepository;

    @Transactional(readOnly = true)
    public List<GameListDTO> findAll() {
        List<GameList> result = gameListRepository.findAll();
        return result.stream().map(GameListDTO::new).toList();
    }

    @Transactional
    public void move(Long listId, int sourceIndex, int destinationIndex) {

        List<GameMinProjection> list = this.gameRepository.searchByList(listId);

        GameMinProjection obj = list.remove(sourceIndex);
        list.add(destinationIndex, obj);

        int min = sourceIndex < destinationIndex ? sourceIndex : destinationIndex;
        int max = sourceIndex < destinationIndex ? destinationIndex : sourceIndex;

        for (int i = min; i <= max; i++) {
            this.gameListRepository.updateBelongingPosition(listId, list.get(i).getId(), i);
        }
    }
}
