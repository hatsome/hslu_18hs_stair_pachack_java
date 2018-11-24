package ch.stair.hackday.packhack.agent;

import ch.stair.hackday.packhack.dto.Direction;
import ch.stair.hackday.packhack.dto.GameState;

public interface Agent {
    public Direction chooseAction(GameState gameState);
    public String getAgentInformation();
}