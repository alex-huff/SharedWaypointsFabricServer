package dev.phonis.sharedwaypoints.server.commands.internal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public abstract class AbstractServerCommand implements IServerCommand {

    protected static final UUID systemUUID = new UUID(0, 0);

    private final String name;
    private final List<String> aliases = new LinkedList<>();
    private final List<IServerCommand> subCommands = new LinkedList<>();

    public AbstractServerCommand(String name) {
        this.name = name;
    }

    protected void addAliases(String alias) {
        this.aliases.add(alias);
    }

    protected void addSubCommand(IServerCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    @Override
    public Collection<String> getAliases() {
        return this.aliases;
    }

    @Override
    public Collection<IServerCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public int execute(CommandContext<ServerCommandSource> source) {
        return this.execute(source, this::onCommand);
    }

    protected int execute(CommandContext<ServerCommandSource> source, CommandExecutor<ServerCommandSource> executor) {
        try {
            executor.accept(source);
        } catch (CommandException | CommandSyntaxException e) {
            Entity entity = source.getSource().getEntity();

            if (entity != null)
                source.getSource().getEntity().sendSystemMessage(Text.of(e.getMessage()), AbstractServerCommand.systemUUID);
        }

        return Command.SINGLE_SUCCESS;
    }

    public abstract void onCommand(CommandContext<ServerCommandSource> source) throws CommandException, CommandSyntaxException;

}
