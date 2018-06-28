package vrp.exceptions;

public class UnexpectedResponseStatusCode extends RuntimeException {
    public UnexpectedResponseStatusCode(String message) {
        super(message);
    }
}
