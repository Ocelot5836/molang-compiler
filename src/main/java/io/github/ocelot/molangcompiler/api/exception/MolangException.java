package io.github.ocelot.molangcompiler.api.exception;

/**
 * <p>An exception that can be thrown by MoLang.</p>
 *
 * @author Ocelot
 * @since 1.0.0
 */
public class MolangException extends Exception
{
    public MolangException(String message)
    {
        super(message, null, true, true);
    }

    public MolangException(Throwable cause)
    {
        super(null, cause, true, true);
    }
}
