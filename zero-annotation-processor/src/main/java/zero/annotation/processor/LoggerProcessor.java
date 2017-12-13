package zero.annotation.processor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * @author Linbing Tang
 * @since 17-11-22.
 */
public abstract class LoggerProcessor extends AbstractProcessor {

  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
  }

  protected void error(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.ERROR, element, message, args);
  }

  protected void errorThrow(Element element, String message, Object... args) {
    message = printMessage(Diagnostic.Kind.ERROR, element, message, args);
    throw new RuntimeException(message);
  }

  protected void note(Element element, String message, Object... args) {
    printMessage(Diagnostic.Kind.NOTE, element, message, args);
  }

  private String printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
    if (args.length > 0) {
      message = String.format(message, args);
    }
    messager.printMessage(kind, message, element);
    return message;
  }
}
