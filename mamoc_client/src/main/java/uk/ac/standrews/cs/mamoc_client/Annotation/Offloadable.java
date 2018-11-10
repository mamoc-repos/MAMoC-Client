package uk.ac.standrews.cs.mamoc_client.Annotation;

import org.atteo.classindex.IndexAnnotated;

@IndexAnnotated
public @interface Offloadable {
    boolean parallelizable() default false;
    boolean resourceDependent() default false;
}
