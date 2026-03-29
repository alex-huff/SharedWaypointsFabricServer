package dev.phonis.sharedwaypoints.server.commands.internal;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.argument.CommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import net.minecraft.commands.CommandSourceStack;

public abstract class OptionalSingleServerCommand<A> extends NoArgServerCommand
{

    protected final CommandArgument<A> a;

    public OptionalSingleServerCommand(String name, CommandArgument<A> a)
    {
        super(name);

        this.a = a;
        this.a.setExecutor(source -> this.execute(source, this::passArgs));

        this.arguments.add(this.a);
    }

    private void passArgs(CommandContext<CommandSourceStack> source) throws CommandException, CommandSyntaxException
    {
        this.onOptionalCommand(source, (A) source.getArgument(this.a.name, Object.class));
    }

    protected abstract void onOptionalCommand(CommandContext<CommandSourceStack> source, A a)
        throws CommandException, CommandSyntaxException;

}
