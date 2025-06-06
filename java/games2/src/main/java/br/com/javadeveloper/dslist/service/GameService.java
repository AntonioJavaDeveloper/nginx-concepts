package br.com.javadeveloper.dslist.service;

import br.com.javadeveloper.dslist.dto.GameDTO;
import br.com.javadeveloper.dslist.dto.GameMinDTO;
import br.com.javadeveloper.dslist.entities.Game;
import br.com.javadeveloper.dslist.projections.GameMinProjection;
import br.com.javadeveloper.dslist.repositories.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GameService {
    @Autowired
    private GameRepository gameRepository;

    @Transactional(readOnly = true)
    public GameDTO findById(Long id) {
        Game result = this.gameRepository.findById(id).get();
        return new GameDTO(result);
    }

    @Transactional(readOnly = true)
    public List<GameMinDTO> findAll() {
        List<Game> result = this.gameRepository.findAll();
        return result.stream().map(GameMinDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<GameMinDTO> findByGameList(Long listId) {
        List<GameMinProjection> games = this.gameRepository.searchByList(listId);
        return games.stream().map(GameMinDTO::new).toList();
    }
}
