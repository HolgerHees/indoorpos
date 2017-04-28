package com.holgerhees.shared.persistance;

public class PersistanceException extends RuntimeException
{
    /**
     *
     */
    private static final long serialVersionUID = 8426962752345977108L;

    public PersistanceException(String message)
    {
        super(message);
    }

    public PersistanceException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
