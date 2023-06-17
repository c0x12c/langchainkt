package langchainkt.internal;

public class Exceptions {

  public static IllegalArgumentException illegalArgument(String format, Object... args) {
    return new IllegalArgumentException(String.format(format, args));
  }
}
