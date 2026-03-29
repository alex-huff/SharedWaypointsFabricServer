package dev.phonis.sharedwaypoints.server.commands.internal;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.argument.CommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import net.minecraft.commands.CommandSourceStack;

public abstract class OptionalPairServerCommand<A, B> extends OptionalSingleServerCommand<A>
{

    protected final CommandArgument<B> b;

    public OptionalPairServerCommand(String name, CommandArgument<A> a, CommandArgument<B> b)
    {
        super(name, a);

        this.b = b;
        this.b.setExecutor(source -> this.execute(source, this::passArgs));

        this.arguments.add(this.b);
    }

    private void passArgs(CommandContext<CommandSourceStack> source) throws CommandException, CommandSyntaxException
    {
        this.onOptionalCommand(source, (A) source.getArgument(this.a.name, Object.class), (B) source.getArgument(this.b.name, Object.class));
    }

    protected abstract void onOptionalCommand(CommandContext<CommandSourceStack> source, A a, B b)
        throws CommandException, CommandSyntaxException;

}
