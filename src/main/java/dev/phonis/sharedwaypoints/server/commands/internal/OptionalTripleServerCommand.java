package dev.phonis.sharedwaypoints.server.commands.internal;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.phonis.sharedwaypoints.server.commands.argument.CommandArgument;
import dev.phonis.sharedwaypoints.server.commands.exception.CommandException;
import net.minecraft.commands.CommandSourceStack;

public abstract class OptionalTripleServerCommand<A, B, C> extends OptionalPairServerCommand<A, B>
{

    protected final CommandArgument<C> c;

    public OptionalTripleServerCommand(String name, CommandArgument<A> a, CommandArgument<B> b, CommandArgument<C> c)
    {
        super(name, a, b);

        this.c = c;
        this.c.setExecutor(source -> this.execute(source, this::passArgs));

        this.arguments.add(this.c);
    }

    private void passArgs(CommandContext<CommandSourceStack> source) throws CommandException, CommandSyntaxException
    {
        this.onOptionalCommand(source, (A) source.getArgument(this.a.name, Object.class), (B) source.getArgument(this.b.name, Object.class), (C) source.getArgument(this.c.name, Object.class));
    }

    protected abstract void onOptionalCommand(CommandContext<CommandSourceStack> source, A a, B b, C c)
        throws CommandException, CommandSyntaxException;

}
