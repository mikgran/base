package mg.util.functional.classmatching;

import java.util.function.BiFunction;
import java.util.function.Consumer;

// boldly borrowing someone else's solution right here:
public class ClassMatcher {

    private final BiFunction<Object, Consumer<Object>, Boolean> binder;

    public static ClassMatcher matcher() {
        return new ClassMatcher((a, b) -> false);
    }

    private ClassMatcher(BiFunction<Object, Consumer<Object>, Boolean> next) {
        this.binder = next;
    }

    public ClassMatcher fallthrough(final Consumer<Object> consumer) {
        return new ClassMatcher((obj, next) -> {

            if (binder.apply(obj, next)) {
                return true;
            }

            consumer.accept(obj);

            return true;

        });
    }

    public void match(Object o) {
        binder.apply(o, null);
    }

    public <Y> ClassMatcher with(final Class<Y> targetClass, final Consumer<Y> consumer) {
        return new ClassMatcher((object, next) -> {

            if (binder.apply(object, next)) {
                return true;
            }

            if (targetClass.isAssignableFrom(object.getClass())) {
                @SuppressWarnings("unchecked")
                final Y target = (Y) object;

                consumer.accept(target);

                return true;
            }

            return false;
        });
    }

    /*
    // Example caching method:
    import java.util.function.Supplier;
    public class ClassMatchCache {
        private ClassMatcher matcher;

        public ClassMatcher cache(Supplier<ClassMatcher> matchFactory) {
            if(matcher == null){
                matcher = matchFactory.get();
            }

            return matcher;
        }
    }
    // example cache usage:
    public class EventHandler {

        private ClassMatchCache mainDispatcher = new ClassMatchCache();

        public void dispatch(Object o){

            mainDispatcher.cache(
                () -> match().with(Bar.class, this::bar)
                             .with(Biz.class, this::biz)
                             .with(Baz.class, this::baz)
                             .with(Foo1.class, this::foo)
                             .with(Foo2.class, this::foo)
                             .with(Foo3.class, this::foo)
                             .with(Foo4.class, this::foo)
                             .with(Foo5.class, this::foo)
                             .with(Foo13.class, this::foo)
                             .fallthrough(this::fallthrough))
                             .exec(o);
        }
    }
    */
}