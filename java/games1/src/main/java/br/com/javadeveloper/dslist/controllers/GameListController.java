package br.com.javadeveloper.dslist.controllers;

import br.com.javadeveloper.dslist.dto.GameListDTO;
import br.com.javadeveloper.dslist.dto.GameMinDTO;
import br.com.javadeveloper.dslist.dto.ReplacementDTO;
import br.com.javadeveloper.dslist.dto.ServerResponse;
import br.com.javadeveloper.dslist.service.GameListService;
import br.com.javadeveloper.dslist.service.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/lists")
public class GameListController {
    @Autowired
    private GameListService gameListService;

    @Autowired
    private GameService gameService;


    @GetMapping
    public List<GameListDTO> findAll() {
        List<GameListDTO> result = this.gameListService.findAll();
        return result;
    }

    @GetMapping(value = "/{listId}/games")
    public List<GameMinDTO> findGames(@PathVariable Long listId) {
        List<GameMinDTO> result = this.gameService.findByGameList(listId);
        return result;
    }

    @PostMapping(value = "/{listId}/replacement")
    public void move(@PathVariable Long listId, @RequestBody ReplacementDTO body) {
        this.gameListService.move(listId, body.getSourceIndex(), body.getDestinationIndex());
    }
}
