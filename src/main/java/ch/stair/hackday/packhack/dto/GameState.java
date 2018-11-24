package ch.stair.hackday.packhack.dto;

import ch.stair.hackday.packhack.dto.PublicPlayer;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "publicPlayers",
        "gameField"
})
public class GameState
{
    private FieldTypes[][] gameField;
    private PublicPlayer[] publicPlayers;
    private int agentIndex;

    @JsonCreator
    public GameState(@JsonProperty("publicPlayers")PublicPlayer[] publicPlayers, @JsonProperty("gameField")FieldTypes[][] gameField, @JsonProperty("agent_id")int agentId){
        this.gameField = gameField;
        this.publicPlayers = publicPlayers;
        this.agentIndex = agentId;
    }

    public FieldTypes[][] getGameField ()
    {
        return gameField;
    }

    public void setGameField (FieldTypes[][] gameField)
    {
        this.gameField = gameField;
    }

    public PublicPlayer[] getPublicPlayers ()
    {
        return publicPlayers;
    }

    public void setPublicPlayers (PublicPlayer[] publicPlayers)
    {
        this.publicPlayers = publicPlayers;
    }

    public int getAgentIndex() {
        return agentIndex;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [gameField = "+gameField+", publicPlayers = "+publicPlayers+"]";
    }
}