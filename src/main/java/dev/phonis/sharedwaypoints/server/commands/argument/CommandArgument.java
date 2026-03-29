package dev.phonis.sharedwaypoints.server.commands.argument;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;

public abstract class CommandArgument<T> implements SuggestionProvider<CommandSourceStack>
{

    public final String name;
    public final ArgumentType<T> type;
    private Command<CommandSourceStack> executor; // set by command

    public CommandArgument(String name, ArgumentType<T> type)
    {
        this.name = name;
        this.type = type;
    }

    public CommandArgument<T> setExecutor(Command<CommandSourceStack> executor)
    {
        this.executor = executor;

        return this;
    }

    public Command<CommandSourceStack> getExecutor()
    {
        return this.executor;
    }

}
