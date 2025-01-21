package ch.puzzle.rewrite.kafka;

import static org.openrewrite.java.Assertions.java;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

public class TimeWindowTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new TimeWindowsRecipe());
    }

    @Test
    void migrateOf() {
        rewriteRun(
                java(
                        """
                                package ch.puzzle.kafka.traffic.stream;
                                
                                import java.time.Duration;
                                import org.apache.kafka.streams.kstream.TimeWindows;
                              
                                public class Dummy{
                                    void dummy(){
                                        TimeWindows windows = TimeWindows.of(Duration.ofSeconds(10));
                                    }
                                }
                                """,

                        """
                                package ch.puzzle.kafka.traffic.stream;
                                
                                import java.time.Duration;
                                import org.apache.kafka.streams.kstream.TimeWindows;
                              
                                public class Dummy{
                                    void dummy(){
                                        TimeWindows windows = TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(10));
                                    }
                                }
                                """
                )
        );
    }
    @Test
    void migrateOfGrace() {
        rewriteRun(
                java(
                        """
                                package ch.puzzle.kafka.traffic.stream;
                                
                                import java.time.Duration;
                                import org.apache.kafka.streams.kstream.TimeWindows;
                              
                                public class Dummy{
                                    void dummy(){
                                        TimeWindows windows = TimeWindows.of(Duration.ofSeconds(10)).grace(Duration.ofSeconds(10));
                                    }
                                }
                                """,

                        """
                                package ch.puzzle.kafka.traffic.stream;
                                
                                import java.time.Duration;
                                import org.apache.kafka.streams.kstream.TimeWindows;
                              
                                public class Dummy{
                                    void dummy(){
                                        TimeWindows windows = TimeWindows.ofSizeAndGrace(Duration.ofSeconds(10), Duration.ofSeconds(10));
                                    }
                                }
                                """
                )
        );
    }
}
