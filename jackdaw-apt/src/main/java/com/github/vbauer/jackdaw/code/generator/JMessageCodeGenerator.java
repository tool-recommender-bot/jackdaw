package com.github.vbauer.jackdaw.code.generator;

import com.github.vbauer.jackdaw.annotation.JMessage;
import com.github.vbauer.jackdaw.code.base.BaseCodeGenerator;
import com.github.vbauer.jackdaw.code.context.CodeGeneratorContext;
import com.github.vbauer.jackdaw.util.DateTimeUtils;
import com.github.vbauer.jackdaw.util.MessageUtils;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.text.Format;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * @author Vladislav Bauer
 */

public class JMessageCodeGenerator extends BaseCodeGenerator {

    private static final Collection<Format> DATE_FORMATS =
        DateTimeUtils.createDefaultDateFormats();


    @Override
    public final Class<JMessage> getAnnotation() {
        return JMessage.class;
    }

    @Override
    public final void generate(final CodeGeneratorContext context) {
        final TypeElement typeElement = context.getTypeElement();
        printInfoIfNecessary(typeElement);
    }


    private <T extends Element> void printInfoIfNecessary(final T rootElement) {
        printElementInfoIfNecessary(rootElement);

        if (rootElement instanceof ExecutableElement) {
            final ExecutableElement executableElement = (ExecutableElement) rootElement;
            printInfoIfNecessary(executableElement.getParameters());
        }

        final List<? extends Element> elements = rootElement.getEnclosedElements();
        printInfoIfNecessary(elements);
    }

    private <T extends Element> void printInfoIfNecessary(final List<T> elements) {
        for (final T element : elements) {
            printInfoIfNecessary(element);
        }
    }

    private void printElementInfoIfNecessary(final Element element) {
        final JMessage annotation = element.getAnnotation(JMessage.class);

        if (annotation != null) {
            final String before = annotation.before();
            final String after = annotation.after();

            if (showMessage(before, after)) {
                final Diagnostic.Kind type = annotation.type();
                final String[] messages = annotation.value();
                final boolean details = annotation.details();
                final Element elem = details ? element : null;

                for (final String message : messages) {
                    MessageUtils.message(type, message, elem);
                }
            }
        }
    }

    private boolean showMessage(final String beforeString, final String afterString) {
        final Date before = DateTimeUtils.parseDate(beforeString, DATE_FORMATS);
        final Date after = DateTimeUtils.parseDate(afterString, DATE_FORMATS);

        final Date current = new Date();
        return (before == null || before.before(current))
            && (after == null || after.after(current));
    }

}
