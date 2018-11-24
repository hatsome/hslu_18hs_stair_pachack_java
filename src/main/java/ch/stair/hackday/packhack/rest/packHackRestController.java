package ch.stair.hackday.packhack.rest;

import ch.stair.hackday.packhack.agent.Agent;
import ch.stair.hackday.packhack.agent.Pacman;
import ch.stair.hackday.packhack.dto.Direction;
import ch.stair.hackday.packhack.dto.GameState;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;

@RestController
public class packHackRestController {

    private Agent agent;

    @PostConstruct
    public void populateMovieCache() {
        this.agent = new Pacman();
    }

    @RequestMapping(value = "/start",
            method = RequestMethod.POST,
            consumes = "text/plain",
            produces = "text/plain")
    public String start(){
        return this.agent.getAgentInformation();
    }

    @RequestMapping(value = "/chooseAction",
            method = RequestMethod.POST,
            consumes = "application/json",
            produces = "application/json")
    public Direction chooseAction(
            @RequestBody GameState gameField) {
        return this.agent.chooseAction(gameField);
    }
}
