
Reproducing bugs and confusing behaviour of micronaut.io.


- [$ expression in @Client annotation is not resolved when injecting low level http client](https://github.com/micronaut-projects/micronaut-core/issues/1144) - fixed in 1.0.4
- confusing errors when property is missing [one](wrong-property-reported/src/test/groovy/micronaut/problems/ConfusingErrorSpec.groovy) and [two](/wrong-property-reported2/src/test/groovy/micronaut/problems/ConfusingErrorSpec2.groovy)
- proxy problems when using ssl [one](https://github.com/micronaut-projects/micronaut-core/issues/1281) and [two](https://github.com/micronaut-projects/micronaut-core/issues/1343)
