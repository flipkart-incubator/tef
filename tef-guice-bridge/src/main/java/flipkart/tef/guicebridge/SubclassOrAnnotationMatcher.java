package flipkart.tef.guicebridge;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.AbstractMatcher;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guice matcher for matching subclasses using TypeLiteral.
 * <p>
 * Date: 31/05/22
 */
public class SubclassOrAnnotationMatcher extends AbstractMatcher<TypeLiteral<?>> implements Serializable {
    /*
    Had to re-implement this class instead of using `Matchers.ofSubclass` since it was not based on TypeLiterals.
     */
    private final Class<?> superclass;

    public SubclassOrAnnotationMatcher(Class<?> superclass) {
        this.superclass = checkNotNull(superclass, "superclass");
    }

    @Override
    public boolean matches(TypeLiteral<?> subclass) {
        return subclass.getRawType().isAnnotationPresent(TefRequestScoped.class)
                || superclass.isAssignableFrom(subclass.getRawType());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubclassOrAnnotationMatcher && ((SubclassOrAnnotationMatcher) other).superclass.equals(superclass);
    }

    @Override
    public int hashCode() {
        return 37 * superclass.hashCode();
    }

    @Override
    public String toString() {
        return "subclassesOf(" + superclass.getSimpleName() + ".class)";
    }

    private static final long serialVersionUID = 0;
}
