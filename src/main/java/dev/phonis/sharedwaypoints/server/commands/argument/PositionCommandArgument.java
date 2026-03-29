package dev.phonis.sharedwaypoints.server.commands.argument;

import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;

public class PositionCommandArgument extends NoSuggestionCommandArgument<Coordinates>
{

    public PositionCommandArgument(String name)
    {
        super(name, BlockPosArgument.blockPos());
    }

}
