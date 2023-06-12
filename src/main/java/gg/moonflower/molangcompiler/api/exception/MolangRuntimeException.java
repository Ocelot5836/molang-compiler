package gg.moonflower.molangcompiler.api.exception;

/**
 * An exception that can be thrown by MoLang during runtime.
 *
 * @author Ocelot
 * @since 3.0.0
 */
public class MolangRuntimeException extends MolangException {

    public MolangRuntimeException(String message) {
        super(message);
    }

    public MolangRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MolangRuntimeException(Throwable cause) {
        super(cause);
    }
}
