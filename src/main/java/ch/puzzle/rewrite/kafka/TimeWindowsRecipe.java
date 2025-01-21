package ch.puzzle.rewrite.kafka;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.ExecutionContext;
import org.openrewrite.NlsRewrite.Description;
import org.openrewrite.NlsRewrite.DisplayName;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J.MethodInvocation;

public class TimeWindowsRecipe extends Recipe {

    @Override
    public @DisplayName @NotNull String getDisplayName() {
        return "Migrate Deprecated TimeWindows Methods";
    }

    @Override
    public @Description @NotNull String getDescription() {
        return "Migrates the deprecated methods of and grace.";
    }

    @Override
    public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaIsoVisitor<ExecutionContext>(){
            MethodMatcher ofMatcher = new MethodMatcher("org.apache.kafka.streams.kstream.TimeWindows of(..)");
            MethodMatcher graceMatcher = new MethodMatcher("org.apache.kafka.streams.kstream.TimeWindows grace(..)");

            private final JavaTemplate ofSizeWithNoGraceTemplate = JavaTemplate.builder("ofSizeWithNoGrace(#{})").contextSensitive().build();
            private final JavaTemplate ofSizeAndGraceTemplate = JavaTemplate.builder("ofSizeAndGrace(#{}, #{})").contextSensitive().build();

            @Override
            public @NotNull MethodInvocation visitMethodInvocation(MethodInvocation method, ExecutionContext c) {
                if(ofMatcher.matches(method)){
                    final String argument = method.getArguments().get(0).print(getCursor());
                    return ofSizeWithNoGraceTemplate.apply(getCursor(),method.getCoordinates().replaceMethod(), argument);
                } else if (graceMatcher.matches(method)
                            && method.getSelect() instanceof MethodInvocation mi) {
                    final String graceArgument = method.getArguments().get(0).print(getCursor());
                    final String ofArgument = mi.getArguments().get(0).print(getCursor());
                    final MethodInvocation result = ofSizeAndGraceTemplate.apply(getCursor(), method.getCoordinates().replaceMethod(), ofArgument, graceArgument);
                    return result.withSelect(mi.getSelect());
                }
                return super.visitMethodInvocation(method, c);
            }
        };
    }
}
