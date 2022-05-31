package flipkart.tef.guicebridge;

import com.google.inject.matcher.AbstractMatcher;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guice matcher for matching subclasses using TypeLiteral.
 *
 * Date: 31/05/22
 */
public class SubclassesOfMatcher extends AbstractMatcher<Object> implements Serializable {
    /*
    Had to re-implement this class instead of using `Matchers.ofSubclass` since it was not based on TypeLiterals.
     */
    private final Class<?> superclass;

    public SubclassesOfMatcher(Class<?> superclass) {
        this.superclass = checkNotNull(superclass, "superclass");
    }

    @Override
    public boolean matches(Object subclass) {
        return superclass.isAssignableFrom(subclass.getClass());
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof SubclassesOfMatcher && ((SubclassesOfMatcher) other).superclass.equals(superclass);
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
