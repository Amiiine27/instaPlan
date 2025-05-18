package org.example.projets2.exception;

/**
 * Exception levée lorsqu'une tentative de connexion échoue.
 */
public class AuthenticationException extends Exception {

    /**
     * Crée une AuthenticationException avec un message utilisateur.
     * @param message explication de l'erreur (affichable à l'utilisateur)
     */
    public AuthenticationException(String message) {
        super(message);
    }

    /**
     * Crée une AuthenticationException en incluant la cause profonde.
     * Utile pour le debug en cas d'erreur JDBC ou autre.
     * @param message explication de l'erreur
     * @param cause   exception d'origine
     */
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}