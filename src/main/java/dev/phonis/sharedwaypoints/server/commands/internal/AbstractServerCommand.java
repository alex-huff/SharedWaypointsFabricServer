package dev.phonis.sharedwaypoints.server.commands.internal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import dev.phonis.sharedwaypoints.server.commands.util.ContextUtil;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.Entity;

public abstract class AbstractServerCommand implements IServerCommand
{

    private final String name;
    private final List<String> aliases = new LinkedList<>();
    private final List<IServerCommand> subCommands = new LinkedList<>();

    public AbstractServerCommand(String name)
    {
        this.name = name;
    }

    protected void addAlias(String alias)
    {
        this.aliases.add(alias);
    }

    protected void addSubCommand(IServerCommand subCommand)
    {
        this.subCommands.add(subCommand);
    }

    @Override
    public Collection<String> getAliases()
    {
        return this.aliases;
    }

    @Override
    public Collection<IServerCommand> getSubCommands()
    {
        return this.subCommands;
    }

    @Override
    public String getName()
    {
        return this.name;
    }

    @Override
    public int execute(CommandContext<CommandSourceStack> source)
    {
        return this.execute(source, this::onCommand);
    }

    protected int execute(CommandContext<CommandSourceStack> source, CommandExecutor<CommandSourceStack> executor)
    {
        try
        {
            executor.accept(source);
        }
        catch (CommandException | CommandSyntaxException e)
        {
            Entity entity = source.getSource().getEntity();

            if (entity != null)
            {
                ContextUtil.sendMessage(source, e.getMessage());
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    public abstract void onCommand(CommandContext<CommandSourceStack> source)
        throws CommandException, CommandSyntaxException;

}
